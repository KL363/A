<template>
    <div class="register-background">
        <div class="register-place">
            <div class="register-title">
                <strong class="register-main-title">Multi-Agent问答平台</strong>
                <div class="register-introduce">知识问答，让知识触手可及</div>
                <br></br>
                <br></br>
                <ElInput v-model="registerName" class="register-name" placeholder="请输入用户名"></ElInput>
                <br></br>
                <ElInput type="password" show-password v-model="registerPassword" class="register-password"
                    placeholder="请输入密码"></ElInput>
                <br></br>
                <ElInput type="password" show-password v-model="registerPasswordSecond" class="register-password-second"
                    placeholder="请再次输入密码">
                </ElInput>
                <br></br>
                <ElInput v-model="registerEmail" class="register-email" placeholder="请输入邮箱号码"></ElInput>
                <br></br>
                <div style="display: flex; flex-direction: row;">
                    <ElInput v-model="registerVerificationCode" class="register-verification-code" placeholder="请输入验证码">
                    </ElInput>
                    <ElButton @click="sendOTP" type="success" :disabled="countdown > 0">
                        {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                    </ElButton>
                </div>
                <br></br>
                <br></br>
                <ElButton @click="register" class="register-button" type="success">注册</ElButton>
                <br></br>
                <div style="font-size: 14px;font-weight: 400;">已经注册？
                    <ElLink type="success" style="font-size: 14px;" href="/login">
                        点击登录
                    </ElLink>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ElButton, ElInput, ElLink, ElMessage } from 'element-plus'
import { onUnmounted, ref } from 'vue'
import axios from 'axios'
import router from '@/router'

const registerName = ref("")
const registerPassword = ref("")
const registerPasswordSecond = ref("")
const registerEmail = ref("")
const registerVerificationCode = ref("")
const OTP = ref("")
const countdown = ref(0) // 新增倒计时状态
let timer: number | null = null // 定时器变量

// 发送验证码
async function sendOTP() {
    if (registerName.value.length < 1 || registerName.value.length > 18) {
        ElMessage.warning("用户名仅支持1-18位")
        return
    }
    if (countdown.value > 0) return // 如果正在倒计时，直接返回

    if (registerEmail.value === "") {
        ElMessage.warning("未填写邮箱")
        return
    }

    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(registerEmail.value)) {
        ElMessage.warning("邮箱格式不正确")
        return
    }

    try {
        const response = await axios({
            url: "http://127.0.0.1:8080/SendOTP",
            method: "POST",
            data: { 
                userEmail: registerEmail.value,
                userName: registerName.value
            }
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
async function register() {
    if (registerName.value.length < 1 || registerName.value.length > 10) {
        ElMessage.warning("用户名仅支持1-10位")
        return
    }
    if (registerPassword.value.length < 6 || registerPassword.value.length > 18) {
        ElMessage.warning("密码仅支持6-18位")
        return
    }
    if (registerPassword.value !== registerPasswordSecond.value) {
        ElMessage.warning("前后输入两次密码不同")
        return
    }
    if (registerEmail.value === "") {
        ElMessage.warning("尚未输入邮箱号码")
        return
    }
    if (registerEmail.value.length > 45) {
        ElMessage.warning("可供输入的邮箱号码长度最多为45")
        return
    }
    if (OTP.value === "") {
        ElMessage.warning("尚未发送验证码")
        return
    }
    if (registerVerificationCode.value === "" || registerVerificationCode.value !== OTP.value) {
        ElMessage.warning("验证码输入有误")
        return
    }
    await axios({
        url: "http://127.0.0.1:8080/register",
        method: "POST",
        data: {
            userName: registerName.value,
            userPassword: registerPassword.value,
            userEmail: registerEmail.value
        }
    }).then(res => {
        if (res.data.code === 200) {
            ElMessage.success("注册成功")
            router.push("/login")
        }
        else ElMessage.error(res.data.message || "注册失败")
    }).catch(e => {
        ElMessage.error("网络错误")
        console.log(e)
    })
}
// 组件卸载时清除定时器
onUnmounted(() => {
    if (timer) {
        clearInterval(timer)
    }
})
</script>

<style scoped>
.register-background {
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

.register-place {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: rgb(233, 243, 223);
    width: 500px;
    height: 630px;
    border-radius: 10px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
}

.register-title {
    height: 80%;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    padding: 0 20%;
}

.register-main-title {
    font-size: 25px;
}

.register-introduce {
    font-weight: 300;
    font-size: 14px;
}

.register-name {
    height: 7%;
    width: 80%;
    font-size: 16px;
}

.register-password {
    height: 7%;
    width: 80%;
    font-size: 16px;
}

.register-password-second {
    height: 7%;
    width: 80%;
    font-size: 16px;
}

.register-email {
    height: 7%;
    width: 80%;
    font-size: 16px;
}

.register-verification-code {
    margin-left: 52px;
    margin-right: 145px;
    height: 100%;
    width: 30%;
    font-size: 16px;
}

.register-button {
    font-size: 18px;
    height: 7%;
    width: 80%;
}

.register-remember-me {
    padding-right: 211px;
}
</style>