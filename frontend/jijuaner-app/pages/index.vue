<template>
    <div>
        <Search />
        <Footer activeNavProp="主页"/>
        <el-button @click="test">测试按钮</el-button>
    </div>
</template>

<script>
import Vue from "vue"
import Search from "../components/common/Search.vue"
import Footer from "../components/common/Footer.vue"
import axios from "axios"

export default Vue.extend({
    components: { Search, Footer },
    name: "Home",
    data() {
        return {}
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
    methods: {
        test() {
            this.$notify({ type: "success", message: "通知内容" })
        },
    },
})
</script>
