<template>
    <div>
        <van-nav-bar
            :title="`${fundCode}发表评论`"
            left-arrow
            @click-left="handleReturn"
            right-text="发送"
            @click-right="addComment"
        />
        <van-field
            v-model="content"
            rows="30"
            autosize
            type="textarea"
            maxlength="1000"
            placeholder="请输入评论"
            show-word-limit
        />
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import { handleReturn } from "../../../utils/common"

export default Vue.extend({
    name: "CommentPage",
    data() {
        return {
            fundCode: "",
            content: "",
        }
    },
    async asyncData({ params }) {
        return { fundCode: params.id }
    },
    methods: {
        handleReturn,
        addComment() {
            axios
                .post(`/api/comment/fundComment/addComment`, {
                    fundCode: this.fundCode,
                    content: this.content,
                })
                .then(({ data }) => {
                    this.$notify({ type: "success", message: data.msg, duration: 500 })
                    handleReturn()
                })
                .catch(console.log)
        },
    },
})
</script>

<style lang="less" scoped>

</style>
