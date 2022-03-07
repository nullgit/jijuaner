<template>
    <div>
        <van-nav-bar :title="`评论回复区`" left-arrow @click-left="handleReturn" />
        <el-card class="comment-card-wrapper">
            <Comment :comment="comment" :userInfos="userInfos" :commentLevel="0" />
        </el-card>

        <van-popup v-model="replyPopup" position="top" :style="{ height: '50%' }">
            <van-field
                v-model="replyContent"
                rows="10"
                autosize
                type="textarea"
                maxlength="250"
                placeholder="请输入回复"
                show-word-limit
            />
            <el-button class="reply-btn" type="text" @click="reply">发送</el-button>
        </van-popup>

        <van-nav-bar title="全部评论" />
        <van-list :loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad" offset="50">
            <el-card @click="test" class="reply-card" v-for="reply, index in replys" :key="index">
                <Comment :comment="reply" :userInfos="userInfos" :commentLevel="1" />
            </el-card>
        </van-list>
        <van-tabbar>
            <van-tabbar-item icon="comment-o" @click="replyPopup = true">回复该评论</van-tabbar-item>
        </van-tabbar>

    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import { handleReturn, getUserInfos } from "../../../utils/common"
import Comment from "../../../components/Comment.vue"

export default Vue.extend({
    name: "Replys",
    components: {Comment},
    data() {
        return {
            toCommentId: "",
            comment: {},
            replys: [], // id userId toCommentId time content likes replyL2
            userInfos: {},
            page: 0,
            size: 10,
            replyPopup: false,
            replyContent: "",
            loading: false,
            finished: false,
        }
    },
    asyncData({ params }) {
        return { toCommentId: params.id }
    },
    filters: {
        dateFormatter(time) {
            return dayjs(time).format("YYYY年MM月DD日 HH:mm:ss")
        },
    },
    methods: {
        test() {
            console.log('!!!')
        },
        handleReturn,
        getComment() {
            axios
                .get(`/api/comment/fundComment/getComment/${this.toCommentId}`)
                .then(({ data }) => {
                    this.comment = data.data
                    getUserInfos(Vue, this.userInfos, [this.comment.userId])
                })
                .catch(console.log)
        },
        getReplyPage() {
            this.loading = true
            axios
                .get(
                    `/api/comment/fundComment/getReplyL1Page?` +
                        `id=${this.toCommentId}&page=${this.page++}&size=${this.size}`
                )
                .then(({ data }) => {
                    if (data.data.length == 0) {
                        this.finished = true
                        console.log("finished!!!")
                    } else {
                        let idSet = new Set()
                        for (let reply of data.data) {
                            idSet.add(reply.userId)
                            for (let replyL2 of reply.someReply) {
                                idSet.add(replyL2.userId)
                            }
                        }
                        let ids = [...idSet].filter(
                            (id) => !this.userInfos.hasOwnProperty(id)
                        )
                        getUserInfos(Vue, this.userInfos, ids)
                        this.replys.push(...data.data)
                        console.log(data.data)
                        this.loading = false
                    }
                })
                .catch(console.log)
        },

        reply() {
            axios
                .post(`/api/comment/fundComment/replyComment`, {
                    toCommentId: this.toCommentId,
                    content: this.replyContent,
                })
                .then(({ data }) => {
                    this.$notify({ type: "success", message: data.msg, duration: 500 })
                    this.replyPopup = false
                    this.page = 0
                    this.replys = []
                    this.getReplyPage()
                })
                .catch(console.log)
        },
        onLoad() {
            console.log("onLoad")
            this.getReplyPage()
        },
    },

    mounted() {
        this.getComment()
        this.getReplyPage()
    },
})
</script>

<style lang="less" scoped>
.reply-btn {
    margin-left: 10px;
}

.comment-card-wrapper {
    margin: 5px;
}
</style>
