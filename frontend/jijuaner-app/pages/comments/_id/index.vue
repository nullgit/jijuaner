<template>
    <div>
        <van-nav-bar :title="`评论区`" left-arrow @click-left="handleReturn" fixed placeholder />

        <el-card class="fund-info-card-wrapper">
            <div class="fund-info-card">{{ fundInfo.fundName }}</div>
        </el-card>

        <div class="no-comment" v-if="comments.length == 0">还没有评论哦</div>
        <van-list
            :loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
            offset="20"
        >
            <el-card class="comment-card-wrapper" v-for="comment of comments" :key="comment.id">
                <Comment :comment="comment" :userInfos="userInfos" commentLevel="0" />
            </el-card>
        </van-list>

        <div class="to-comment-wrapper" @click="toComment">
            <el-input placeholder="快来评论吧" class="to-comment" prefix-icon="el-icon-s-comment"></el-input>
        </div>
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import { handleReturn, getUserInfos } from "../../../utils/common"
import Comment from "../../../components/Comment.vue"

export default Vue.extend({
    name: "Comments",
    components: { Comment },
    data() {
        return {
            fundCode: "",
            fundInfo: {},
            comments: [], // id userId  time content fundCode replyNum replyL1 likes
            page: 0,
            size: 10,
            replyPopup: false,
            userInfos: {},
            loading: false,
            finished: false,
        }
    },
    filters: {
        dateFormatter(time) {
            return dayjs(time).format("YYYY年MM月DD日 HH:mm:ss")
        },
    },
    asyncData({ params }) {
        return { fundCode: params.id }
    },
    methods: {
        handleReturn,
        toComment() {
            this.$router.push(`/comment/${this.fundCode}`)
        },
        getCommentPage() {
            axios
                .get(
                    `/api/comment/fundComment/getCommentPage?` +
                        `id=${this.fundCode}&page=${this.page++}&size=${this.size}`
                )
                .then(({ data }) => {
                    if (data.data.length == 0) {
                        console.log("finished")
                        this.finished = true
                    } else {
                        let ids = [...new Set(data.data.map((comment) => comment.userId))].filter(
                            (id) => !this.userInfos.hasOwnProperty(id)
                        )
                        getUserInfos(Vue, this.userInfos, ids)
                        this.comments.push(...data.data)
                        console.log(data.data)
                    }
                })
                .catch(console.log)
        },
        onLoad() {
            console.log("onLoad")
            this.loading = true
            this.getCommentPage()
            this.loading = false
        },
    },

    mounted() {
        axios
            .get(`/api/fund/fundInfo/simple/${this.fundCode}`)
            .then(({ data }) => {
                this.fundInfo = data.data
            })
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
.no-comment {
    text-align: center;
    margin: 20px;
}

.comment-card-wrapper,
.fund-info-card-wrapper {
    margin: 5px;
}

.to-comment-wrapper {
    position: fixed;
    bottom: 0;
    width: 100%;
    .to-comment {
        width: 95%;
        margin: 5px;
    }
}
</style>
