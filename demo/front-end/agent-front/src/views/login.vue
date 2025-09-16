<template>
    <div class="login-background">
        <div class="login-place">
            <div class="login-title">
                <strong class="login-main-title">Multi-Agent问答平台</strong>
                <div class="login-introduce">知识问答，让知识触手可及</div>
                <br></br>
                <br></br>
                <ElInput v-model="loginName" class="login-name" placeholder="请输入用户名"></ElInput>
                <br></br>
                <ElInput type="password" show-password v-model="loginPassword" class="login-password" placeholder="请输入密码"></ElInput>
                <br></br>
                <div style="display: flex; flex-direction: row;">
                    <ElCheckbox class="login-remember-me" v-model="rememberMe">记住我</ElCheckbox>
                    <ElLink type="success" href="/LoginByEmail">忘记密码</ElLink>
                </div>
                <br></br>
                <br></br>
                <ElButton @click="login" class="login-button" type="success">登录</ElButton>
                <br></br>
                <div style="font-size: 14px;font-weight: 400;">还没有注册？
                    <ElLink type="success" style="font-size: 14px;" href="/register">
                        点击注册
                    </ElLink>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ElButton, ElCheckbox, ElInput, ElLink, ElMessage } from 'element-plus'
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const loginName = ref(localStorage.getItem("userName") ?? "")
const loginPassword = ref("")
const rememberMe = ref(false)
const router = useRouter()

async function login() {
    if (loginName.value.length < 1 || loginName.value.length > 10) {
        ElMessage.warning("用户名仅支持1-18位")
        return
    }
    if (loginPassword.value.length < 6 || loginPassword.value.length > 18) {
        ElMessage.warning("密码仅支持6-18位")
        return
    }
    await axios({
        url: "http://127.0.0.1:8080/login",
        method: "POST",
        data: {
            userName: loginName.value,
            userPassword: loginPassword.value
        }
    }).then(res => {
        if (res.data.code === 200) {
            if (rememberMe.value === true)
                localStorage.setItem("userName", loginName.value)
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
    height: 500px;
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

.login-name {
    height: 10%;
    width: 80%;
    font-size: 16px;
}

.login-password {
    height: 10%;
    width: 80%;
    font-size: 16px;
}

.login-button {
    font-size: 18px;
    height: 9%;
    width: 80%;
}

.login-remember-me {
    padding-right: 211px;
}
</style>