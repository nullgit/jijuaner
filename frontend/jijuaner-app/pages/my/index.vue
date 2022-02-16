<template>
    <div class="my">
        <el-card class="user-card-wrapper">
            <div class="user-card">
                <div class="head-wrapper">
                    <el-upload
                        class="head-uploader"
                        :data="oss"
                        action="https://jijuaner-oss.oss-cn-shanghai.aliyuncs.com"
                        :before-upload="getOssPolicy"
                        :show-file-list="false"
                        :on-success="uploadHeadImgSuccess"
                    >
                        <img class="head-img" v-if="userInfo.headImg" :src="userInfo.headImg" alt="" />
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    </el-upload>
                </div>

                <div class="user-info-wrapper">
                    <div class="user-name">
                        <nuxt-link :to="userInfo.userName.length > 0 ? 'setting' : 'signIn'">
                            <div class="user-name">
                                {{ userInfo.userName.length > 0 ? userInfo.userName : "登录/注册" }}
                            </div>
                        </nuxt-link>
                    </div>
                    <div class="email">邮箱：{{ userInfo.email.length > 0 ? userInfo.email : "" }}</div>
                </div>
            </div>
        </el-card>

        <van-cell-group inset>
            <van-cell icon="setting-o" title="设置" is-link to="setting" />
            <!-- <van-cell title="单元格" value="内容" label="描述信息" /> -->
        </van-cell-group>
        <Footer activeNavProp="我的" />
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import Footer from "../../components/common/Footer.vue"
import {nanoid} from 'nanoid'

export default Vue.extend({
    name: "My",
    components: {
        Footer,
    },
    data() {
        return {
            userInfo: {
                headImg: "/img/defaultHeadImg.png",
                userId: -1,
                email: "",
                userName: "",
            },
            oss: {
                policy: "",
                signature: "",
                key: "",
                ossaccessKeyId: "",
                dir: "",
                host: "",
            },
        }
    },
    computed: {},
    asyncData({ params }) {},
    methods: {
        async getOssPolicy(file) {
            await axios
                .get(`/api/user/userList/getOssPolicy`)
                .then(({ data }) => {
                    this.oss.policy = data.data.policy
                    this.oss.signature = data.data.signature
                    this.oss.ossaccessKeyId = data.data.accessid
                    this.oss.key = data.data.dir + nanoid() + "_" + file.name
                    this.oss.dir = data.data.dir
                    this.oss.host = data.data.host
                })
                .catch(console.log)
        },
        uploadHeadImgSuccess({file}) {
            let headImg = `${this.oss.host}/${this.oss.key}`
            axios
                .get(`/api/user/userList/setHeadImg?headImg=${headImg}`)
                .then(({ data }) => {
                    this.$notify({ type: "success", message: "头像上传成功", duration: 500 })
                    this.userInfo.headImg = headImg
                    localStorage.setItem("user", JSON.stringify(this.userInfo))
                })
                .catch(console.log)
        },
    },
    mounted() {
        axios
            .get(`/api/user/userList/getLoggedUserInfo`)
            .then(({ data }) => {
                localStorage.setItem("user", JSON.stringify(data.data))
            })
            .catch(console.log)

        let user = JSON.parse(localStorage.getItem("user"))
        if (user != null) {
            this.userInfo = user
        }
        console.log(this.userInfo)
    },
})
</script>

<style lang="less" scoped>
.user-card-wrapper {
    margin: 5px;

    .user-card {
        width: 100%;
        height: 100px;
        display: flex;
        justify-items: flex-start;
        align-items: center;

        .head-wrapper {
            width: 100px;

            .head-uploader {
                width: 80px;
                height: 80px;
                .head-img {
                    width: 80px;
                    height: 80px;
                }
            }
        }

        .user-info-wrapper {
            width: 300px;
            height: 50px;
            display: flex;
            flex-direction: column;
            justify-content: space-evenly;

            .user-name {
                color: black;
                font-size: 20px;
                font-weight: bold;
            }

            .email {
                color: black;
            }
        }
    }
}
</style>
