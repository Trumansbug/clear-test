<template>
  <div class="login-container">
    <canvas id="waterCanvas" class="water-canvas"></canvas>
    <div class="login-box">
      <div class="title">乘风 1.0</div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      },
      particles: [],
      mouse: {
        x: 0,
        y: 0
      }
    }
  },
  mounted() {
    this.initWaterWave()
  },
  beforeUnmount() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        
        this.loading = true
        try {
          const res = await login(this.loginForm)
          await this.$store.dispatch('setUser', res.data)
          this.$router.push('/')
        } catch (error) {
          this.$message.error(error.message)
        } finally {
          this.loading = false
        }
      })
    },
    handleMouseMove(e) {
      this.mouse.x = e.clientX
      this.mouse.y = e.clientY
    },
    initWaterWave() {
      const canvas = document.getElementById('waterCanvas')
      const ctx = canvas.getContext('2d')
      
      // 设置canvas尺寸为窗口大小
      const resizeCanvas = () => {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      }
      
      resizeCanvas()
      window.addEventListener('resize', resizeCanvas)

      // 波浪参数
      const waves = [
        {
          y: 0.3,     // 波浪在画布的垂直位置
          length: 0.5, // 波长
          amplitude: 20, // 振幅
          frequency: 0.02, // 频率
          color: 'rgba(255, 255, 255, 0.2)'
        },
        {
          y: 0.4,
          length: 0.3,
          amplitude: 30,
          frequency: 0.015,
          color: 'rgba(255, 255, 255, 0.3)'
        },
        {
          y: 0.5,
          length: 0.5,
          amplitude: 25,
          frequency: 0.01,
          color: 'rgba(255, 255, 255, 0.1)'
        }
      ]

      let step = 0

      const drawWave = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        
        waves.forEach(wave => {
          ctx.beginPath()
          ctx.moveTo(0, wave.y * canvas.height)
          
          // 绘制波浪
          for (let x = 0; x <= canvas.width; x++) {
            const dx = x * wave.length / canvas.width
            const dy = Math.sin(dx * Math.PI * 2 + step * wave.frequency)
            const y = wave.y * canvas.height + dy * wave.amplitude
            
            if (x === 0) {
              ctx.moveTo(x, y)
            } else {
              ctx.lineTo(x, y)
            }
          }
          
          // 填充波浪
          ctx.lineTo(canvas.width, canvas.height)
          ctx.lineTo(0, canvas.height)
          ctx.closePath()
          
          // 渐变填充
          const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height)
          gradient.addColorStop(0, wave.color)
          gradient.addColorStop(1, 'rgba(255, 255, 255, 0)')
          ctx.fillStyle = gradient
          ctx.fill()
        })
        
        step += 1
        this.animationFrameId = requestAnimationFrame(drawWave)
      }
      
      drawWave()
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #1e90ff, #70a1ff);
  overflow: hidden;
}

.water-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.login-box {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 400px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 1;
  
  .title {
    font-size: 24px;
    color: #303133;
    text-align: center;
    margin-bottom: 30px;
    font-weight: bold;
  }

  :deep(.el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.8);
  }

  :deep(.el-button) {
    width: 100%;
    border-radius: 4px;
    margin-top: 10px;
    background: linear-gradient(45deg, #1e90ff, #70a1ff);
    border: none;
    
    &:hover {
      background: linear-gradient(45deg, #70a1ff, #1e90ff);
    }
  }

  .el-form-item {
    margin-bottom: 25px;
  }
}
</style> 