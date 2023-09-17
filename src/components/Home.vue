<template>
  <div>
    <el-table
      :data="tableData"
      border
      style="width: 100%">
      <el-table-column
        fixed
        prop="filePath"
        label="文件目录"
        width="300">
      </el-table-column>
      <el-table-column
        label="操作"
        width="450">
        <template slot-scope="scope">
          <el-button @click="download(scope.row)" plain icon="el-icon-download" size="small">下载</el-button>
          <el-button @click="deleteFile(scope.row)" plain icon="el-icon-delete" size="small">删除</el-button>
          <template v-if="scope.row.isDir">
            <el-button @click="listFile(scope.row)" plain icon="el-icon-folder-opened" size="small">打开文件夹</el-button>
          </template>
          <el-button @click="wordCount(scope.row)" type="text" size="small">单词统计</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  methods: {
    download (filePath) {
      filePath = filePath.filePath
      this.$prompt('请输入本地存储路径（含文件名）', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(savePath => {
        this.$message({
          type: 'success',
          message: '文件下载到: ' + savePath.value
        })
        this.$http.get('http://localhost:8081/hadoop/download',
          {params: {filePath: filePath, savePath: savePath.value}})
          .then(resp => {
            this.$alert('下载成功！', '消息', {
              confirmButtonText: '确定',
              callback: action => {
                window.location.reload()
              }
            })
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消输入'
        })
      })
    },
    deleteFile (path) {
      path = path.filePath
      this.$http.delete('http://localhost:8081/hadoop/delFile', {params: {filePath: path}})
        .then(resp => {
          this.$alert('删除成功！', '消息', {
            confirmButtonText: '确定',
            callback: action => {
              window.location.reload()
            }
          })
        })
    },
    listFile (path) {
      path = path.filePath
      this.$http.get('http://localhost:8081/hadoop/listFiles', {params: {path: path}})
        .then(resp => {
          this.tableData = resp.data
        })
    },
    wordCount (path) {
      path = path.filePath
      this.$prompt('请输入统计结果在本地的存储路径（含文件名）', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(savePath => {
        this.$http.get('http://localhost:8081/hadoop/wordCount',
          {params: {srcPath: path, savePath: savePath.value}})
          .then(resp => {
            this.$alert('存储成功！', '消息', {
              confirmButtonText: '确定',
              callback: action => {
                window.location.reload()
              }
            })
          })
      })
    }
  },
  data () {
    return {
      tableData: []
    }
  },
  created () {
    this.$http.get('http://localhost:8081/hadoop/listFiles', {params: {path: '/user'}})
      .then(resp => {
        // console.log(resp)
        this.tableData = resp.data
      })
  }
}
</script>
