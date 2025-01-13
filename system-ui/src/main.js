import Vue from 'vue';
import moment from 'moment';
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.use(ElementUI)
Vue.config.productionTip = false

Vue.prototype.$formatTime = function(cellValue) {
  if (!cellValue) {
    return '';
  }
  return moment(cellValue).format('YYYY-MM-DD HH:mm:ss');
};

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app') 