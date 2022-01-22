const router = require("koa-router")()
const axios = require("axios")
const { R } = require("./utils/R")
const { JiJuanerException } = require("./utils/JiJuanerException")

router.prefix("/jsdata/fund")

router.get("/hello", async (ctx) => {
    ctx.body = new R().ok().putMsg("hello, jijuaner jsdata!")
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
            ctx.body = new R().ok().putData(JSON.stringify(body))
        })
        .catch((err) => {
            console.log("出错了")
            ctx.body = new R().error().putMsg(err)
        })
})

router.get("/info/:fund_code", async (ctx, next) => {
    let fund_code = ctx.params.fund_code
    console.log(`获取基金数据：${fund_code}`)
    await axios
        .get(`http://fund.eastmoney.com/pingzhongdata/${fund_code}.js?v=${Date.parse(new Date())}`)
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
            ctx.response.body = new R().ok().putData(
                JSON.stringify({
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
            )
        })
        .catch((err) => {
            console.log(err)
            console.log("该基金数据不存在或出错了~")
            ctx.body = new R()
                .error()
                .putCode(JiJuanerException.FUND_INFO_EXCEPTION.code)
                .putMsg("该基金数据不存在或出错了~")
        })
})

module.exports = router
