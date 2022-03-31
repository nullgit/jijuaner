<template>
    <div>
        支付成功, 正在跳转到我的页面...
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
// import dayjs from "dayjs"
import { handleReturn } from "../../utils/common"

export default Vue.extend({
    name: "PaySuccess",
    data() {
        return {
        }
    },
    methods: {
        handleReturn,
    },
    async asyncData({ params }) {
    },
    async mounted() {
        setTimeout(() => {
            this.$router.push(`/my`)
        },1000)
    },
})
</script>

<style lang="less" scoped>
</style>
