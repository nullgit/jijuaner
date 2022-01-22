<template>
    <div>
        <Search />
    </div>
</template>

<script>
import Vue from "vue"
import Search from "../../components/common/Search.vue"

export default Vue.extend({
    components: { Search },
    name: "SearchPage",
})
</script>
