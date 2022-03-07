<template>
    <div>
        <img :src="`http://j4.dfcfw.com/charts/pic6/000689.png?v=${new Date().getTime()}`" alt="">
    </div>
</template>

<style>
</style>

<script>
import axios from "axios"

export default {
    methods: {

    },
    data() {
        return {

        }
    },
    mounted() {
        // axios
        //     .get(`http://j4.dfcfw.com/charts/pic6/000689.png?v=${new Date().getTime()}`)
        //     .then((resp) => {
        //         console.log(resp)
        //     })
        //     .catch(console.log)
    }
}
</script>
