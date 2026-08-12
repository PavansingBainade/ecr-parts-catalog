import { createRouter, createWebHistory } from 'vue-router'
import ECRListView from '../views/ECRListView.vue'
import ECRDetailView from '../views/ECRDetailView.vue'
import ECRForm from '../views/ECRForm.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      name: 'ecr-list',
      component: ECRListView
    },
    {
      path: '/ecrs/:id',
      name: 'ecr-detail',
      component: ECRDetailView
    },
    {
      path: '/ecrs/new',
      name: 'ecr-create',
      component: ECRForm
    }
  ]
})

export default router