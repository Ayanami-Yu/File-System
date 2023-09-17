import Vue from 'vue'
import Router from 'vue-router'
import Home from '../components/Home.vue'
import UploadFile from '../components/UploadFile.vue'
import Index from '../components/Index.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: '文件系统',
      component: Index,
      redirect: '/Home',
      children: [
        {
          path: '/Home',
          name: '文件目录',
          component: Home
        },
        {
          path: '/UploadFile',
          name: '上传文件',
          component: UploadFile
        }
      ]
    }
  ]
})
