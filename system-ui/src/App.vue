<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  mounted() {
    document.title = '乘风 1.0';
  }
}
</script>

<style>
bady {
  font-family: "Microsoft YaHei",serif;
}
#app {
  height: 100%;
}
</style> 