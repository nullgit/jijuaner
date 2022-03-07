import Vue from "vue"
import {
    Notify,
    Dialog,
    Field,
    NavBar,
    ActionSheet,
    Tab,
    Tabs,
    Tabbar,
    TabbarItem,
    Button,
    Grid,
    GridItem,
    Image as VanImage,
    Cell,
    CellGroup,
    Swipe, SwipeItem, Uploader,
    Popup, Badge, Icon, List
} from "vant"

// 目前在 nuxt 中无法按需引入样式，因此采用手动引入的方式
import "vant/lib/index.css"

Vue.use(Notify)
    .use(Dialog)
    .use(Field)
    .use(NavBar)
    .use(ActionSheet)
    .use(Tab)
    .use(Tabs)
    .use(Tabbar)
    .use(TabbarItem)
    .use(Button)
    .use(Grid)
    .use(GridItem)
    .use(VanImage)
    .use(Cell)
    .use(CellGroup)
    .use(Swipe).use(SwipeItem).use(Uploader)
    .use(Popup).use(Badge).use(Icon).use(List)
