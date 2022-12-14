<template>
    <div class="option-wrapper">
        <van-tabs class="top-tabs" type="card">
            <van-tab title="自选基金">
                <el-card class="option-fund">
                    <div v-if="loading">加载中</div>
                    <div v-else-if="!isSignIn">您尚未登录！</div>
                    <div v-else-if="userOptions.length == 0">
                        您尚未开通基金自选服务！
                        <el-button type="primary" @click="enableOption">开通基金自选服务</el-button>
                    </div>

                    <van-tabs v-else class="option-group" v-model="activeGroupId" @click="handleClickGroup">
                        <van-tab
                            v-for="(group, index) of userOptions"
                            :key="index"
                            :title="group.groupName"
                            :name="group.groupId.toString()"
                        ></van-tab>

                        <div class="buttons">
                            <nuxt-link to="/search" v-if="activeGroupId == userOptions[0].groupId">
                                <el-button type="text">添加自选基金</el-button>
                            </nuxt-link>
                            <el-button v-else @click="addFundDrawer = true" type="text">从“全部”中添加</el-button>
                            <el-button @click="fundDrawer = true" type="text"> 管理自选基金 </el-button>

                            <van-button
                                class="option-group-manage-btn"
                                color="linear-gradient(to right, #ff6034, #ee0a24)"
                                @click="groupDrawer = true"
                            >
                                自选分组管理
                            </van-button>
                        </div>

                        <van-action-sheet class="option-group-manage" v-model="groupDrawer" title="自选分组管理">
                            <el-button class="add-new-group-btn" type="text" @click="addNewGroupDialog = true">添加分组</el-button>

                            <van-dialog
                                v-model="addNewGroupDialog"
                                title="请输入分组名称"
                                show-cancel-button
                                :beforeClose="addNewGroup"
                            >
                                <van-field
                                    v-model="addNewGroupInput"
                                    autosize
                                    label="分组名称"
                                    type="textarea"
                                    placeholder="最多6个字符"
                                />
                            </van-dialog>

                            <van-dialog
                                v-model="renameGroupDialog"
                                title="重命名分组"
                                show-cancel-button
                                :before-close="renameGroup"
                            >
                                <van-field
                                    v-model="renameGroupInput"
                                    autosize
                                    label="新分组名称"
                                    type="textarea"
                                    placeholder="最多6个字符"
                                />
                            </van-dialog>
                            <el-table :data="userOptions" style="width: 100%">
                                <!-- 删除分组 -->
                                <el-table-column label="" width="50">
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
                                <el-table-column prop="groupName" label="分组名称" width="120"></el-table-column>
                                <el-table-column label="编辑组名" width="80">
                                    <template slot-scope="scope">
                                        <el-button
                                            icon="el-icon-edit"
                                            v-if="scope.$index != 0"
                                            circle
                                            @click="showRenameGroupDialog(scope.row)"
                                        >
                                        </el-button>
                                    </template>
                                </el-table-column>
                                <!-- 上移分组 -->
                                <el-table-column label="" width="50">
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
                                <el-table-column label="" width="50">
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
                        </van-action-sheet>

                        <van-action-sheet v-model="fundDrawer" title="管理自选基金">
                            <el-table :data="groupIdFundsMap[activeGroupId]" style="width: 100%">
                                <!-- 删除基金 -->
                                <el-table-column label="" width="80">
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
                                <el-table-column label="基金代码" prop="fundCode" width="100"></el-table-column>
                                <el-table-column label="基金名称" prop="fundName"></el-table-column>
                            </el-table>
                        </van-action-sheet>

                        <van-action-sheet class="add-fund-from-all" v-model="addFundDrawer" title="从【全部】分组中添加基金">
                            <!-- :title="groupIdFundsMap[activeGroupId].groupName" -->
                            <el-button class="add-fund-to-cur" type="primary" @click="addNewFunds">添加到当前分组</el-button>
                            <el-table
                                :data="groupIdFundsMap[userOptions[0].groupId]"
                                style="width: 100%"
                                @selection-change="handleSelectFundFromAll"
                                ref="addFundFromAllTable"
                            >
                                <!-- TODO 退出的时候清除选择的内容 -->
                                <el-table-column type="selection" width="55" :selectable="selectableFormAll">
                                </el-table-column>
                                <el-table-column label="基金代码" prop="fundCode"></el-table-column>
                                <el-table-column label="基金名称" prop="fundName"></el-table-column>
                            </el-table>
                        </van-action-sheet>

                        <el-table
                            class="fund-list"
                            :data="groupIdFundsMap[activeGroupId]"
                            highlight-current-row
                            @current-change="clickInfo"
                        >
                            <!-- <el-table-column label="基金代码" prop="fundCode" width="100"/> -->
                            <el-table-column label="基金名称" prop="fundName" width="200"/>
                            <el-table-column label="估算涨跌幅" prop="valuationRate" width="100"/>
                            <el-table-column label="近一月" prop="yieldOneMonth"/>
                            <el-table-column label="近三月" prop="yieldThreeMonths"/>
                            <el-table-column label="近六月" prop="yieldSixMonths"/>
                            <el-table-column label="近一年" prop="yieldOneYear"/>
                            <el-table-column label="类型" prop="fundType"/>
                            <el-table-column label="日期" prop="date"/>
                            <el-table-column label="净值" prop="netWorth"/>
                        </el-table>
                    </van-tabs>
                </el-card>
            </van-tab>
            <van-tab title="自选股票">
                尚未实现，敬请期待！
            </van-tab>
        </van-tabs>

        <Footer activeNavProp="自选" />
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
            addNewGroupDialog: false,
            addNewGroupInput: "",
            fundDrawer: false,
            addFundDrawer: false,
            selectedFunds: [],
            renameGroupDialog: false,
            renameGroupRow: {},
            renameGroupInput: "",
        }
    },
    methods: {
        enableOption() {
            axios.get(`/api/user/userOption/enableOption`).then(({ data }) => {
                if (data.code == 0) {
                    this.$notify({ type: "success", message: data.msg })
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
                        this.$notify({ type: "danger", message: "请先登录！" })
                        return
                    }
                    this.isSignIn = true
                    if (data.code == JiJuanerException.OPTION_GROUP_EXCEPTION.code) {
                        return
                    }
                    this.userOptions = data.data
                    // Vue.set(this.userOptions, data.data)
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
                        this.$notify({ type: "danger", message: data.msg })
                        return
                    }
                    let funds = data.data.funds
                    let infos = data.data.infos
                    this.$set(this.groupIdFundsMap, this.activeGroupId, [])
                    this.$set(this.groupIdFundSetMap, this.activeGroupId, {})
                    for (let i = 0; i < funds.length; ++i) {
                        // 展示的 table
                        this.groupIdFundsMap[this.activeGroupId].push({
                            fundCode: funds[i],
                            ...infos[i],
                        })
                        // 将对应 set 中的基金置为 true（存在）
                        this.groupIdFundSetMap[this.activeGroupId][funds[i]] = true
                    }
                    console.log('getFunds!!!')
                    console.log(this.groupIdFundsMap[this.activeGroupId])
                })
                .catch(console.log)
        },
        handleClickGroup() {
            this.getFunds()
        },
        groupNameValidator(groupName) {
            groupName = groupName.trim()
            if (groupName == null || groupName.length == 0) {
                return "输入不能为空"
            } else if (groupName.length > 6) {
                return "最多6个字符"
            }
            return null
        },
        addNewGroup(action, done) {
            if (action == "cancel") {
                done()
                return
            } else if (this.userOptions.length >= 100) {
                this.$notify({ type: "danger", message: "您已添加超过100个分组！" })
                done()
                return
            }
            let msg = this.groupNameValidator(this.addNewGroupInput)
            if (msg != null) {
                this.$notify({ type: "danger", message: msg })
                done()
            } else {
                axios
                    .get(`/api/user/userOption/addNewGroup?groupName=${this.addNewGroupInput}`)
                    .then(({ data }) => {
                        if (data.code == 0) {
                            this.$notify({ type: "success", message: `你的新分组名称是: ${this.addNewGroupInput}` })
                            this.addNewGroupInput = ""
                            this.getGroups()
                            done()
                        } else {
                            this.$notify({ type: "danger", message: data.msg })
                            done()
                        }
                    })
                    .catch(console.log)
            }
        },
        delGroup(optionGroup) {
            this.$dialog
                .confirm({ title: "删除分组", message: `确认删除分组：【${optionGroup.groupName}】？` })
                .then(() => {
                    axios
                        .get(`/api/user/userOption/delGroup?groupId=${optionGroup.groupId}`)
                        .then(() => {
                            this.$notify({ type: "success", message: "删除成功" })
                            this.getGroups()
                        })
                        .catch(console.log)
                })
                .catch(console.log)
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
                        this.$notify({ type: "success", message: "移动成功" })
                    }
                })
                .catch(console.log)
        },
        showRenameGroupDialog(optionGroup) {
            this.renameGroupDialog = true
            this.renameGroupRow = optionGroup
        },
        renameGroup(action, done) {
            if (action == "cancel") {
                done()
                return
            }
            let msg = this.groupNameValidator(this.renameGroupInput)
            if (msg != null) {
                this.$notify({ type: "danger", message: msg })
                done()
            } else {
                axios
                    .get(
                        `/api/user/userOption/renameGroup?groupId=${this.renameGroupRow.groupId}&groupName=${this.renameGroupInput}`
                    )
                    .then(({ data }) => {
                        if (data.code == 0) {
                            this.$notify({ type: "success", message: `你的新分组名称是: ${this.renameGroupInput}` })
                            this.getGroups()
                            done()
                        } else {
                            this.$notify({ type: "danger", message: data.msg })
                            done()
                        }
                    })
                    .catch(console.log)
            }
        },
        handleDelFunds(row) {
            if (this.activeGroupId == this.userOptions[0].groupId) {
                this.$dialog
                    .confirm({ message: "从【全部】分组中删除，也将删除其他分组的该基金，确认删除？" })
                    .then(() => {
                        this.delFunds([row.fundCode])
                    })
                    .catch(console.log)
            } else {
                this.delFunds([row.fundCode])
            }
        },
        delFunds(funds) {
            axios
                .post(`/api/user/userOption/delFunds`, {
                    funds,
                    groupId: this.activeGroupId == this.userOptions[0].groupId ? null : this.activeGroupId,
                })
                .then(({ data }) => {
                    if (data.code == 0) {
                        this.$notify({ type: "success", message: "删除成功", duration: 500 })
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
                        this.$notify({ type: "success", message: `添加成功` })
                        this.$refs.addFundFromAllTable.clearSelection()
                        this.getFunds()
                    }
                })
                .catch(console.log)
        },
        clickInfo(currentRow) {
            this.$router.push(`/info/${currentRow.fundCode}`)
        },
    },
    mounted() {
        this.getGroups()
    },
})
</script>

<style lang="less" scoped>
.top-tabs {
    margin: 10px 0;
    .option-fund {
        margin: 5px;
        margin-bottom: 50px;
    }
}


.buttons {
    // display: flex;
    .option-group-manage-btn {
        float: right;
    }
}

.option-group-manage {
    .add-new-group-btn{
        margin-left: 10px;
    }
}

.add-fund-from-all {
    .add-fund-to-cur{
        margin-left: 10px;
    }
}
</style>
