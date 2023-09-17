<template>
  <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
    <el-form-item label="源文件地址" prop="srcFile">
      <el-input v-model="ruleForm.srcFile"></el-input>
    </el-form-item>
    <el-form-item label="上传地址" prop="destFile">
      <el-input v-model="ruleForm.destFile"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm('ruleForm')">上传</el-button>
      <el-button @click="resetForm('ruleForm')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  data () {
    return {
      ruleForm: {
        srcFile: '',
        destFile: ''
      },
      rules: {
        srcFile: [
          {required: true, message: '请输入在本地的源文件地址', trigger: 'blur'}
        ],
        destFile: [
          {required: true, message: '请输入在HDFS的目标地址', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$http.get('http://localhost:8081/hadoop/upload',
            {params: {srcFile: this.ruleForm.srcFile, destFile: this.ruleForm.destFile}})
            .then(resp => {
              if (resp.data === 'success') {
                this.$alert('上传成功！', '消息', {
                  confirmButtonText: '确定',
                  callback: action => {
                    this.$router.push('/Home')
                  }
                })
              }
            })
        } else {
          return false
        }
      })
    },
    resetForm (formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>
