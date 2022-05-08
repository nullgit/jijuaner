const router = require("koa-router")()
const axios = require("axios")
const { R } = require("./utils/R")
const { JiJuanerException } = require("./utils/JiJuanerException")

router.prefix("/jsdata/fund")

router.get("/hello", async (ctx) => {
    ctx.body = new R().ok().putMsg("hello, jijuaner jsdata fund!")
})

router.get("/list", async (ctx, next) => {
    console.log("获取所有基金列表")
    await axios
        .get(`http://fund.eastmoney.com/js/fundcode_search.js`)
        .then((resp) => {
            eval(resp.data)
            // console.log(r)
            let body = []
            for (let fund of r) {
                body.push({
                    fundCode: fund[0],
                    // 'fundNameSingleSpell': fund[1],
                    fundName: fund[2],
                    fundType: fund[3],
                    // 'fundNameAllSpell': fund[4],
                })
            }
            ctx.body = new R().ok().putData(body)
        })
        .catch((err) => {
            console.log("/list 出错了")
            ctx.body = new R().error().putMsg(err.toString())
        })
})

router.get("/info/:fundCode", async (ctx, next) => {
    let fundCode = ctx.params.fundCode
    console.log(`获取基金数据：${fundCode}`)
    await axios
        .get(`http://fund.eastmoney.com/pingzhongdata/${fundCode}.js?v=${Date.parse(new Date())}`)
        .then((response) => {
            let dataCode = response.data
            // TODO 危险操作
            eval(dataCode)
            Data_ACWorthTrend = Data_ACWorthTrend.map((xy) => {
                return {
                    x: xy[0],
                    y: xy[1],
                }
            })
            Data_rateInSimilarType = Data_rateInSimilarType.map((item) => {
                item.total = Number(item.sc)
                item.sc = undefined
                return item
            })
            let origin_Data_fluctuationScale = Data_fluctuationScale
            // Data_fluctuationScale = {
            //     categories: ["2021-09-30", ... ],
            //     series: [{ y: 698.47, mom: "-22.30%" }, ...],
            // };
            Data_fluctuationScale = []
            for (let i = 0; i < origin_Data_fluctuationScale.categories.length; ++i) {
                Data_fluctuationScale.push({
                    x: origin_Data_fluctuationScale.categories[i],
                    y: origin_Data_fluctuationScale.series[i].y,
                })
            }
            let origin_Data_currentFundManager = Data_currentFundManager
            // [
            //     {
            //         id: "30335060",
            //         pic: "https://pdf.dfcfw.com/pdf/H8_30335060_1.PNG",
            //         name: "左金保",
            //         star: 4,
            //         workTime: "6年又290天",
            //         fundSize: "30.29亿(15只基金)",
            //         power: {
            //             avr: "75.69",
            //             categories: ["经验值", "收益率", "抗风险", "稳定性", "择时能力"],
            //             dsc: [
            //                 "反映基金经理从业年限和管理基金的经验",
            //                 "根据基金经理投资的阶段收益评分，反映\u003cbr\u003e基金经理投资的盈利能力",
            //                 "反映基金经理投资的回撤控制能力",
            //                 "反映基金经理投资收益的波动",
            //                 "反映基金经理根据对市场的判断，通过\u003cbr\u003e调整仓位及配置而跑赢业绩的基准能力",
            //             ],
            //             data: [89.4, 81.6, 51.0, 62.4, 85.5],
            //             jzrq: "2021-12-24",
            //         },
            //         profit: {
            //             categories: ["任期收益", "同类平均", "沪深300"],
            //             series: [
            //                 {
            //                     data: [
            //                         { name: null, color: "#7cb5ec", y: 116.5445 },
            //                         { name: null, color: "#414c7b", y: 134.6 },
            //                         { name: null, color: "#f7a35c", y: 36.04 },
            //                     ],
            //                 },
            //             ],
            //             jzrq: "2021-12-24",
            //         },
            //     },
            // ];
            Data_currentFundManager = []
            for (let manager of origin_Data_currentFundManager) {
                Data_currentFundManager.push({
                    managerId: manager.id,
                    pic: manager.pic,
                    name: manager.name,
                    workTime: manager.workTime,
                    fundSize: manager.fundSize,
                })
            }
            ctx.response.body = new R().ok().putData({
                fundName: fS_name,
                fundCode: fS_code,
                yieldOneYear: syl_1n,
                yieldSixMonths: syl_6y,
                yieldThreeMonths: syl_3y,
                yieldOneMonth: syl_1y,
                // netWorthTrend: Data_netWorthTrend,
                acWorthTrend: Data_ACWorthTrend,
                // [{ x: 1357228800000, y: 1.0}, ...]
                ranksInSimilarType: Data_rateInSimilarType,
                // [{ x: 1357228800000, y: 303, total: 389 }, ...]
                currentManagers: Data_currentFundManager,
                // [{
                //     managerId: "xxx",
                //     pic: "http://xxx",
                //     name: "xxx",
                //     workTime: "xxx",
                //     fundSize: "xxx"
                // }, ...]
                scales: Data_fluctuationScale,
                // [{ x: "2021-09-30", y: 100.01}, ...]
            })
        })
        .catch((err) => {
            console.log(err)
            ctx.body = new R()
                .error()
                .putCode(JiJuanerException.FUND_INFO_EXCEPTION.code)
                .putMsg("该基金数据不存在或出错了~")
        })
})

router.get("/realTime/:fundCode", async (ctx, next) => {
    let fundCode = ctx.params.fundCode
    console.log(`获取基金实时数据：${fundCode}`)
    await axios
        .get(`http://fundgz.1234567.com.cn/js/${fundCode}.js?rt=${new Date().getTime()}`)
        .then((resp) => {
            let resultStr = resp.data
            let data = JSON.parse(resultStr.substring(8, resultStr.length - 2))
            // "fundcode":"005827",  // 基金代码
            // "name":"易方达蓝筹精选混合",  // 基金名字
            // "jzrq":"2021-12-23",  // 截止日期
            // "dwjz":"2.6359",  // 单位净值
            // "gsz":"2.6478",  // 估算值
            // "gszzl":"0.45",  // 估算增长率
            // "gztime":"2021-12-24 15:00"  // 估值时间
            data = {
                fundCode: data.fundcode,
                fundName: data.name,
                netWorth: data.dwjz,
                valuation: data.gsz,
                valuationRate: data.gszzl,
                valuationTime: data.gztime,
                date: data.jzrq,
            }
            ctx.body = new R().ok().putData(data)
        })
        .catch((err) => {
            console.log("出错了")
            ctx.body = new R().error().putMsg(err.toString())
        })
})

router.get("/evalImg/:fundCode", async (ctx, next) => {
    let fundCode = ctx.params.fundCode
    console.log(`获取基金估值图数据：${fundCode}`)
    await axios
        .get(`http://j4.dfcfw.com/charts/pic6/${fundCode}.png?v=${new Date().getTime()}`)
        .then((resp) => {
            ctx.body = new R().ok().putData(resp.data)
        })
        .catch((err) => {
            console.log("出错了")
            ctx.body = new R().error().putMsg(err.toString())
        })
})

router.get("/subscriptionStatus", async (ctx, next) => {
    console.log(`获取所有基金申购状态`)
    await axios
        .get(`http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?page=1,50000&t=8&sort=fcode,asc`)
        .then((resp) => {
            let data = JSON.parse(resp.data.substring(14, resp.data.lastIndexOf("]]") + 2))
            data = data.map((arr) => {
                // [
                //  0 "980003" 基金代码
                //  1 "太平洋六个月滚动持有债"  基金名称
                //  2 "债券型-长债"  类型
                //  3 "1.5223"  最新净值
                //  4 "03-24"  最新净值 报告时间
                //  5 "开放申购"  申购状态
                //  6 "开放赎回"  赎回状态
                //  7 ""  下一个开放日
                //  8 "100" 购买起点
                //  9 "100000000000"  日累计限定额
                //  10 "1"  -
                //  11 "1"  -
                //  12 "0.05%"  手续费
                // ]
                return {
                    fundCode: arr[0],
                    fundName: arr[1],
                    fundType: arr[2],
                    subscriptionStatus: arr[5],
                    redemptionStatus: arr[6],
                    nextOpenDay: arr[7],
                    minAmount: Math.round(arr[8] * 100), // 改为 BigInteger 类型, 小数后2位
                    maxAmountPerDay: Math.round(arr[9] * 100), // 改为 BigInteger 类型, 小数后2位
                    serviceCharge: Math.round(arr[12].replace("%", "") * 1000), // 改为 BigInteger 类型, 小数后3位
                }
            })
            ctx.body = new R().ok().putData(data)
        })
        .catch((err) => {
            console.log("出错了")
            ctx.body = new R().error().putMsg(err.toString())
        })
})

module.exports = router
