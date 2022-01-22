<template>
    <div>
        <Search />
        <Footer />
    </div>
</template>

<script>
import Vue from "vue"
import Search from "../components/common/Search.vue"
import Footer from "../components/common/Footer.vue"
import axios from "axios"
// import Sortable from 'sortablejs'

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
})
</script>
