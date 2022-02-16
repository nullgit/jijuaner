<template>
    <div>
        <van-nav-bar :title="fundInfo.fundName" left-arrow @click-left="handleReturn" />

        <el-card class="title-wrapper">
            <div class="title-top-wrapper">
                <div class="fund-name">{{ fundInfo.fundName }}</div>
                <div class="fund-code">{{ fundInfo.fundCode }}</div>
                <div class="fund-type">{{ fundInfo.fundType }}</div>
            </div>

            <ul class="title-bottom-wrapper">
                <li class="yield-one-day">
                    <div class="num">{{ yieldOneDay.toFixed(2) }}%</div>
                    <div class="msg">日涨跌幅</div>
                </li>
                <li class="yield-one-year">
                    <div class="num">
                        {{ fundInfo.yieldOneYear ? `${fundInfo.yieldOneYear}%` : "该基金成立不足一年" }}
                    </div>
                    <div class="msg">近一年收益率</div>
                </li>
                <li class="net-worth">
                    <div class="num">{{ netWorth }}</div>
                    <div class="msg">最新净值</div>
                </li>
            </ul>
        </el-card>

        <el-card class="chart-wrapper">
            <el-tabs v-model="navName" @tab-click="handleClickNav">
                <el-tab-pane label="收益详情" name="yieldDetail">
                    <div class="yield-chart" id="yield-chart"></div>

                    <el-tabs
                        class="yield-nav"
                        v-model="yieldNavName"
                        tab-position="bottom"
                        @tab-click="handleClickYieldNav"
                    >
                        <el-tab-pane label="今年来" name="thisYear"></el-tab-pane>
                        <el-tab-pane label="近1月" name="nearOneMonth"></el-tab-pane>
                        <el-tab-pane label="近1年" name="nearOneYear"></el-tab-pane>
                        <el-tab-pane label="近3年" name="nearThreeYear"></el-tab-pane>
                        <el-tab-pane label="成立以来" name="sinceEstablishment"></el-tab-pane>
                    </el-tabs>
                </el-tab-pane>

                <el-tab-pane label="实时估值" name="realTimeValuation">
                    <div class="valuation-chart" id="valuation-chart"></div>
                </el-tab-pane>
            </el-tabs>
        </el-card>

        <div class="detail-wrapper"></div>

        <el-card class="managers">
            <div class="managers-title">基金经理</div>
            <div v-for="manager of fundInfo.currentManagers" :key="manager.managerId" class="manager">
                <div class="manager-info">
                    <div class="manager-name">{{ manager.name }}</div>
                    <div class="manager-work-time">从业{{ manager.workTime }}</div>
                    <div class="manager-fund-size">管理基金{{ manager.fundSize }}</div>
                </div>
                <img :src="manager.pic" alt="" class="manager-img" />
            </div>
        </el-card>

        <van-grid class="buttom-bar" :column-num="2">
            <van-grid-item icon="comment-o" />
            <van-grid-item v-if="isOptional" @click="cancelOption" icon="star" />
            <van-grid-item v-else @click="addOption" icon="star-o" />
        </van-grid>
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import { handleReturn } from "../../../utils/common"
import { config } from "../../../utils/config"

export default Vue.extend({
    name: "Info",
    components: {
        // Search
    },
    data() {
        return {
            fundCode: "",
            fundInfo: {
                fundCode: "",
                fundName: "",
                fundType: "",
                yieldOneYear: 0,
                yieldSixMonths: 0,
                yieldThreeMonths: 0,
                yieldOneMonth: 0,
                acWorthTrend: [],
                currentManagers: [],
            },
            navName: "yieldDetail",
            yieldNavName: "sinceEstablishment",
            yieldChart: null,
            establishmentDay: null,
            xs: [],
            ys: [],
            isOptional: false,
        }
    },
    computed: {
        yieldOneDay() {
            if (this.validateAcWorthTrend(2)) {
                return (
                    ((this.fundInfo.acWorthTrend[0].y - this.fundInfo.acWorthTrend[1].y) /
                        this.fundInfo.acWorthTrend[1].y) *
                    100
                )
            }
            return 0.0
        },
        netWorth() {
            // TODO 单位净值而不是累计净值
            if (this.validateAcWorthTrend(1)) {
                return this.fundInfo.acWorthTrend[0].y
            }
            return 0.0
        },
        dataDate() {
            if (this.validateAcWorthTrend(1)) {
                return dateToStr(new Date(this.fundInfo.acWorthTrend[0].x))
            }
            return ""
        },
    },
    async asyncData({ params }) {
        return { fundCode: params.id }
        // return axios
        //     .get(`${config.gateway}/api/fund/fundInfo/${params.id}`, {
        //         headers: {"Access-Control-Allow-Origin":"*",}
        //     })
        //     .then(({ data }) => {
        //         return { fundInfo: { ...data.data } }
        //     })
        //     .catch(console.log)
    },
    methods: {
        handleReturn,
        getOptionalStatus() {
            axios
                .get(`/api/user/userOption/isOptional?fundCode=${this.fundInfo.fundCode}`)
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.isOptional = data.data
                    }
                })
                .catch(console.log)
        },
        addOption() {
            axios
                .post(`/api/user/userOption/addNewFunds`, {
                    funds: [this.fundInfo.fundCode],
                })
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.isOptional = true
                        this.$notify({ type: "success", message: "添加自选成功", duration: 500 })
                    }
                })
                .catch(console.log)
        },
        cancelOption() {
            axios
                .post(`/api/user/userOption/delFunds`, {
                    funds: [this.fundInfo.fundCode],
                })
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.isOptional = false
                        this.$notify({ type: "success", message: "取消自选成功", duration: 500 })
                    }
                })
                .catch(console.log)
        },
        echartsInit() {
            // 在这里 acWorthTrend 是正序的，使用完后 acWorthTrend 会变成逆序的（时间从近到远）
            let acWorthTrend = this.fundInfo.acWorthTrend
            let yieldChart = this.$echarts.init(document.getElementById("yield-chart"))
            this.yieldChart = yieldChart
            this.establishmentDay = dayjs()
            this.days = []
            this.netWorths = []
            this.xs = []
            this.ys = []
            if (this.validateAcWorthTrend(1)) {
                this.establishmentDay = dayjs(acWorthTrend[0].x)
                this.days.push(this.establishmentDay)
                let baseNetWorth = acWorthTrend[0].y
                this.netWorths.push(baseNetWorth)
                this.xs.push(this.establishmentDay.format("YYYY-MM-DD"))
                this.ys.push(0)
                for (let i = 1; i < acWorthTrend.length; ++i) {
                    let netWorth = acWorthTrend[i]
                    let day = dayjs(netWorth.x)
                    this.days.push(day)
                    this.netWorths.push(netWorth.y)
                    this.xs.push(day.format("YYYY-MM-DD"))
                    this.ys.push(((netWorth.y - baseNetWorth) / baseNetWorth) * 100)
                }
            }
            yieldChart.setOption({
                xAxis: {
                    type: "category",
                    data: this.xs,
                },
                yAxis: {
                    type: "value",
                },
                series: [
                    {
                        data: this.ys,
                        type: "line",
                    },
                ],
            })
        },
        validateAcWorthTrend(len) {
            return this.fundInfo.acWorthTrend != null && this.fundInfo.acWorthTrend.length >= len
        },
        handleClickNav(tab, event) {
            if (tab.name == "yieldDetail") {
                // this.echartsInit()
            }
            console.log(tab, event)
        },
        handleClickYieldNav(tab, event) {
            console.log(tab.name)
            let xs = []
            let ys = []
            let baseDay = dayjs()
            if (tab.name == "thisYear") {
                baseDay = baseDay.set("month", 0).set("date", 1)
            } else if (tab.name == "nearOneMonth") {
                baseDay = baseDay.subtract(1, "month")
            } else if (tab.name == "nearOneYear") {
                baseDay = baseDay.subtract(1, "year")
            } else if (tab.name == "nearThreeYear") {
                baseDay = baseDay.subtract(3, "year")
            } else if (tab.name == "sinceEstablishment") {
                baseDay = baseDay.subtract(10000, "year")
            }
            if (baseDay.isBefore(this.establishmentDay)) {
                xs = this.xs
                ys = this.ys
            } else {
                let i
                let baseNetWorth
                for (i = this.days.length - 1; i >= 0; --i) {
                    if (baseDay.isAfter(this.days[i])) {
                        baseNetWorth = this.netWorths[i]
                        break
                    }
                }
                xs = this.xs.slice(i)
                for (; i < this.netWorths.length; ++i) {
                    ys.push(((this.netWorths[i] - baseNetWorth) / baseNetWorth) * 100)
                }
            }

            this.yieldChart.setOption({ xAxis: { data: xs }, series: [{ data: ys }] })
        },
    },

    async mounted() {
        axios
            .get(`/api/fund/fundInfo/${this.fundCode}`)
            .then(({ data }) => {
                this.fundInfo = { ...data.data }
                console.log(this.fundInfo)
                this.getOptionalStatus()
                this.echartsInit()
                this.fundInfo.acWorthTrend.reverse()
            })
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
van-nav-bar {
    position: fixed;
    top: 0;
}

.title-wrapper {
    margin: 5px;
    .title-top-wrapper {
        .fund-name {
            font-weight: bold;
            font-size: 18px;
        }

        .fund-code {
            font-size: 14px;
            color: gray;
            display: inline-block;
        }

        .fund-type {
            display: inline-block;
            font-size: 12px;
            color: blue;
            background-color: #409eff;
        }
    }

    .title-bottom-wrapper {
        display: flex;
        justify-content: space-evenly;
        align-items: center;

        li {
            width: 100px;
            display: flex;
            flex-direction: column;
            .msg {
                font-size: 14px;
                color: gray;
            }
        }

        .yield-one-day {
            .num {
                color: red;
                font-weight: bold;
                font-size: 30px;
            }
        }
    }
}

.chart-wrapper {
    margin: 5px;
    height: 400px;

    .yield-chart,
    .valuation-chart {
        height: 250px;
    }
}

.managers {
    margin: 5px;
    margin-bottom: 50px;
    .managers-title {
        font-weight: bold;
        font-size: 18px;
    }
}

.manager {
    margin: 5px;
    display: flex;
    justify-content: space-between;

    .manager-info {
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
        font-size: 14px;

        .manager-name {
            font-weight: bold;
            font-size: 16px;
        }
        .manager-work-time,
        .manager-fund-size {
            color: grey;
        }
    }
    .manager-img {
        width: 80px;
        height: 80px;
    }
}

.buttom-bar {
    height: 50px;
    width: 100%;
    position: fixed;
    bottom: 0;
}
</style>
