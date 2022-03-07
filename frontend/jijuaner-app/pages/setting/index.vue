<template>
    <div>
        <van-nav-bar title="设置" left-arrow @click-left="handleReturn" />

        <van-cell-group inset>
            <van-cell title="我的头像" @click="resetHeadImgDialog">
                <template #right-icon>
                    <img class="head-img" :src="userInfo.headImg" alt="" />
                </template>
            </van-cell>
            <van-cell title="我的名称/重命名" :value="userInfo.userName" @click="renameDialog = !renameDialog" />
            <van-dialog v-model="renameDialog" title="请输入重命名" show-cancel-button :beforeClose="rename">
                <van-field v-model="renameInput" autosize label="重命名" type="textarea" placeholder="最多32个字符" />
            </van-dialog>
            <van-cell title="我的邮箱" :value="userInfo.email" @click="resetEmail"/>
            <van-cell title="重置密码" is-link @click="resetEmail"/>
        </van-cell-group>

        <van-cell-group inset>
            <van-cell title="重新登录" is-link to="signIn" />
            <van-cell title="退出登录" is-link @click="quit" />
        </van-cell-group>
    </div>
</template>

<script>
import Vue from "vue"
import { handleReturn } from "../../utils/common"
import axios from "axios"

export default Vue.extend({
    name: "Setting",
    data() {
        return {
            userInfo: {},
            renameDialog: false,
            renameInput: "",
        }
    },
    methods: {
        handleReturn,
        userNameValidator(userName) {
            userName = userName.trim()
            if (userName == null || userName.length == 0) {
                return "输入不能为空"
            } else if (userName.length > 32) {
                return "最多32个字符"
            }
            return null
        },
        resetHeadImgDialog() {
            this.$dialog.alert({ message: "在“我的”页面点击头像即可更换", theme: "round-button" })
        },
        resetEmail() {
            this.$dialog.alert({ message: "暂不支持更换邮箱，\n如有需要，请联系管理员", theme: "round-button" })
        },
        rename(action, done) {
            if (action == "cancel") {
                done()
                return
            }
            let msg = this.userNameValidator(this.renameInput)
            if (msg != null) {
                this.$notify({ type: "danger", message: msg })
                done()
            } else {
                axios
                    .get(`/api/user/userList/rename?name=${this.renameInput}`)
                    .then(({ data }) => {
                        if (data.code == 0) {
                            this.$notify({ type: "success", message: `你的新名称是: ${this.renameInput}` })
                            this.userInfo.userName = this.renameInput
                            localStorage.setItem("user", JSON.stringify(this.userInfo))
                            done()
                        } else {
                            this.$notify({ type: "danger", message: data.msg })
                            done()
                        }
                    })
                    .catch(console.log)
            }
        },
        quit(){
            axios
                .get(`/api/user/userList/quit`)
                .then(({ data }) => {
                    localStorage.setItem("user", null)
                    this.$router.push(`/my`)
                })
                .catch(console.log)
        },
    },
    mounted() {
        this.userInfo = JSON.parse(localStorage.getItem("user"))
        console.log(this.userInfo)
    },
})
</script>

<style lang="less" scoped>
// van-cell-group {
//     margin: 5px;
// }

.head-img {
    width: 50px;
    height: 50px;
}
</style>
