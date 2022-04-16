<template>
    <div class="pay-page">
        <van-nav-bar title="买入" left-arrow @click-left="handleReturn" fixed placeholder />

        <div class="fund-info">
            <div class="fund-name">{{ payFundInfo.fundName }}</div>
            <div class="status">申购状态: {{ payFundInfo.subscriptionStatus }} {{ payFundInfo.redemptionStatus }}</div>
        </div>

        <div class="safe"><van-icon name="certificate" />资金安全有保障 ></div>

        <div class="buy-title-wrapper">
            <div class="buy-title">买入金额 ￥</div>
            <div class="trading-rules">交易规则</div>
        </div>
        <van-field
            class="amount"
            v-model="amount"
            type="digit"
            label=""
            :placeholder="`最低买入${payFundInfo.minAmount}元, 当日最多买入${payFundInfo.maxAmountPerDay}元`"
        />

        <div class="option">
            <van-button
                class="option-button"
                v-for="option of ['100元', '1000元', '10000元']"
                :key="option"
                color="#409eff"
                size="small"
                plain
                >{{ option }}</van-button
            >
        </div>

        <div class="service-charge">
            买入费率: {{ payFundInfo.serviceCharge }}%
            预计x月x日(星期x)以x月x日(星期x)净值确认份额(关于销售服务费,赎回等详见交易规则)
        </div>

        <div class="pay-way">
            <div class="pay-way-title">付款方式</div>
            <div class="alipay">支付宝</div>
        </div>

        <div class="license">
            基金销售服务由xx公司提供<br />
            基金销售资格: 证监许可[xxxx]xxx号
        </div>

        <div class="bottom">
            <div class="disclaimer">点击确定即代表您已知晓该基金的产品概要和投资人权益须知等相关内容</div>
            <a
                class="confirm"
                :href="
                    `http://gateway.jijuaner.vaiwan.com/api/pay/transaction/subscribe` +
                    `?fundCode=${this.payFundInfo.fundCode}&amount=${this.amount}&token=${this.payFundInfo.token}`
                "
            >
                <van-button color="#409eff" block>确定</van-button>
            </a>
        </div>
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
            payUrl: "",
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
            .get(`/api/pay/fund/info/${this.fundCode}`)
            .then(({ data }) => (this.payFundInfo = data.data))
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
@color: #409eff;
@bg-color: #ebf3ff;

.pay-page {
    // background-color: gray;
    // height: 100%;
    // height: calc(~"100% - 50px");
}

.fund-info {
    margin: 10px;

    .fund-name {
        font-size: 22px;
        font-weight: 700;
        margin: 10px;
    }

    .status {
        margin-left: 10px;
    }
}

.safe {
    line-height: 30px;
    background-color: @bg-color;
    color: @color;
    font-weight: 600;
    text-align: center;
    margin: 10px 0;
    margin-top: 20px;
}

.buy-title-wrapper {
    margin: 20px;
    .buy-title {
        font-size: 18px;
        float: left;
        font-weight: 700;
    }
    .trading-rules {
        float: right;
    }
}

.amount {
    display: inline-block;
    width: 300px;
    margin: 10px;
    font-size: 16px;
}

.option {
    display: flex;
    justify-content: space-around;
    width: 300px;
    margin: 10px;
    .option-button {
        height: 25px;
        background-color: @bg-color;
        color: @color;
    }

}

.service-charge {
    border-top: 1px average(white, gray) solid;
    // font-size: 14px;
    // font-weight: 700;
    line-height: 25px;
    color: gray;
    padding: 10px 0;
    margin: 20px 10px;
}

.pay-way {
    display: flex;
    justify-content: space-between;
    margin: 20px;
    .pay-way-title {
        // float: left;
        font-size: 16px;
        font-weight: 700;
    }
    .alipay {
        font-size: 14px;
        color: gray;
    }
}

.license {
    font-size: 14px;
    color: gray;
    text-align: center;
    line-height: 20px;
}

.bottom {
    // background-color: gray;
    position: fixed;
    bottom: 0;
    margin: 10px;
    .disclaimer {
        margin: 10px 5px;
        line-height: 20px;
        font-size: 14px;
        color: grey;
    }
    .confirm {
        display: block;
    }
}
</style>
