const router = require("koa-router")()
const axios = require("axios")
const https = require("https")
const { R } = require("./utils/R")
const { JiJuanerException } = require("./utils/JiJuanerException")

// 在 axios 请求时，选择性忽略 SSL
const agent = new https.Agent({
    rejectUnauthorized: false,
})

router.prefix("/jsdata/index")

router.get("/hello", async (ctx) => {
    ctx.body = new R().ok().putMsg("hello, jijuaner jsdata fund!")
})

router.get("/list", async (ctx, next) => {
    console.log("获取所有指数列表")
    await axios
        .get(`http://danjuanapp.com/djapi/index_eva/dj`)
        .then(({data}) => {
            if (data.result_code != 0) {
                throw "外部接口出错了！"
            }
            let body = []
            for (let index of data.data.items) {
                body.push({
                    id: index.id,
                    indexCode: index.index_code,
                    indexName: index.name,
                    pe: index.pe.toFixed(2),
                    pb: index.pb.toFixed(2),
                    pePercentile: (index.pe_percentile * 100).toFixed(2),
                    pbPercentile: (index.pb_percentile * 100).toFixed(2),
                    roe: (index.roe * 100).toFixed(2),
                    yield: (index.yeild * 100).toFixed(2),
                    peg: (index.peg * 100).toFixed(2),
                    pbFlag: index.pb_flag,
                    evalType: index.eva_type,
                })
            }
            ctx.body = new R().ok().putData(body)
        })
        .catch((err) => {
            console.log(err)
            ctx.body = new R().error().putMsg(err)
        })
})

module.exports = router
