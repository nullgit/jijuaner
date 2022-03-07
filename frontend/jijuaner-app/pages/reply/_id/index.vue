<template>
    <div>
        
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import { handleReturn } from "../../../utils/common"

export default Vue.extend({
    name: "Reply",
    components: {},
    data() {
        return {
            fundCode: "",
            comments: [], // id userId  time content fundCode replyNum replyL1 like
            page: 0,
            size: 10,
            replyPopup: false,
        }
    },
    async asyncData({ params }) {
        return { fundCode: params.id }
    },
    methods: {
        handleReturn,
        toComment() {
            this.$router.push(`/comment/${this.fundCode}`)
        },
    },

    async mounted() {
        axios
            .get(`/api/comment/fundComment/getCommentPage?fundCode=${this.fundCode}&page=0&size=${this.size}`)
            .then(({ data }) => {
                this.comments = data.data
                console.log(data.data)
            })
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
</style>
