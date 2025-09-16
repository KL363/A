<template>
    <div class="login-background">
        <div class="login-place">
            <div class="login-title">
                <strong class="login-main-title">Multi-Agent问答平台</strong>
                <div class="login-introduce">知识问答，让知识触手可及</div>
                <br></br>
                <br></br>
                <ElInput v-model="loginEmail" class="login-email" placeholder="请输入邮箱号码"></ElInput>
                <br></br>
                <div style="display: flex; flex-direction: row;">
                    <ElInput v-model="loginVerificationCode" class="login-verification-code" placeholder="请输入验证码">
                    </ElInput>
                    <ElButton @click="sendOTP" type="success" :disabled="countdown > 0">
                        {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                    </ElButton>
                </div>
                <br></br>
                <br></br>
                <ElButton @click="login" class="login-button" type="success">登录</ElButton>
                <br></br>
                <ElLink type="success" href="/login">用密码登录</ElLink>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ElButton, ElInput, ElLink, ElMessage } from 'element-plus'
import { onUnmounted, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const loginEmail = ref("")
const OTP = ref("")
const loginVerificationCode = ref("")
const router = useRouter()
const countdown = ref(0) // 新增倒计时状态
let timer: number | null = null // 定时器变量

async function login() {
    if (loginEmail.value === "") {
        ElMessage.warning("未填写邮箱")
        return
    }
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(loginEmail.value)) {
        ElMessage.warning("邮箱格式不正确")
        return
    }
    if (OTP.value === "") {
        ElMessage.warning("尚未发送验证码")
        return
    }
    if(OTP.value !== loginVerificationCode.value){
        ElMessage.warning("验证码输入错误")
        return
    }
    await axios({
        url: "http://127.0.0.1:8080/LoginByEmail",
        method: "POST",
        data: {
            userEmail: loginEmail.value
        }
    }).then(res => {
        if (res.data.code === 200) {
            localStorage.setItem("id",res.data.data.id)
            localStorage.setItem("uuid",res.data.data.uuid)
            router.push("/home")
        }
        else ElMessage.error(res.data.message);
    }).catch(err => {
        ElMessage.error("网络错误")
        console.log(err)
    })
}
// 发送验证码
async function sendOTP() {
    if (countdown.value > 0) return // 如果正在倒计时，直接返回
    if (loginEmail.value === "") {
        ElMessage.warning("未填写邮箱")
        return
    }
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(loginEmail.value)) {
        ElMessage.warning("邮箱格式不正确")
        return
    }
    try {
        const response = await axios({
            url: "http://127.0.0.1:8080/SendOTP",
            method: "POST",
            data: { userEmail: loginEmail.value }
        })
        if (response.data.code === 200) {
            ElMessage.success("发送成功")
            OTP.value = response.data.data
            // 开始倒计时
            countdown.value = 60
            timer = setInterval(() => {
                countdown.value--
                if (countdown.value <= 0 && timer) {
                    clearInterval(timer)
                    timer = null
                }
            }, 1000)
        } else {
            ElMessage.warning(response.data.message || "未知错误")
        }
    } catch (err) {
        ElMessage.error("网络错误")
        console.error(err)
    }
}
// 组件卸载时清除定时器
onUnmounted(() => {
    if (timer) {
        clearInterval(timer)
    }
})
</script>

<style scoped>
.login-background {
    background-image: url("/src/images/LoginBackground.jpeg");
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    width: 100vw;
    height: 100vh;
    position: fixed;
    top: 0;
    left: 0;
    display: flex;
    justify-content: center;
    align-items: center;
}

.login-place {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: rgb(233, 243, 223);
    width: 425px;
    height: 400px;
    border-radius: 10px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
}

.login-title {
    height: 80%;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    padding: 0 20%;
}

.login-main-title {
    font-size: 25px;
}

.login-introduce {
    font-weight: 300;
    font-size: 14px;
}

.login-email {
    height: 12%;
    width: 80%;
    font-size: 16px;
}

.login-button {
    font-size: 18px;
    height: 12%;
    width: 80%;
}

.login-verification-code {
    margin-left: 45px;
    margin-right: 110px;
    height: 100%;
    width: 30%;
    font-size: 16px;
}
</style>