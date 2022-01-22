<template>
    <div>
        <div v-if="loading">加载中</div>
        <div v-else-if="!isSignIn" class="notSignIn">您尚未登录！</div>
        <div v-else-if="userOptions.length == 0" class="optionDisabled">
            您尚未开通基金自选服务！
            <el-button type="primary" @click="enableOption">开通基金自选服务</el-button>
        </div>
        <div v-else class="optionWrapper">
            <el-tabs type="border-card" v-model="activeGroupId" @tab-click="handleClickGroup">
                <el-tab-pane
                    v-for="group of userOptions"
                    :key="group.groupId"
                    :label="group.groupName"
                    :name="group.groupId.toString()"
                >
                </el-tab-pane>
                <nuxt-link to="/search" v-if="activeGroupId == userOptions[0].groupId">添加自选基金</nuxt-link>
                <el-button v-else @click="addFundDrawer = true">从“全部”分组中添加基金</el-button>
                <el-button @click="fundDrawer = true" type="text"> 管理自选基金 </el-button>
                <el-button @click="groupDrawer = true" type="primary"> 自选分组管理 </el-button>

                <el-drawer
                    class="manageOption"
                    title="自选分组管理"
                    :visible.sync="groupDrawer"
                    direction="btt"
                    :size="'70%'"
                >
                    <!-- :before-close="handleClose" -->
                    <el-button type="text" @click="addNewGroup">添加分组</el-button>
                    <el-table :data="userOptions" style="width: 100%">
                        <!-- 删除分组 -->
                        <el-table-column label="" width="100">
                            <template slot-scope="scope">
                                <el-button
                                    v-if="scope.$index != 0"
                                    type="danger"
                                    icon="el-icon-delete"
                                    circle
                                    @click="delGroup(scope.row)"
                                    size="mini"
                                ></el-button>
                            </template>
                        </el-table-column>
                        <el-table-column prop="groupName" label="分组名称"></el-table-column>
                        <el-table-column label="编辑组名">
                            <template slot-scope="scope">
                                <el-button
                                    icon="el-icon-edit"
                                    v-if="scope.$index != 0"
                                    circle
                                    @click="renameGroup(scope.row)"
                                ></el-button>
                            </template>
                        </el-table-column>
                        <!-- <el-table-column label="编辑组内基金">
                            <el-button type="primary" icon="el-icon-edit" circle @click="fundDrawer = true"></el-button>
                        </el-table-column> -->
                        <!-- 上移分组 -->
                        <el-table-column label="">
                            <template slot-scope="scope">
                                <el-button
                                    v-if="scope.$index > 1"
                                    icon="el-icon-top"
                                    circle
                                    size="mini"
                                    @click="moveGroup(scope.$index, -1)"
                                ></el-button>
                            </template>
                        </el-table-column>
                        <!-- 下移分组 -->
                        <el-table-column label="">
                            <template slot-scope="scope">
                                <el-button
                                    v-if="scope.$index != userOptions.length - 1 && scope.$index != 0"
                                    icon="el-icon-bottom"
                                    circle
                                    size="mini"
                                    @click="moveGroup(scope.$index, 1)"
                                ></el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-drawer>

                <el-drawer
                    class="manageFundDrawer"
                    title="管理自选基金"
                    :visible.sync="fundDrawer"
                    direction="btt"
                    :size="'100%'"
                >
                    <!-- :before-close="handleClose" -->
                    <!-- :title="groupIdFundsMap[activeGroupId].groupName" -->
                    <el-button type="text" @click="addNewGroup">添加基金</el-button>
                    <el-button type="primary" @click="addNewGroup">保存更改</el-button>
                    <el-table :data="groupIdFundsMap[activeGroupId]" style="width: 100%">
                        <!-- 删除基金 -->
                        <el-table-column label="" width="100">
                            <template slot-scope="scope">
                                <el-button
                                    type="danger"
                                    icon="el-icon-delete"
                                    circle
                                    size="mini"
                                    @click="handleDelFunds(scope.row)"
                                ></el-button>
                            </template>
                        </el-table-column>
                        <el-table-column label="基金代码" prop="fundCode"></el-table-column>
                        <el-table-column label="基金名称" prop="fundName"></el-table-column>
                    </el-table>
                </el-drawer>

                <el-drawer
                    class="addFundFromAllDrawer"
                    title="从【全部】分组中添加基金"
                    :visible.sync="addFundDrawer"
                    direction="btt"
                    :size="'100%'"
                >
                    <!-- :title="groupIdFundsMap[activeGroupId].groupName" -->
                    <el-button type="primary" @click="addNewFunds">添加到当前分组</el-button>
                    <el-table
                        :data="groupIdFundsMap[userOptions[0].groupId]"
                        style="width: 100%"
                        @selection-change="handleSelectFundFromAll"
                        ref="addFundFromAllTable"
                    >
                    <!-- TODO 退出的时候清除选择的内容 -->
                        <el-table-column type="selection" width="55" :selectable="selectableFormAll"> </el-table-column>
                        <el-table-column label="基金代码" prop="fundCode"></el-table-column>
                        <el-table-column label="基金名称" prop="fundName"></el-table-column>
                    </el-table>
                </el-drawer>

                <ul class="fundList">
                    fundList{{
                        activeGroupId
                    }}:
                    <li
                        v-for="fund of groupIdFundsMap[activeGroupId]"
                        :key="fund.fundCode"
                        class="fundItem"
                    >
                        <nuxt-link :to="`info/${fund.fundCode}`">{{ fund.fundCode + fund.fundName }}</nuxt-link>
                    </li>
                </ul>
            </el-tabs>
        </div>
        <Footer />
    </div>
</template>

<script>
import axios from "axios"
import Vue from "vue"
import Footer from "../../components/common/Footer.vue"
import { JiJuanerException } from "../../utils/JiJuanerException"

export default Vue.extend({
    name: "Option",
    components: {
        Footer,
    },
    data() {
        return {
            loading: true,
            isSignIn: false,
            userOptions: [],
            // [{groupId: xxx, groupName: xxx, funds: [ xxx, xxx ], sort: x}, ...]
            activeGroupId: "-1",
            groupIdFundsMap: {},
            // { groupId: [ {fundCode: xxx, fundName: xxx}, ... ], ...}
            groupIdFundSetMap: {},
            // { groupId: { xxx: true, xxx: true, ...}, ...}
            groupDrawer: false,
            fundDrawer: false,
            addFundDrawer: false,
            selectedFunds: [],
        }
    },
    methods: {
        enableOption() {
            axios.get(`/api/user/userOption/enableOption`).then(({ data }) => {
                if (data.code == 0) {
                    alert(data.msg)
                    this.getGroups()
                }
            })
        },
        getGroups() {
            // 得到该用户的基金分组 + 默认分组的全部基金
            axios
                .get(`/api/user/userOption/getGroups`)
                .then(({ data }) => {
                    this.loading = false
                    if (data.code == JiJuanerException.SIGN_IN_EXCEPTION.code) {
                        alert("请先登录！")
                        return
                    }
                    this.isSignIn = true
                    if (data.code == JiJuanerException.OPTION_GROUP_EXCEPTION.code) {
                        return
                    }
                    this.userOptions = data.data
                    this.activeGroupId = this.userOptions[0].groupId.toString()
                    this.getFunds()
                })
                .catch(console.log)
        },
        getFunds() {
            // 得到该用户某分组id下的所有基金
            if (this.activeGroupId == "-1") {
                return
            }
            axios
                .get(`/api/user/userOption/getFunds?groupId=${this.activeGroupId}`)
                .then(({ data }) => {
                    if (data.code != 0) {
                        alert(data.msg)
                        return
                    }
                    let funds = data.data.funds
                    let fundNames = data.data.fundNames
                    this.$set(this.groupIdFundsMap, this.activeGroupId, [])
                    this.$set(this.groupIdFundSetMap, this.activeGroupId, {})
                    for (let i = 0; i < funds.length; ++i) {
                        this.groupIdFundsMap[this.activeGroupId].push({
                            fundCode: funds[i],
                            fundName: fundNames[i],
                        })
                        this.groupIdFundSetMap[this.activeGroupId][funds[i]] = true
                    }
                })
                .catch(console.log)
        },
        handleClickGroup() {
            this.getFunds()
        },
        handleClose(done) {
            this.$confirm("确认关闭？")
                .then((_) => {
                    done()
                })
                .catch((_) => {})
        },
        groupNameValidator(groupName) {
            groupName = groupName.trim()
            if (groupName == null || groupName.length == 0) {
                return "输入不能为空"
            } else if (groupName.length > 6) {
                return "最多6个字符"
            }
            return true
        },
        addNewGroup() {
            if (this.userOptions.length >= 100) {
                this.$message({ type: "error", message: "您已添加超过100个分组！" })
            }
            // this.$prompt(文字框上的提示, 标题)
            this.$prompt("最多6个字符", "请输入分组名称", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                inputValidator: this.groupNameValidator,
            })
                .then(({ value }) => {
                    axios
                        .get(`/api/user/userOption/addNewGroup?groupName=${value}`)
                        .then(({ data }) => {
                            if (data.code == 0) {
                                this.$message({ type: "success", message: `你的新分组名称是: ${value}` })
                                this.getGroups()
                            } else {
                                this.$message({ type: "error", message: data.msg })
                            }
                        })
                        .catch(console.log)
                })
                .catch(() => {
                    this.$message({ type: "info", message: "取消输入" })
                })
        },
        delGroup(optionGroup) {
            this.$confirm(`确认删除分组：【${optionGroup.groupName}】？`).then((_) => {
                axios
                    .get(`/api/user/userOption/delGroup?groupId=${optionGroup.groupId}`)
                    .then(() => {
                        this.$message({ type: "success", message: "删除成功" })
                        this.getGroups()
                    })
                    .catch(console.log)
            })
        },
        moveGroup(groupRow, step) {
            this.swapGroup(groupRow, groupRow + step)
        },
        swapGroup(idx1, idx2) {
            axios
                .post(`/api/user/userOption/swapGroup?idx1=${idx1}&idx2=${idx2}`)
                .then(({ data }) => {
                    if (data.code != 0) {
                        alert(data.msg)
                    } else {
                        this.getGroups()
                        this.$message({ type: "success", message: "移动成功" })
                    }
                })
                .catch(console.log)
        },
        renameGroup(optionGroup) {
            this.$prompt("最多6个字符", "请输入新的分组名称", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                inputValidator: this.groupNameValidator,
            })
                .then(({ value }) => {
                    axios
                        .get(`/api/user/userOption/renameGroup?groupId=${optionGroup.groupId}&groupName=${value}`)
                        .then(({ data }) => {
                            if (data.code == 0) {
                                this.$message({ type: "success", message: `你的新分组名称是: ${value}` })
                                this.getGroups()
                            } else {
                                this.$message({ type: "error", message: data.msg })
                            }
                        })
                        .catch(console.log)
                })
                .catch(() => {
                    this.$message({ type: "info", message: "取消输入" })
                })
        },
        handleDelFunds(row) {
            if (this.activeGroupId == this.userOptions[0].groupId) {
                this.$confirm("从【全部】分组中删除，也将删除其他分组的该基金，确认删除？")
                    .then((_) => {
                        this.delFunds([row.fundCode])
                        done()
                    })
                    .catch((_) => {})
            }
            this.delFunds([row.fundCode])
        },
        delFunds(funds) {
            axios
                .post(`/api/user/userOption/delFunds`, {
                    funds,
                    groupId: this.activeGroupId == this.userOptions[0].groupId ? null : this.activeGroupId,
                })
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.$message({ type: "success", message: `删除成功` })
                        this.getFunds()
                    }
                })
                .catch(console.log)
        },
        handleSelectFundFromAll(val) {
            this.selectedFunds = val
            console.log(val)
        },
        selectableFormAll(row) {
            return !this.groupIdFundSetMap[this.activeGroupId][row.fundCode]
        },
        addNewFunds() {
            axios
                .post(`/api/user/userOption/addNewFunds`, {
                    funds: this.selectedFunds.map((fund) => fund.fundCode),
                    groupId: this.activeGroupId,
                })
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.$message({ type: "success", message: `添加成功` })
                        this.$refs.addFundFromAllTable.clearSelection()
                        this.getFunds()
                    }
                })
                .catch(console.log)
        },
    },
    mounted() {
        this.getGroups()
    },
})
</script>

<style lang="less" scoped></style>
