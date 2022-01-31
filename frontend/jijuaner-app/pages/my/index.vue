<template>
    <div class="my">
        <el-card class="user-card-wrapper">
            <div class="user-card">
                <div class="head-wrapper">
                    <van-image round width="80px" height="80px" fit="cover" :src="userInfo.headImg" />
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
        <!-- <el-card>

        </el-card> -->
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
        }
    },
    computed: {
        // user: JSON.parse(localStorage.getItem("user"))
    },
    asyncData({ params }) {},
    methods: {},
    mounted() {
        axios
            .get(`/api/user/userList/getLoggedUserInfo`)
            .then(({ data }) => {
                localStorage.setItem("user", JSON.stringify(data.data))
                console.log(JSON.parse(localStorage.getItem("user")))
            })
            .catch(console.log)

        let user = JSON.parse(localStorage.getItem("user"))
        if (user != null) {
            this.userInfo.userId = user.userId
            this.userInfo.email = user.email
            this.userInfo.userName = user.userName
        }
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
