from dashscope import Application
from http import HTTPStatus
import os
import json
from multiRAG import MultiRAG

APP_ID = "c2affdebf6664d438a4043216ee15dea"
apiKey= "sk-93817db303964020bbc79b017be4768b"

class LLM_model:
    def __init__(self):
        """
        初始化LLM模型实例
        """
        self.session_id = "default_session"
        # 初始化MultiRAG系统
        self.multirag = MultiRAG(
            index_path="./faiss_index1",
            collection_name="document_embeddings",
            embedding_model_path="./Qwen3-Embedding-0.6B",
            cross_encoder_path="./cross-encoder-model",
            image_output_dir="./processed_images",
            image_mapping_file="./image_mapping.json"
        )
        print("MultiRAG系统初始化完成")

    def start_LLM(self):
        """
        启动LLM服务
        
        Returns:
            str: 启动状态信息
        """
        return "LLM model started successfully"
    
    def retrieve_and_answer(self, query: str, top_k: int = 5):
        """
        智能检索并回答问题
        
        Args:
            query (str): 用户问题
            top_k (int): 检索的片段数量
            
        Yields:
            str: 生成的文本增量
        """
        try:
            # 1. 使用MultiRAG检索相关片段
            print(f"正在检索与问题相关的top-{top_k}片段...")
            results = self.multirag.retrieve(query, topk=top_k)
            
            if not results:
                print("未找到相关片段，使用通用知识回答")
                yield from self.call_llm_stream(query, [])
                return
            
            # 2. 处理检索结果，根据MultiRAG的返回格式
            text_chunks = []
            image_info = []
            
            for result in results:
                result_type = result.get('type', 0)  # 0代表纯文字，1代表图片描述
                document = result.get('document', '')
                source = result.get('source', '')
                
                if result_type == 1:  # 图片描述
                    if source and source != "":
                        image_info.append({
                            'description': document,
                            'path': source,
                            'score': 1.0  # MultiRAG已经排序过了
                        })
                        # 将图片描述也加入文本片段，但标注为图片
                        text_chunks.append(f"[图片内容] {document} [图片地址: {source}]")
                    else:
                        text_chunks.append(f"[图片内容] {document}")
                else:  # 普通文本片段
                    text_chunks.append(document)
            
            print(f"检索到 {len(text_chunks)} 个文本片段，{len(image_info)} 个图片")
            
            # 3. 构建增强的prompt
            enhanced_chunks = self._enhance_chunks_with_images(text_chunks, image_info)
            
            # 4. 调用LLM生成回答
            yield from self.call_llm_stream(query, enhanced_chunks)
            
        except Exception as e:
            print(f"检索过程出错: {e}")
            import traceback
            traceback.print_exc()
            # 出错时回退到原始方法
            yield from self.call_llm_stream(query, [])
    
    def _enhance_chunks_with_images(self, text_chunks, image_info):
        """
        根据图片信息增强文本片段
        
        Args:
            text_chunks (list): 文本片段列表
            image_info (list): 图片信息列表
            
        Returns:
            list: 增强后的文本片段
        """
        enhanced_chunks = text_chunks.copy()
        
        # 如果有图片信息，添加图片使用说明
        if image_info:
            image_instruction = "\n注意：回答中如需引用图片，请直接使用图片地址，格式为：[具体路径]\n"
            enhanced_chunks.append(image_instruction)
            
            # 添加图片摘要信息
            image_summary = "可用图片资源：\n"
            for i, img in enumerate(image_info[:3]):  # 最多显示3个最相关的图片
                image_summary += f"{i+1}. {img['description']} [地址: {img['path']}]\n"
            enhanced_chunks.append(image_summary)
        
        return enhanced_chunks

    def call_llm_stream(self, query, list):
        """
        流式生成回答，只返回增量字符
        
        Args:
            query (str): 用户问题
            list (list): 相关文档片段列表
            
        Yields:
            str: 生成的文本增量
        """
        # 将换行符提取到变量中，避免f-string中的反斜杠问题
        separator = "\n\n"
        prompt = f"""你是一位知识助手，请根据用户的问题和相关背景知识回答问题。

用户问题: {query}

背景知识:
{separator.join(list)}

回答要求：
1. 如果背景知识中包含图片信息（标注为[图片内容]或[图片地址]），请在回答中适当引用
2. 引用图片时，直接使用提供的图片地址，格式：[具体路径]
3. 不要描述图片内容，直接用图片地址代替
4. 若用户问题与背景知识无关，则用通用知识解决问题
5.如果遇到需要输入图片地址的情况，直接输出[图片具体路径即可]，不要输出[图片地址: 具体路径]，也就是说框内只需要有一个路径，无需任何解释或前缀后缀以及标题

"""

        prev = ""  # 记录上一次完整内容，用于计算增量
        responses = Application.call(
            api_key=apiKey,
            app_id=APP_ID,
            prompt=prompt,
            session_id=self.session_id,
            stream=True
        )
        
        for response in responses:
            if response.status_code == HTTPStatus.OK:
                current = response.output.text
                # 计算增量：当前完整内容减去之前的内容
                delta = current[len(prev):]
                prev = current
                if delta:  # 只有当有新内容时才yield
                    yield delta
            else:
                print(f'Request id: {response.request_id}')
                print(f'Status code: {response.status_code}')
                print(f'Error code: {response.code}')
                print(f'Error message: {response.message}')
                break