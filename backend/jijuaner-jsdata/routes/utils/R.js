class R {
    constructor() {
        this.msg = ''
        this.data = null
    }

    ok() {
        this.code = 0
        return this
    }

    error() {
        this.code = -1
        return this
    }

    putCode(code) {
        this.code = code
        return this
    }

    putData(data) {
        this.data = data
        return this
    }

    putMsg(msg) {
        this.msg = msg
        return this
    }
}

exports.R = R