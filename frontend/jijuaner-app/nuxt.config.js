import { config } from "./utils/config"
export default {
    // Global page headers: https://go.nuxtjs.dev/config-head
    head: {
        title: "jijuaner-app",
        htmlAttrs: {
            lang: "en",
        },
        meta: [
            { charset: "utf-8" },
            { name: "viewport", content: "width=device-width, initial-scale=1" },
            { hid: "description", name: "description", content: "" },
            { name: "format-detection", content: "telephone=no" },
        ],
        link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    },

    // Global CSS: https://go.nuxtjs.dev/config-css
    css: ["static/css/reset.css", "element-ui/lib/theme-chalk/index.css"],

    // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
    plugins: ["@/plugins/element-ui", "@/plugins/echarts", "~/plugins/vant"],

    // Auto import components: https://go.nuxtjs.dev/config-components
    components: true,

    // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
    buildModules: [],

    // Modules: https://go.nuxtjs.dev/config-modules
    modules: ["@nuxtjs/axios"],
    axios: {
        proxy: true,
    },
    proxy: {
        // '/api': 'http://app.jijuaner.com:80/',
        "/api": config.gateway,
    },

    // Build Configuration: https://go.nuxtjs.dev/config-build
    build: {
        // transpile: [/^element-ui/],
        babel: {
            plugins: [
                [
                    "import",
                    {
                        libraryName: "vant",
                        // 目前在 nuxt 中无法按需引入样式，因此采用手动引入的方式
                        style: false,
                    },
                    "vant",
                ],
                [
                    "component",
                    {
                        libraryName: "element-ui",
                        styleLibraryName: "theme-chalk",
                    },
                ],
            ],
        },
    },
}
