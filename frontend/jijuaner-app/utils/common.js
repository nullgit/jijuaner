const { default: axios } = require("axios")
const dayjs = require("dayjs")

exports.handleReturn = () => {
    history.back()
}

exports.dateFormatter = (time) => {
    return dayjs(time).format("YYYY年MM月DD日 HH:mm:ss")
}

let getUserInfos = (Vue, userInfos, ids) => {
    axios
        .post(`/api/user/userList/getUserInfos`, ids)
        .then(({ data }) => {
            for (let userInfo of data.data) {
                if (userInfo.headImg == null) {
                    userInfo.headImg = "/img/defaultHeadImg.png"
                }
                Vue.set(userInfos, userInfo.userId, userInfo)
            }
        })
        .catch(console.log)
}
exports.getUserInfos = getUserInfos
