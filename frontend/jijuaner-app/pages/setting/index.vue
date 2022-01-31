<template>
    <div>
        <van-nav-bar title="设置" left-arrow @click-left="handleReturn" />

        <van-cell-group inset>
            <van-cell title="重命名" value="原来的名字" />
            <van-cell title="更换邮箱" value="原来的邮箱" />
        </van-cell-group>

        <van-cell-group inset>
            <van-cell title="重新登录" is-link to="signIn"/>
            <van-cell title="退出登录" is-link to="signIn"/>
        </van-cell-group>
    </div>
</template>

<script>
import Vue from "vue"

export default Vue.extend({
    name: "Setting",
    methods: {
        handleReturn() {
            history.back()
        },
    },
})
</script>
