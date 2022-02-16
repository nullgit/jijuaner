<template>
    <div>
        <van-nav-bar title="搜索" left-arrow @click-left="handleReturn" />
        <Search />
    </div>
</template>

<script>
import Vue from "vue"
import Search from "../../components/common/Search.vue"
import { handleReturn } from "../../utils/common"

export default Vue.extend({
    components: { Search },
    name: "SearchPage",
    methods: {
        handleReturn,
    }
})
</script>
