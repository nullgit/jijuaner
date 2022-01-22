<template>
    <div>
        <el-card>
            <div id="top-cart">
                <div id="head-warper" class="block">
                    <el-avatar :size="50" :src="userInfo.headImg"></el-avatar>
                </div>
                <div id="user-info-warper">
                    <nuxt-link :to="userInfo.userName.length > 0 ? 'setting' : 'signIn'">
                        <div id="user-name">{{ userInfo.userName.length > 0 ? userInfo.userName : "登录/注册" }}</div>
                        <div id="user-info"></div>
                    </nuxt-link>
                </div>
            </div>
            <div id="bottom-cart">bottom-cart</div>
        </el-card>
        <button @click="test">test</button>
        <!-- test -->
        <Footer />
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import Footer from "../../components/common/Footer.vue"
import { config } from "../../utils/config.js"

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
    methods: {
        test() {},
    },
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
#top-cart {
    height: 200px;
    display: flex;
}
</style>
