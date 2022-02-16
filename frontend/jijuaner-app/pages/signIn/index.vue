<template>
    <div>
        <van-nav-bar title="登录" left-arrow @click-left="handleReturn" />
        <el-card class="sign-in-card">
            <div class="title">登录鸡圈儿</div>
            <el-form
                id="sign-in-form"
                ref="signInForm"
                :model="signInForm"
                status-icon
                :rules="rules"
                label-width="50px"
            >
                <!-- status-icon属性为输入框添加了表示校验结果的反馈图标。 -->
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="signInForm.email"></el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input type="password" v-model="signInForm.password" autocomplete="off"></el-input>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm('signInForm')">提交</el-button>
                    <el-button @click="resetForm('signInForm')">重置</el-button>
                </el-form-item>
            </el-form>
            <nuxt-link to="login">
                <div class="login">还没有账号，去注册→</div>
            </nuxt-link>
        </el-card>
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import { JiJuanerException } from "../../utils/JiJuanerException"
import { config } from "../../utils/config"
import { handleReturn } from "../../utils/common"

export default Vue.extend({
    name: "SignIn",
    components: {},
    data() {
        var checkEmail = (rule, value, callback) => {
            if (!value) {
                callback(new Error("邮箱不能为空"))
            } else if (!/\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/.test(value)) {
                callback(new Error("邮箱不正确"))
            } else {
                callback()
            }
        }

        var validatePassword = (rule, value, callback) => {
            if (value === "") {
                callback(new Error("请输入密码"))
            } else {
                callback()
            }
        }

        return {
            signInForm: {
                email: "",
                password: "",
            },
            rules: {
                email: [{ validator: checkEmail, trigger: "blur" }],
                password: [{ validator: validatePassword, trigger: "blur" }],
            },
        }
    },
    methods: {
        handleReturn,
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    axios
                        .post("/api/user/userList/signIn", {
                            email: this.signInForm.email,
                            password: this.signInForm.password,
                        })
                        .then(({ data }) => {
                            alert(data.msg)
                            if (data.code != JiJuanerException.SIGN_IN_EXCEPTION.code) {
                                location.assign(config.website)
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
    },
})
</script>

<style lang="less" scoped>
.sign-in-card {
    margin: 5px;

    .title {
        font-size: 20px;
        font-weight: bold;
        text-align: center;
        padding: 10px;
    }

    .login {
        font-size: 14px;
        color: gray;
    }
}
</style>
