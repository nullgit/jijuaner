<template>
    <div>
        <van-popup v-model="replyPopup" position="top" :style="{ height: '50%' }" @click.stop="">
            <van-field
                v-model="replyContent"
                rows="10"
                autosize
                type="textarea"
                maxlength="250"
                placeholder="请输入回复"
                show-word-limit
            />
            <el-button class="reply-btn" type="text" @click.stop="reply">发送</el-button>
        </van-popup>

        <div class="comment-card">
            <div class="comment-user">
                <img
                    class="comment-user-head"
                    :src="userInfos[comment.userId] ? userInfos[comment.userId].headImg : ''"
                    alt=""
                />
                <div class="comment-user-info">
                    <div class="comment-user-name">
                        {{ userInfos[comment.userId] ? userInfos[comment.userId].userName : "" }}
                    </div>
                    <div class="comment-time">{{ comment.time | dateFormatter }}</div>
                </div>
            </div>

            <div class="comment-content" @click.stop="toReplys(comment.id)">{{ comment.content }}</div>

            <div class="comment-reply-likes">
                <van-icon
                    name="chat-o"
                    size="20"
                    @click.stop="
                        replyPopup = true
                        toId = comment.id
                        toCommentLevel = commentLevel
                        toUserId = comment.userId
                    "
                />
                {{ commentLevel == 0 ? comment.replyNum : comment.someReply.length }}
                <van-icon
                    :name="comment.hasLiked ? 'good-job' : 'good-job-o'"
                    size="20"
                    @click.stop="toggleLikes(comment.id)"
                />
                {{ comment.likes }}
            </div>
        </div>

        <div class="comment-some-reply">
            <div
                class="comment-reply"
                v-for="reply of comment.someReply"
                :key="reply.id"
                @click.stop="
                    replyPopup = true
                    toCommentLevel = commentLevel + 1
                    toId = commentLevel == 0 ? reply.id : comment.id
                    toUserId = reply.userId
                "
            >
                <span class="reply-user-name">
                    {{ userInfos[reply.userId] ? userInfos[reply.userId].userName : "" }}
                </span>
                <span v-if="commentLevel == 1" class="reply-to-user-name">
                    @ {{ userInfos[reply.toUserId] ? userInfos[reply.toUserId].userName : "" }}
                </span>
                <span class="reply-content">
                    {{ reply.content }}
                </span>
            </div>

            <div
                class="more-reply"
                v-if="commentLevel == 0 && comment.someReply && comment.someReply.length >= 1"
                @click.stop="toReplys(comment.id)"
            >
                更多回复 >
            </div>
        </div>
    </div>
</template>

<script>
import { dateFormatter } from "../utils/common"
import axios from "axios"

export default {
    name: "Comment",
    props: ["comment", "userInfos", "commentLevel"],
    filters: { dateFormatter },
    data() {
        return {
            replyPopup: false,
            replyContent: "",
            toId: "",
            toUserId: -1,
            toCommentLevel: 0,
        }
    },
    methods: {
        toReplys(id) {
            if (this.commentLevel == 0) {
                this.$router.push(`/replys/${id}`)
            }
        },
        toggleLikes(id) {
            axios
                .get(`/api/comment/fundComment/toggleLikes?commentId=${id}&commentLevel=${this.commentLevel}`)
                .then(({ data }) => {
                    this.comment.likes += this.comment.hasLiked ? -1 : 1
                    this.comment.hasLiked = !this.comment.hasLiked
                })
                .catch(console.log)
        },
        reply() {
            let p
            if (this.toCommentLevel == 0) {
                p = axios.post(`/api/comment/fundComment/replyComment`, {
                    toCommentId: this.toId,
                    content: this.replyContent,
                })
            } else {
                p = axios.post(`/api/comment/fundComment/replyToReply`, {
                    toReplyId: this.toId,
                    toUserId: this.toUserId,
                    content: this.replyContent,
                })
            }
            p.then(({ data }) => {
                this.$notify({ type: "success", message: data.msg, duration: 500 })
                this.$router.go(0)
            }).catch(console.log)
        },
    },
}
</script>

<style lang="less" scoped>
.comment-card {
    display: flex;
    flex-direction: column;
    .comment-user {
        display: flex;
        flex-direction: row;
        .comment-user-head {
            width: 40px;
            height: 40px;
        }
        .comment-user-info {
            padding-left: 10px;
            .comment-user-name {
                font-weight: bold;
                font-size: 18px;
                padding-bottom: 3px;
            }
            .comment-time {
                color: gray;
                font-size: 12px;
            }
        }
    }

    .comment-content {
        margin: 5px;
        word-break: break-word;
    }

    .comment-reply-likes {
        display: inline;
        align-self: flex-end;
        float: right;
    }
}

.comment-some-reply {
    background: #f9f9f9;
    margin: 5px;
    line-height: 20px;

    .comment-reply {
        margin: 5px;

        .reply-user-name,
        .reply-to-user-name {
            font-weight: bold;
        }
        .reply-content {
            // white-space: nowrap;
            // overflow: hidden;
            // text-overflow: ellipsis;
        }
    }

    .more-reply {
        margin: 5px;
        color: gray;
    }
}
</style>
