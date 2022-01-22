<template>
    <div>
        <el-form id="loginForm" ref="loginForm" :model="loginForm" status-icon :rules="rules" label-width="100px">
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="loginForm.email"></el-input>
            </el-form-item>

            <el-form-item label="验证码" prop="verificationCode">
                <el-input v-model="loginForm.verificationCode" autocomplete="off"></el-input>
                <el-button
                    id="sendCodeBtn"
                    :disabled="loginForm.sendCodeBtn.disabled"
                    @click="sendCode"
                    type="primary"
                    >{{ loginForm.sendCodeBtn.msg }}</el-button
                >
            </el-form-item>

            <el-form-item label="密码" prop="password">
                <el-input type="password" v-model="loginForm.password" autocomplete="off"></el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="checkPassword">
                <el-input type="password" v-model="loginForm.checkPassword" autocomplete="off"></el-input>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitForm('loginForm')">提交</el-button>
                <el-button @click="resetForm('loginForm')">重置</el-button>
            </el-form-item>
        </el-form>
        <div>{{ `${loginForm.email}-${loginForm.password}-${loginForm.checkPassword}` }}</div>
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import { JiJuanerException } from "../../utils/JiJuanerException"

export default Vue.extend({
    name: "Login",
    components: {},
    data() {
        var checkEmail = (rule, value, callback) => {
            if (!value) {
                callback(new Error("邮箱不能为空"))
            } else if (!this.testEmail(value)) {
                callback(new Error("邮箱不正确"))
            } else {
                callback()
            }
        }

        var checkCode = (rule, value, callback) => {
            if (!value) {
                callback(new Error("验证码不能为空"))
            } else {
                callback()
            }
        }

        var validatePass = (rule, value, callback) => {
            if (value.lenth < 6) {
                callback(new Error("密码长度必须大于6"))
            } else {
                callback()
            }
        }

        var validatePass2 = (rule, value, callback) => {
            if (value === "") {
                callback(new Error("请再次输入密码"))
            } else if (value !== this.loginForm.password) {
                callback(new Error("两次输入密码不一致!"))
            } else {
                callback()
            }
        }

        return {
            loginForm: {
                email: "",
                verificationCode: "",
                password: "",
                checkPassword: "",
                sendCodeBtn: {
                    disabled: false,
                    msg: "发送验证码",
                },
            },
            rules: {
                email: [{ validator: checkEmail, trigger: "blur" }],
                verificationCode: [{ validator: checkCode, trigger: "blur" }],
                password: [{ validator: validatePass, trigger: "blur" }],
                checkPassword: [{ validator: validatePass2, trigger: "blur" }],
            },
        }
    },
    methods: {
        testEmail(email) {
            return /\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/.test(email)
        },
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    axios
                        .post(`/api/user/userList/login?code=${this.loginForm.verificationCode}`, {
                            email: this.loginForm.email,
                            password: this.loginForm.password,
                            userName: `鸡圈儿用户-${Math.floor(Math.random() * 1000000000)}`,
                        })
                        .then(({ data }) => {
                            alert(data.msg)
                            if (data.code != JiJuanerException.LOGIN_EXCEPTION.code) {
                                location.assign("http://app.jijuaner.com:3000/signIn")
                            }
                        })
                        .catch(console.log)
                } else {
                    console.log("error submit!!")
                    return false
                }
            })
        },
        resetForm(formName) {
            this.$refs[formName].resetFields()
        },
        sendCode() {
            if (this.testEmail(this.loginForm.email)) {
                console.log("要发邮件了！" + this.loginForm.email)
                axios.get(`/api/user/userList/sendCode?email=${this.loginForm.email}`)
                this.loginForm.sendCodeBtn.disabled = true
                let countDownNum = 3
                let timer = setInterval(() => {
                    --countDownNum
                    this.loginForm.sendCodeBtn.msg = `${countDownNum}s后重新发送`
                    if (countDownNum == 0) {
                        clearInterval(timer)
                        this.loginForm.sendCodeBtn.disabled = false
                        this.loginForm.sendCodeBtn.msg = "发送验证码"
                    }
                }, 1000)
            } else {
                alert("邮箱不正确")
            }
        },
    },
})
</script>

<style lang="less" scoped></style>
