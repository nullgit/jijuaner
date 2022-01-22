<template>
    <div>
        <!-- info/{{ $route.params.id }}-{{ fundInfo.fundCode }} -->
        <div class="titleWrapper">
            <div class="fundName">{{ fundInfo.fundName }}</div>
            <div class="fundCode">{{ fundInfo.fundCode }}</div>
            <div class="fundType">{{ fundInfo.fundType }}</div>
        </div>
        ---------------------------
        <div class="infoWrapper">
            <div class="yieldOneDay">日涨跌：{{ yieldOneDay }}%</div>
            <div class="yieldOneYear">
                近一年收益率：{{
                    fundInfo.yieldOneYear ? `${fundInfo.yieldOneYear}%` : "该基金成立不足一年"
                }}
            </div>
            <div class="netWorth">最新净值：{{ netWorth }}</div>
        </div>
        <div class="chartWrapper">
            <el-tabs v-model="navName" @tab-click="handleClickNav">
                <el-tab-pane label="收益详情" name="yieldDetail">
                    <div class="yieldChart" id="yieldChart"></div>
                    <el-tabs
                        v-model="yieldNavName"
                        tab-position="bottom"
                        style="height: 200px"
                        @tab-click="handleClickYieldNav"
                    >
                        <el-tab-pane label="今年来" name="thisYear"></el-tab-pane>
                        <el-tab-pane label="近1月" name="nearOneMonth"></el-tab-pane>
                        <el-tab-pane label="近1年" name="nearOneYear"></el-tab-pane>
                        <el-tab-pane label="近3年" name="nearThreeYear"></el-tab-pane>
                        <el-tab-pane label="成立以来" name="sinceEstablishment"></el-tab-pane>
                    </el-tabs>
                </el-tab-pane>

                <el-tab-pane label="实时估值" name="realTimeValuation"> 实时估值 </el-tab-pane>
            </el-tabs>
        </div>
        <div class="detailWrapp">
            
        </div>
        <div class="managersWrapper">
            <div v-for="manager of fundInfo.currentManagers" :key="manager.managerId" class="managerWrapper">
                <img :src="manager.pic" alt="">
                {{manager.name}}-{{manager.workTime}}-{{manager.fundSize}}
            </div>
        </div>
        <div class="buttomBar">
            <el-button v-if="isOptional" round @click="cancelOption">取消自选</el-button>
            <el-button v-else type="warning" round @click="addOption">加自选</el-button>
        </div>
    </div>
</template>

<script>
import Vue from "vue"
import axios from "axios"
import dayjs from "dayjs"
import {config} from "../../../utils/config"

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
        return {fundCode: params.id}
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
                if(data.code == 0) {
                    this.isOptional = true
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
                if(data.code == 0) {
                    this.isOptional = false
                }
            })
            .catch(console.log)
        },
        echartsInit() {
            // 在这里 acWorthTrend 是正序的，使用完后 acWorthTrend 会变成逆序的（时间从近到远）
            let acWorthTrend = this.fundInfo.acWorthTrend
            let yieldChart = this.$echarts.init(document.getElementById("yieldChart"))
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
                this.getOptionalStatus()
                this.echartsInit()
                this.fundInfo.acWorthTrend.reverse()
            })
            .catch(console.log)
    },
})
</script>

<style lang="less" scoped>
.yieldChart {
    width: 100%;
    height: 300px;
    // background-color: #bfa;
}

.buttomBar {
    height: 50px;
    width: 100%;
    position: fixed;
    bottom: 0;
    display: flex;
    flex-direction: row;
    justify-content: space-evenly;
}
</style>
