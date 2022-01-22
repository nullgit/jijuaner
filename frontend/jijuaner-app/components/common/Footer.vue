<template>
    <ul class="footer">
        <li v-for="navItem of nav" :key="navItem.name">
            <nuxt-link :to="navItem.to">
                <i :class="navItem.icon"></i>
                <div>{{ navItem.name }}</div>
            </nuxt-link>
            <!-- {{navItem.icon}}-{{navItem.a}}-{{navItem.name}} -->
            <!-- <el-button>默认按钮</el-button> -->
        </li>
    </ul>
</template>

<script lang='ts'>
export default {
    name: "Footer",
    data() {
        return {
            nav: [
                { icon: "el-icon-s-home", to: "/", name: "主页" },
                { icon: "el-icon-star-on", to: "option", name: "自选" },
                { icon: "el-icon-s-custom", to: "my", name: "我的" },
            ],
        }
    },
}
</script>

<style lang='less' scoped>
.footer {
    height: 50px;
    width: 100%;

    position: fixed;
    bottom: 0;

    display: flex;
    flex-direction: row;
    justify-content: space-evenly;
}

i {
    background-color: #bfa;
}

a {
    text-decoration: none;
    color: inherit;
}
</style>
