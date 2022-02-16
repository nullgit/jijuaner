<template>
    <div class="index-wrapper">
        <van-nav-bar title="指数估值" left-arrow @click-left="handleReturn" />

        <el-table
            :data="indexList"
            :row-class-name="indexClassName"
            style="width: 100%"
            ref="table"
            :height="tableHeight"
        >
            <el-table-column label="指数名称" prop="indexName" width="120" fixed></el-table-column>
            <el-table-column label="PE" prop="pe" width="80" sortable></el-table-column>
            <el-table-column label="PE百分位" prop="pePercentile" width="120" sortable></el-table-column>
            <el-table-column label="PB" prop="pb" width="80" sortable></el-table-column>
            <el-table-column label="PB百分位" prop="pbPercentile" width="120" sortable></el-table-column>
            <el-table-column label="ROE" prop="roe" width="80" sortable></el-table-column>
            <el-table-column label="股息率" prop="yield" width="100" sortable></el-table-column>
            <el-table-column label="PEG" prop="peg" width="80" sortable></el-table-column>
        </el-table>
        <div class="disclaimers">本页的估值数据来自蛋卷基金</div>
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import Footer from "../../components/common/Footer.vue"
import { handleReturn } from "../../utils/common"

export default Vue.extend({
    name: "Value",
    components: {
        Footer,
    },
    data() {
        return {
            tableHeight: 0,
            indexList: [], // { id indexCode pbFlag evalType indexName pe pePercentile pb pbPercentile roe yield peg }
        }
    },
    methods: {
        handleReturn,
        indexClassName({ row }) {
            return row.evalType + "-index"
        },
    },
    mounted() {
        // 为了固定表头
        this.$nextTick(function () {
            this.tableHeight = window.innerHeight - this.$refs.table.$el.offsetTop - 50
            let self = this
            window.onresize = function () {
                self.tableHeight = window.innerHeight - self.$refs.table.$el.offsetTop - 50
            }
        })
        axios
            .get(`/api/fund/indexList/all`)
            .then(({ data }) => {
                // console.log(data.data)
                this.indexList = data.data
            })
            .catch(console.log)
    },
})
</script>

<style lang="less">
.el-table {
    .low-index {
        background: #d4efe1;
    }
    .mid-index {
        background: #ffebc6;
    }
    .high-index {
        background: #ffe7e5;
    }
    .unsort-index {
        background: white;
    }
}
</style>

<style lang="less" scoped>
.disclaimers {
    margin: 5px;
}
</style>
