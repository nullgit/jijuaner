<template>
    <div>
        <van-nav-bar title="买入" left-arrow @click-left="handleReturn" fixed placeholder />
        <div class="fund-name">{{ payFundInfo.fundName }}</div>
        <div class="status">申购状态: {{ payFundInfo.subscriptionStatus }} {{ payFundInfo.redemptionStatus }}</div>
        交易规则
        <van-field
            v-model="amount"
            type="digit"
            label="买入金额"
            :placeholder="`最低买入${payFundInfo.minAmount}元, 当日最多买入${payFundInfo.maxAmountPerDay}元`"
        />
        10元 100元 1000元 10000元
        <div class="service-charge">
            买入费率: {{ payFundInfo.serviceCharge }}%
            预计x月x日(星期x)以x月x日(星期x)净值确认份额(关于销售服务费,赎回等详见交易规则)
        </div>
        付款方式

        <div class="license">
            基金销售服务由xx公司提供<br />
            基金销售资格: 证监许可[xxxx]xxx号
        </div>
        点击确定即代表您已知晓该基金的产品概要和投资人权益须知等相关内容
        <button>确定</button>
        token: {{payFundInfo.token}}
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
// import dayjs from "dayjs"
import { handleReturn } from "../../../utils/common"

export default Vue.extend({
    name: "Pay",
    data() {
        return {
            fundCode: "",
            payFundInfo: {
                fundCode: "",
                fundName: "",
                fundType: "",
                subscriptionStatus: "",
                redemptionStatus: "",
                nextOpenDay: "",
                minAmount: 0,
                maxAmountPerDay: 0,
                serviceCharge: 0.0,
                token: "",
            },
            amount: "",
        }
    },
    computed: {},
    methods: {
        handleReturn,
    },
    async asyncData({ params }) {
        return { fundCode: params.id }
    },
    async mounted() {
        axios
            .get(`/api/pay/fundInfo/${this.fundCode}`)
            .then(({ data }) => (this.payFundInfo = data.data))
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
</style>
