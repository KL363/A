<template>
    <ElContainer class="background">
        <ElAside class="toolbar">
            <ElContainer style="display: flex;">
                <ElHeader style="display: flex;flex-direction: row;margin-top: 45px;">
                    <ElImage class="avatar" src="/src/images/039-coconutdrink.svg"></ElImage>
                    <div style="display: flex;flex-direction: column;align-items: center;">
                        <div class="user-name">UserName</div>
                        <div style="color: wheat;">name</div>
                    </div>
                </ElHeader>
                <ElFooter style="margin-top: 530px;">
                    <ElLink :icon="Plus" type="success" :underline="false" @click="startNewChat">
                        &nbsp;&nbsp;新对话</ElLink>
                    <br></br>
                    <br></br>
                    <ElLink @click="openHistoryDialog" :icon="Document" type="success" :underline="false">
                        &nbsp;&nbsp;历史记录</ElLink>
                    <br></br>
                    <br></br>
                    <ElLink :icon="Back" type="success" :underline="false" href="/login">
                        &nbsp;&nbsp;退出登录</ElLink>
                </ElFooter>
            </ElContainer>
        </ElAside>
        <ElMain class="main-place">
            <ElContainer>
                <ElHeader style="justify-items: center;">
                    <div style="font-size: 33px;color: green;font-weight: 700;margin: 10px;">Multi-Agent问答平台</div>
                    <div style="font-size: 12px;color: gray;">众星汇智，织就问答的光；每一次叩问，都遇见通透的方向</div>
                </ElHeader>
                <br></br>
                <ElDivider style="border-color:lightgrey"></ElDivider>
                <ElMain class="chat-container">
                    <div class="messages-wrapper">
                        <div v-for="(message, index) in chatMessages" :key="index" class="message"
                            :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'ai' }">
                            <div class="message-content">
                                <div v-if="message.role === 'user'" class="message-avatar">
                                    <div class="message-text">
                                        <template v-if="message.type === 'text'">
                                            <div v-html="parseContent(message.content)"></div>
                                        </template>
                                        <template v-else-if="message.type === 'image'">
                                            <img :src="message.content" class="message-image" alt="Uploaded image" />
                                        </template>
                                    </div>
                                    <div>&nbsp;</div>
                                    <div>&nbsp;</div>
                                    <ElImage class="avatar" :src="message.avatar"></ElImage>
                                </div>
                                <div v-if="message.role === 'ai'" class="message-avatar">
                                    <ElImage class="avatar" :src="message.avatar"></ElImage>
                                    <div>&nbsp;</div>
                                    <div>&nbsp;</div>
                                    <div class="message-text">
                                        <template v-if="message.type === 'text'">
                                            <div v-html="parseContent(message.content)"></div>
                                        </template>
                                        <template v-else-if="message.type === 'image'">
                                            <img :src="message.content" class="message-image" alt="Uploaded image" />
                                        </template>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </ElMain>
                <div class="falling-images-container">
                    <div v-for="(image, index) in fallingImages" :key="index" class="falling-image" :style="{
                        left: image.left + 'px',
                        animationDuration: image.duration + 's',
                        width: image.size + 'px',
                        height: image.size + 'px',
                        opacity: image.opacity
                    }">
                        <img :src="image.src" :alt="'falling-image-' + index" />
                    </div>
                </div>
                <ElFooter style="margin-bottom: 20px;">
                    <ElInput type="textare" v-model="UserQuestion" style="height: 60px;font-size: 19px;"
                        @keyup.enter="sendMessage" placeholder="输入您的问题...">
                        <template #suffix>
                            <input type="file" ref="fileInput" style="display: none" accept="image/*"
                                @change="handleFileUpload">
                            <el-button :plain="true" type="success" :icon="FolderOpened" :circle="true"
                                @click="triggerFileInput"></el-button>
                            <el-button v-if="!isLoading" :plain="true" type="success" :icon="Promotion" :circle="true"
                                @click="sendMessage"></el-button>
                            <el-button v-else :plain="true" type="danger" :icon="SwitchButton" :circle="true"
                                @click="cancelRequest" title="强制打断"></el-button>
                        </template>
                    </ElInput>
                </ElFooter>
            </ElContainer>
        </ElMain>
    </ElContainer>
    <ElDialog v-model="dialogVisible" title="历史记录" width="50%" draggable>
        <div class="history-dialog-content">
            <ElTable :data="historyRecords" style="width: 100%" v-loading="historyLoading" class="history-table">
                <ElTableColumn prop="title" label="对话标题" width="180" />
                <ElTableColumn prop="createTime" label="日期" width="180" />
                <ElTableColumn label="操作">
                    <template #default="scope">
                        <ElButton size="small" @click="loadHistory(scope.row)">加载</ElButton>
                        <ElButton size="small" type="danger" @click="deleteHistory(scope.row.id)">删除</ElButton>
                    </template>
                </ElTableColumn>
            </ElTable>
        </div>
    </ElDialog>
</template>

<script setup lang="ts">
import { ElAside, ElButton, ElContainer, ElDialog, ElDivider, ElFooter, ElHeader, ElImage, ElInput, ElLink, ElMain, ElMessage, ElTable, ElTableColumn } from 'element-plus';
import { Document, Plus, Back, Promotion, FolderOpened, SwitchButton } from '@element-plus/icons-vue'
import axios from 'axios';
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import MarkdownIt from 'markdown-it';

// 初始化markdown-it
const md = new MarkdownIt({
  html: true, // 允许HTML标签
  linkify: true, // 自动转换URL为链接
  typographer: true, // 启用一些语言中立的替换和引号美化
});

// 聊天相关状态
const UserQuestion = ref("");
const chatMessages = ref<Array<{
    avatar: string | "/src/images/048-gingerbread.svg",
    role: 'user' | 'ai',
    content: string,
    type: 'text' | 'image',
    timestamp?: string
}>>([]);
const isLoading = ref(false);
const dialogVisible = ref(false);
const historyLoading = ref(false);
const historyRecords = ref<Array<{
    id: string,
    title: string,
    createTime: string,
    messages: Array<{ role: 'user' | 'ai', content: string, type: 'text' | 'image' }>
}>>([]);
const currentSessionId = ref<string | null>(null);
const MAX_CONVERSATION_LENGTH = 20; // 最大对话长度
const fileInput = ref<HTMLInputElement | null>(null);
const avatar = ref("/src/images/048-gingerbread.svg"); // 默认头像
// 用于SSE连接的EventSource
let eventSource: EventSource | null = null;

// 修改parseContent方法，支持Markdown解析
const parseContent = (content: string) => {
  // 先处理图片路径
  let processedContent = content.replace(
    /\[\.\/processed_images\/([^\]]+)\]/g, 
    (match, filename) => {
      return `<img src="/src/images/processed_images/${filename}" alt="${filename.replace(/\..+$/, '')}" class="content-image">`;
    }
  );
  
  // 然后解析Markdown
  return md.render(processedContent);
};

// 触发文件输入
const triggerFileInput = () => {
    if (fileInput.value) {
        fileInput.value.click();
    }
};

// 处理文件上传
const handleFileUpload = (event: Event) => {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
        const file = input.files[0];
        if (!file.type.match('image.*')) {
            ElMessage.warning('请选择图片文件');
            return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            const imageUrl = e.target?.result as string;
            sendImageMessage(imageUrl);
        };
        reader.readAsDataURL(file);
    }
};

// 发送图片消息
const sendImageMessage = (imageUrl: string) => {
    if (isLoading.value) return;

    // 检查对话长度是否超出限制
    if (checkConversationLength()) {
        ElMessage.warning("对话长度已达到限制，请开启新对话");
        return;
    }

    chatMessages.value.push({
        avatar: avatar.value,
        role: 'user',
        content: imageUrl,
        type: 'image'
    });
    isLoading.value = true;

    // 模拟AI回复（实际项目中应该调用API）
    setTimeout(() => {
        chatMessages.value.push({
            avatar: avatar.value,
            role: 'ai',
            content: "这是一张图片，我已经收到了。",
            type: 'text'
        });
        isLoading.value = false;

        nextTick(() => {
            const container = document.querySelector('.messages-wrapper');
            if (container) {
                container.scrollTo({
                    top: container.scrollHeight,
                    behavior: 'smooth'
                });
            }
        });
    }, 1500);
};

// 获取当前时间戳（中国时区）
const getCurrentTimestamp = () => {
    return new Date().toLocaleString('zh-CN', {
        timeZone: 'Asia/Shanghai',
        hour12: false
    });
};

// 检查对话长度是否超出限制
const checkConversationLength = () => {
    return chatMessages.value.length >= MAX_CONVERSATION_LENGTH;
};

// 打开历史记录对话框
const openHistoryDialog = async () => {
    dialogVisible.value = true;
    await fetchHistoryRecords();
};

// 获取历史记录
const fetchHistoryRecords = async () => {
    try {
        historyLoading.value = true;
        const response = await axios.post('http://127.0.0.1:8080/ai/GetHistory', {
            id: localStorage.getItem("id")
        }, {
            headers: {
                'Content-Type': 'application/json',
                'id': localStorage.getItem("id"),
                'uuid': localStorage.getItem("uuid")
            }
        });

        if (response.data.toString() === 'Not Login') {
            ElMessage.error("Not Login")
            window.location.href = "/login"
            localStorage.removeItem('id')
            localStorage.removeItem('uuid')
            return null
        }

        if (response.data && response.data.data) {
            historyRecords.value = response.data.data.map((item: any) => ({
                id: item.id.toString(),
                title: item.title || '未命名对话',
                createTime: item.date,
                messages: item.messages || []
            }));
        }
    } catch (error) {
        console.error("获取历史记录失败:", error);
        ElMessage.error("获取历史记录失败");
    } finally {
        historyLoading.value = false;
    }
};

// 加载历史记录
const loadHistory = async (record: any) => {
    if (isLoading.value) {
        cancelRequest();
    }
    try {
        historyLoading.value = true;

        // 调用API加载历史记录
        const response = await axios.post('http://127.0.0.1:8080/ai/HistoryLoad', {
            userId: localStorage.getItem("id"),
            historyId: record.id
        }, {
            headers: {
                'Content-Type': 'application/json',
                'id': localStorage.getItem("id"),
                'uuid': localStorage.getItem("uuid")
            }
        });

        if (response.data.toString() === 'Not Login') {
            ElMessage.error("Not Login")
            window.location.href = "/login"
            localStorage.removeItem('id')
            localStorage.removeItem('uuid')
            return null
        }

        if (response.data && response.data.data) {
            // 更新当前会话ID
            currentSessionId.value = record.id;
            // 转换消息格式
            const messages = response.data.data.map((msg: any) => ({
                avatar: msg.avatar,
                role: msg.role,
                content: msg.content,
                type: msg.type || 'text' // 默认为文本类型
            }));

            // 更新聊天消息
            chatMessages.value = messages;

            // 关闭对话框
            dialogVisible.value = false;

            ElMessage.success("历史记录加载成功");

            // 滚动到底部
            nextTick(() => {
                const container = document.querySelector('.messages-wrapper');
                if (container) {
                    container.scrollTo({
                        top: container.scrollHeight,
                        behavior: 'smooth'
                    });
                }
            });
        } else {
            ElMessage.warning("该历史记录没有可加载的内容");
        }
    } catch (error) {
        console.error("加载历史记录失败:", error);
        ElMessage.error("加载历史记录失败");
    } finally {
        historyLoading.value = false;
    }
};

// 发送消息历史到后端
const sendMessageHistory = async (avatar: string, role: 'user' | 'ai', content: string, type: 'text' | 'image' = 'text') => {
    try {
        const timestamp = getCurrentTimestamp();
        if (avatar !== "/src/images/048-gingerbread.svg") {
            const response = await axios.post('http://127.0.0.1:8080/ai/history', {
                avatar: avatar,
                role: role,
                content: content,
                type: type,
                askTime: timestamp,
                userId: localStorage.getItem("id")
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'id': localStorage.getItem("id"),
                    'uuid': localStorage.getItem("uuid")
                }
            });

            if (response.data.toString() === 'Not Login') {
                ElMessage.error("Not Login")
                window.location.href = "/login"
                localStorage.removeItem('id')
                localStorage.removeItem('uuid')
                return null
            }

            return response.data;
        }
    } catch (error) {
        console.error("发送消息历史失败:", error);
        throw error;
    }
};

// 创建新会话
const createNewSession = async () => {
    try {
        const response = await axios.post('http://127.0.0.1:8080/ai/NewSession', {
            userId: localStorage.getItem("id")
        }, {
            headers: {
                'Content-Type': 'application/json',
                'uuid': localStorage.getItem("uuid"),
                'id': localStorage.getItem("id")
            }
        });

        if (response.data.toString() === 'Not Login') {
            ElMessage.error("Not Login")
            window.location.href = "/login"
            localStorage.removeItem('id')
            localStorage.removeItem('uuid')
            return null
        }

        currentSessionId.value = response.data.data;
        return response.data;
    } catch (error) {
        console.error("创建新会话失败:", error);
        throw error;
    }
};

// 连接到SSE流
const connectToSSE = (userInput: string) => {
    // 关闭之前的连接
    if (eventSource) {
        eventSource.close();
    }

    // 创建新的AI消息条目，初始为空
    const aiMessageIndex = chatMessages.value.push({
        avatar: avatar.value,
        role: 'ai',
        content: '',
        type: 'text'
    }) - 1;

    // 创建新的EventSource连接
    eventSource = new EventSource(`http://127.0.0.1:8080/ai/stream-chat?message=${encodeURIComponent(userInput)}&userId=${localStorage.getItem("id")}&uuid=${localStorage.getItem("uuid")}&historyId=${currentSessionId.value}`);

    eventSource.onmessage = (event) => {
        if (event.data === null) {
            localStorage.removeItem('id');
            localStorage.removeItem('uuid');
            ElMessage.error("Not Login");
            window.location.href = "/login";
            return;
        }

        try {
            const data = JSON.parse(event.data);
            if (data.avatar) {
                chatMessages.value[aiMessageIndex].avatar = "/src/images/" + data.avatar.replace(/\s/g, '');
                avatar.value = chatMessages.value[aiMessageIndex].avatar;
            }
            if (data.delta) {
                chatMessages.value[aiMessageIndex].content += data.delta;
            }
        } catch (e) {
            ElMessage.error("解析AI回复失败，请稍后再试");
            console.error("解析AI回复失败:", e);
        }
        // 自动滚动到底部
        nextTick(() => {
            const container = document.querySelector('.messages-wrapper');
            if (container) {
                container.scrollTo({
                    top: container.scrollHeight,
                    behavior: 'smooth'
                });
            }
        });
    };

    eventSource.onerror = (error) => {
        console.error('SSE error:', error);
        // 标记加载完成
        isLoading.value = false;
        // 关闭连接
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }

        // 如果AI消息为空，添加一条错误消息
        if (chatMessages.value[aiMessageIndex].content === '') {
            chatMessages.value[aiMessageIndex].content = "抱歉，获取回复时出现错误。";
        }

        // 保存AI回复历史
        sendMessageHistory(avatar.value, 'ai', chatMessages.value[aiMessageIndex].content);
    };
};

// 修改cancelRequest方法
const cancelRequest = () => {
    if (eventSource) {
        eventSource.close();
        eventSource = null;
        ElMessage.warning("已取消当前请求");
        isLoading.value = false;
    }
};

// 发送消息
const sendMessage = async () => {
    if (!UserQuestion.value.trim() || isLoading.value) return;

    // 检查对话长度是否超出限制
    if (checkConversationLength()) {
        ElMessage.warning("对话长度已达到限制，请开启新对话");
        return;
    }

    const userMessage = UserQuestion.value;
    chatMessages.value.push({
        avatar: "/src/images/039-coconutdrink.svg",
        role: 'user',
        content: userMessage,
        type: 'text'
    });
    UserQuestion.value = "";
    isLoading.value = true;

    try {
        // 发送用户消息历史
        await sendMessageHistory('/src/images/039-coconutdrink.svg', 'user', userMessage);

        // 连接到SSE流
        connectToSSE(userMessage);

    } catch (error: any) {
        console.error("发送消息失败:", error);
        chatMessages.value.push({
            avatar: avatar.value,
            role: 'ai',
            content: "抱歉，发送消息时出现错误。",
            type: 'text'
        });
        isLoading.value = false;
    }
};

// 开始新对话
const startNewChat = async () => {
    // 如果正在加载（即AI正在回答），先取消当前请求
    if (isLoading.value) {
        cancelRequest();
    }

    // 关闭SSE连接
    if (eventSource) {
        eventSource.close();
        eventSource = null;
    }

    if (chatMessages.value.length > 0) {
        chatMessages.value = [];
    }

    try {
        await createNewSession();
        ElMessage.success("新会话已创建");
    } catch (error) {
        ElMessage.error("创建新会话失败");
    }
};

// 删除历史记录
const deleteHistory = async (id: string) => {
    if (isLoading.value) {
        cancelRequest();
    }
    try {
        const response = await axios.post('http://127.0.0.1:8080/ai/DeleteHistory', {
            historyId: id,
            userId: localStorage.getItem("id")
        }, {
            headers: {
                'Content-Type': 'application/json',
                'id': localStorage.getItem("id"),
                'uuid': localStorage.getItem("uuid")
            }
        });

        if (response.data.toString() === 'Not Login') {
            ElMessage.error("Not Login")
            window.location.href = "/login"
            localStorage.removeItem('id')
            localStorage.removeItem('uuid')
            return null
        }

        // 检查当前会话ID是否等于被删除的会话ID
        if (currentSessionId.value?.toString() === id) {
            // 清空当前会话
            chatMessages.value = [];
            // 创建新的会话
            createNewSession();
            ElMessage.success("当前会话已被删除，已创建新会话");
        }

        historyRecords.value = historyRecords.value.filter(r => r.id !== id);
        ElMessage.success("历史记录已删除");
    } catch (error) {
        console.error("删除历史记录失败:", error);
        ElMessage.error("删除历史记录失败");
    }
};

// 图片掉落效果相关代码
interface FallingImage {
    src: string;
    left: number;
    duration: number;
    size: number;
    opacity: number;
}

const fallingImages = ref<FallingImage[]>([]);
const imageSources = [
    "src/images/007-gintonic.svg",
    "src/images/014-mojito.svg",
    "src/images/042-milkshake.svg",
    "src/images/044-whiskeysour.svg",
    "src/images/039-coconutdrink.svg",
    "src/images/050-lemonjuice.svg",
    "src/images/048-gingerbread.svg"
];

const getRandom = (min: number, max: number) => {
    return Math.random() * (max - min) + min;
};

const addFallingImage = () => {
    const containerWidth = document.querySelector('.main-place')?.clientWidth || 800;
    fallingImages.value.push({
        src: imageSources[Math.floor(Math.random() * imageSources.length)],
        left: getRandom(0, containerWidth - 50),
        duration: getRandom(5, 15),
        size: 30,
        opacity: getRandom(0.1, 0.3)
    });
    // 限制最大数量，避免性能问题
    if (fallingImages.value.length > 20) {
        fallingImages.value.shift();
    }
};

let intervalId: number;

onMounted(async () => {
    createNewSession();
    for (let i = 0; i < 5; i++) {
        addFallingImage();
    }
    intervalId = window.setInterval(() => {
        addFallingImage();
    }, 2000);
});

onUnmounted(() => {
    clearInterval(intervalId);
    if (eventSource) {
        eventSource.close();
    }
});
</script>

<style lang="css" scoped>
/* 原有样式保持不变 */
.toolbar {
    height: 100vh;
    width: 33vh;
    background-color: rgb(30, 85, 0);
    border-top-right-radius: 2vb;
    border-bottom-right-radius: 2vb;
    align-items: center;
    justify-content: center;
    justify-items: center;
}

.avatar {
    padding-top: 10px;
    width: 40px;
    height: 40px;
    min-width: 40px;
    min-height: 40px;
    object-fit: cover;
}

.user-name {
    padding-left: 15px;
    margin-right: 20px;
    font-size: 20px;
    font-weight: 700;
    color: rgb(235, 243, 120);
}

.background {
    display: flex;
    height: 100vh;
    background-image: url("/src/images/HomeBackground.png");
    background-size: cover;
    background-position: center;
}

.main-place {
    display: flex;
    justify-content: center;
    position: relative;
    overflow: hidden;
    height: 100vh;
}

.chat-container {
    padding: 20px;
    height: calc(100vh - 300px);
    overflow: hidden;
    position: relative;
}

.messages-wrapper {
    height: 100%;
    overflow-y: auto;
    padding-right: 10px;
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 128, 0, 0.5) transparent;

    mask-size: 100% 40px, 100% calc(100% - 80px);
    mask-position: top, bottom;
    mask-repeat: no-repeat;
}

.messages-wrapper::-webkit-scrollbar {
    width: 8px;
}

.messages-wrapper::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 4px;
}

.messages-wrapper::-webkit-scrollbar-thumb {
    background-color: rgba(0, 128, 0, 0.5);
    border-radius: 4px;
    border: 2px solid transparent;
    background-clip: content-box;
    transition: background-color 0.3s;
}

.messages-wrapper::-webkit-scrollbar-thumb:hover {
    background-color: rgba(0, 128, 0, 0.7);
}

.message {
    margin-bottom: 15px;
    display: flex;
}

.message-content {
    display: flex;
    max-width: 80%;
    align-items: flex-start;
}

.message-avatar {
    display: flex;
    flex-direction: row;
    margin-left: 10px;
}

.message-text {
    padding: 12px 16px;
    border-radius: 18px;
    line-height: 1.5;
    max-width: calc(100% - 50px);
    word-break: break-word;
}

.message-image {
    max-width: 100%;
    max-height: 300px;
    border-radius: 8px;
    object-fit: contain;
}

.user-message {
    justify-content: flex-end;
}

.user-message .message-text {
    background-color: #f0f9eb;
    color: #333;
    border-bottom-right-radius: 4px;
}

.ai-message {
    justify-content: flex-start;
}

.ai-message .message-text {
    background-color: #ecf5ff;
    color: #333;
    border-bottom-left-radius: 4px;
}

/* 加载消息样式 */
.loading-message {
    min-height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.loading-dots {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
}

.loading-dots .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #67c23a;
    animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots .dot:nth-child(1) {
    animation-delay: -0.32s;
}

.loading-dots .dot:nth-child(2) {
    animation-delay: -0.16s;
}

@keyframes bounce {

    0%,
    80%,
    100% {
        transform: scale(0);
    }

    40% {
        transform: scale(1.0);
    }
}

.falling-images-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.falling-image {
    position: absolute;
    top: -100px;
    animation-name: fall;
    animation-timing-function: linear;
    animation-iteration-count: 1;
    will-change: transform;
}

.falling-image img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: opacity 0.3s ease;
}

@keyframes fall {
    from {
        transform: translateY(-100px);
    }

    to {
        transform: translateY(calc(100vh + 100px));
    }
}

/* 新增的历史记录对话框样式 */
.history-dialog-content {
    /* background-color: #f0f9e6; 黄绿色背景 */
    border-radius: 8px;
    padding: 15px;
    max-height: 60vh;
    /* 设置最大高度 */
    overflow-y: auto;
    /* 超出部分可滚动 */
}

.history-dialog-content::-webkit-scrollbar {
    width: 8px;
}

.history-dialog-content::-webkit-scrollbar-track {
    background: #f0f9e6;
    border-radius: 4px;
}

.history-dialog-content::-webkit-scrollbar-thumb {
    background-color: rgba(144, 238, 144, 0.7);
    border-radius: 4px;
    border: 2px solid #f0f9e6;
}

.history-dialog-content::-webkit-scrollbar-thumb:hover {
    background-color: rgba(144, 238, 144, 0.9);
}

.history-table {
    background-color: transparent;
}

.history-table :deep(.el-table__body-wrapper) {
    scrollbar-width: thin;
    scrollbar-color: rgba(144, 238, 144, 0.7) #f0f9e6;
}

.history-table :deep(.el-table__body-wrapper)::-webkit-scrollbar {
    height: 8px;
}

.history-table :deep(.el-table__body-wrapper)::-webkit-scrollbar-track {
    background: #f0f9e6;
}

.history-table :deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb {
    background-color: rgba(144, 238, 144, 0.7);
    border-radius: 4px;
    border: 2px solid #f0f9e6;
}

.history-table :deep(.el-table__body-wrapper)::-webkit-scrollbar-thumb:hover {
    background-color: rgba(144, 238, 144, 0.9);
}

.history-table :deep(.el-table th) {
    background-color: #e1f5d4 !important;
}

.history-table :deep(.el-table tr) {
    background-color: rgba(255, 255, 255, 0.7);
}

.history-table :deep(.el-table--enable-row-hover .el-table__body tr:hover>td) {
    background-color: rgba(224, 255, 224, 0.7) !important;
}

/* 新增的Markdown样式 */
.message-text :deep(p) {
    margin: 0.5em 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
    margin: 0.5em 0;
    padding-left: 1.5em;
}

.message-text :deep(li) {
    margin: 0.25em 0;
}

.message-text :deep(blockquote) {
    margin: 0.5em 0;
    padding-left: 1em;
    border-left: 3px solid #ccc;
    color: #666;
}

.message-text :deep(pre) {
    background-color: #f5f5f5;
    padding: 0.5em;
    border-radius: 4px;
    overflow-x: auto;
}

.message-text :deep(code) {
    font-family: monospace;
    background-color: #f5f5f5;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-size: 0.9em;
}

.message-text :deep(a) {
    color: #409eff;
    text-decoration: none;
}

.message-text :deep(a:hover) {
    text-decoration: underline;
}

.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
    margin: 0.8em 0 0.4em;
    font-weight: bold;
}

.message-text :deep(h1) {
    font-size: 1.5em;
    border-bottom: 1px solid #eee;
}

.message-text :deep(h2) {
    font-size: 1.3em;
    border-bottom: 1px solid #eee;
}

.message-text :deep(h3) {
    font-size: 1.1em;
}

.message-text :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
}

.message-text :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 0.5em 0;
}

.message-text :deep(th),
.message-text :deep(td) {
    border: 1px solid #ddd;
    padding: 0.3em 0.5em;
}

.message-text :deep(th) {
    background-color: #f2f2f2;
}
</style>