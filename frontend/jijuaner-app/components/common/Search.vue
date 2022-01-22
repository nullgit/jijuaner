<template>
    <!-- <el-input placeholder="请输入内容" prefix-icon="el-icon-search" v-model="input2"> </el-input> -->
    <!--

        prefix-icon 是前面的图标
     -->
    <el-autocomplete
        class="search-box"
        prefix-icon="el-icon-search"
        placeholder="请输入基金名称或代码"
        v-model="input"
        :fetch-suggestions="querySearchAsync"
        @select="handleSelect"
    ></el-autocomplete>
</template>

<script>
import axios from "axios"
export default {
    name: "Search",
    data() {
        return {
            fundList: [],
            input: "",
        }
    },
    methods: {
        querySearchAsync(queryString, callback) {
            axios
                .get(`/api/search/search?input=${queryString}`)
                .then(({ data }) => {
                    // console.log(data)
                    if (data.data == null) {
                        callback([])
                    }
                    callback(
                        data.data.map((fund) => {
                            return {
                                value: `${fund.fundName} - ${fund.fundCode}`,
                                fundCode: fund.fundCode,
                            }
                        })
                    )
                })
                .catch(console.log)
            callback([])
        },
        handleSelect(item) {
            // console.log(item)
            location.assign(`/info/${item.fundCode}`)
        },
    },
    mounted() {
        // this.restaurants = this.loadAll()
    },
}
</script>

<style lang='less' scoped>
.search-box {
    width: 100%;
    height: 180px;
}
</style>
