const Koa = require("koa")
const app = new Koa()
const views = require("koa-views")
const json = require("koa-json")
const onerror = require("koa-onerror")
const bodyparser = require("koa-bodyparser")
const logger = require("koa-logger")
const cors = require("koa2-cors")

const index = require("./routes/index")
const fund = require("./routes/fund")

// routes
app.use(index.routes(), index.allowedMethods())
app.use(fund.routes(), fund.allowedMethods())

// error handler
onerror(app)

// middlewares
app.use(
    bodyparser({
        enableTypes: ["json", "form", "text"],
    })
)
app.use(json())
app.use(logger())
app.use(require("koa-static")(__dirname + "/public"))
// 解决跨域的问题
app.use(cors())

app.use(
    views(__dirname + "/views", {
        extension: "pug",
    })
)

// logger
app.use(async (ctx, next) => {
    const start = new Date()
    await next()
    const ms = new Date() - start
    console.log(`${ctx.method} ${ctx.url} - ${ms}ms`)
})

// error-handling
app.on("error", (err, ctx) => {
    console.error("server error", err, ctx)
})

// nacos
// 'use strict';
const NacosNamingClient = require("nacos").NacosNamingClient
const nacosLogger = console
const nacosClient = new NacosNamingClient({
    logger: nacosLogger,
    serverList: "127.0.0.1:8848", // replace to real nacos serverList
    namespace: "0686879d-ea4f-45b0-ad9a-9da5681d2420",
})
nacosClient.ready()
const serviceName = "jsdata"
// registry instance
nacosClient.registerInstance(
    serviceName,
    {
        ip: "127.0.0.1",
        port: 33333,
    },
    "dev"
)

// // subscribe instance
// client.subscribe(serviceName, hosts => {
//   console.log(hosts);
// });

// const NacosConfigClient = require('nacos').NacosConfigClient; // js

// // for direct mode
// const configClient = new NacosConfigClient({
//   serverAddr: '127.0.0.1:8848',
// });

module.exports = app
