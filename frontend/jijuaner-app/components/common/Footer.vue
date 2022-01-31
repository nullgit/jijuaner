<template>
    <van-tabbar v-model="activeNav" class="footer">
        <van-tabbar-item
            v-for="navItem of nav"
            :name="navItem.name"
            :key="navItem.name"
            :icon="navItem.icon"
            :to="navItem.to"
        >
            {{ navItem.name }}
        </van-tabbar-item>
    </van-tabbar>
</template>

<script>
export default {
    name: "Footer",
    props: ["activeNavProp"],
    data() {
        return {
            activeNav: this.activeNavProp,
            nav: [
                { icon: "home-o", to: "/", name: "主页" },
                { icon: "star-o", to: "option", name: "自选" },
                { icon: "user-circle-o", to: "my", name: "我的" },
            ],
        }
    },
}
</script>
