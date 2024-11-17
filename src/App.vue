<template>
  <div id="app">
    <!-- This is the entry point for your Vue app -->
    <GameComponent />
  </div>
</template>

<script>
import GameComponent from './components/GameComponent.vue';

export default {
  components: {
    GameComponent,
  },
};
</script>

<style>
@import './assets/css/style.css';
</style>
