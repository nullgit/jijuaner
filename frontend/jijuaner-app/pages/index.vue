<template>
    <div>
        <Search />
        <van-swipe class="swipe" :autoplay="3000" lazy-render>
            <van-swipe-item class="swipe-item" v-for="image in images" :key="image">
                <img class="swipe-img" :src="image" alt="" />
            </van-swipe-item>
        </van-swipe>
        <van-grid :column-num="3">
            <van-grid-item v-for="item in icons" :key="item.text" :icon="item.icon" :text="item.text" :to="item.to" />
        </van-grid>

        <Footer activeNavProp="主页" />
    </div>
</template>

<script>
import Vue from "vue"
import Search from "../components/common/Search.vue"
import Footer from "../components/common/Footer.vue"
import axios from "axios"
import {config} from "../utils/config"

export default Vue.extend({
    components: { Search, Footer },
    name: "Home",
    data() {
        return {
            icons: [
                { icon: "diamond-o", text: "指数估值", to: "value" },
                { icon: "chart-trending-o", text: "量化", to: "" },
                { icon: "gift-card-o", text: "投资箴言", to: "" },
                { icon: "gold-coin-o", text: "模拟投资", to: "" },
                { icon: "fire-o", text: "恐贪指数", to: "" },
                { icon: "photo-o", text: "文字", to: "" },
            ],
            images: [
                config.gateway + "/static/home/jijuaner-1.jpg",
                config.gateway + "/static/home/jijuaner-2.jpg",
            ],
        }
    },
    mounted() {
        axios
            .get(`/api/user/userList/getLoggedUserInfo`)
            .then(({ data }) => {
                localStorage.setItem("user", JSON.stringify(data.data))
                console.log(JSON.parse(localStorage.getItem("user")))
            })
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
.swipe {
    margin: 5px;
    .swipe-item {
        height: 150px;
        text-align: center;
        .swipe-img {
            height: 150px;
        }
    }
}
</style>
