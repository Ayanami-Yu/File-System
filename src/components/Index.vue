<template>
  <div>
    <el-container style="height: 500px; border: 1px solid #eee">
      <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
        <el-menu :default-active="this.$route.path" :router="true" router:default-openeds="['0']">
          <el-submenu
            v-for="(item, index) in $router.options.routes"
            :key="item.name"
            :index="index+''"
          >
            <template slot="title">
              <i class="el-icon-folder"></i>{{ item.name }}
            </template>
            <el-menu-item
              v-for="(item2) in item.children"
              :key="item2.name"
              :index="item2.path"
              :class="$route.path===item2.path?'is-active':''">{{ item2.name }}
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <el-container>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style>
.el-header {
  background-color: #B3C0D1;
  color: #333;
  line-height: 60px;
}

.el-aside {
  color: #333;
}
</style>
