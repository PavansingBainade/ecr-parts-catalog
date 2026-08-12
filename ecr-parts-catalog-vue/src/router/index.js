import { createRouter, createWebHistory } from 'vue-router'
import ECRListView from '../views/ECRListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      name: 'ecr-list',
      component: ECRListView
    }
  ]
})

export default router