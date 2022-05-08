<template>
    <div>
        支付成功, 正在跳转到我的页面...
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
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
    // TODO 生成订单页 http://app.jijuaner.vaiwan.com:3000/succuss?charset=utf-8&out_trade_no=1508991159629955074&method=alipay.trade.page.pay.return&total_amount=1.00&sign=OwrdJhNC4zyhlWwz8VwzC4ppgqbnTmL3T46W7m%2B8pud1uRHWhCg%2Fnrt2PGvZ6xYc%2FwPtc4C1ndK%2BcnoPdGRd5H8VoydyQhCLwBy2QJLiGE5QBD7BXAXAVlGWPOiRKvAFivxy3muEzLJ4gZi8%2FrX6ownzy48ZgV013k2XRRrMC6%2F3ltj6QqwkvlqdgN4AhgoqjZzSQltCXnlJXUaTB5NIgJrC%2FzTDnkUalsCznaIwv2fphoksJ1NmpTK1Jg7dO29yWScma0a1IOgKucjkpK%2FmaHzHOcZPCJgGCDcmyVwY9bUQnKmYSJOOkhz3gRNHfPrQWGvdB%2B%2BBTHK6y9OEzdcTQw%3D%3D&trade_no=2022033022001475870501841475&auth_app_id=2021000118648236&version=1.0&app_id=2021000118648236&sign_type=RSA2&seller_id=2088621957105329&timestamp=2022-03-30%2010%3A15%3A53
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
