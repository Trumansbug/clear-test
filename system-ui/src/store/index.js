import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: null,
    roles: JSON.parse(localStorage.getItem('roles')) || [],
    token: localStorage.getItem('token') || null
  },
  mutations: {
    SET_USER(state, data) {
      const { token, user } = data
      state.user = user
      state.roles = user ? user.roles : []
      if (user) {
        localStorage.setItem('token', token)
        localStorage.setItem('roles', JSON.stringify(user.roles))
      }
    },
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    CLEAR_USER(state) {
      state.user = null
      state.roles = []
      localStorage.removeItem('token')
      localStorage.removeItem('roles')
    }
  },
  actions: {
    setUser({ commit }, user) {
      commit('SET_USER', user)
    },
    clearUser({ commit }) {
      commit('CLEAR_USER')
    }
  },
  getters: {
    isAuthenticated: state => !!state.user,
    hasRole: state => role => Array.isArray(state.roles) && state.roles.includes(role)
  }
})