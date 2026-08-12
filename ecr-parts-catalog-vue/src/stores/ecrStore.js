import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useEcrStore = defineStore('ecr', () => {

  const ecrs = ref([])
  const loading = ref(false)
  const error = ref(null)

  const draftCount = computed(() => {
    return ecrs.value.filter(
      ecr => ecr.status === 'Draft'
    ).length
  })

  async function fetchECRs() {

    loading.value = true
    error.value = null

    try {

      const response = await api.get('/ecrs')

      ecrs.value = response.data

    } catch (err) {

      error.value = 'Failed to load ECRs'
      console.error(err)

    } finally {

      loading.value = false
    }
  }

  async function updateStatus(id, newStatus) {

    try {

      const response = await api.put(
        `/ecrs/${id}/status`,
        {
          status: newStatus
        }
      )

      const index = ecrs.value.findIndex(
        ecr => ecr.id === id
      )

      if (index !== -1) {
        ecrs.value[index] = response.data
      }

      return response.data

    } catch (err) {

      console.error(
        'Failed to update status:',
        err
      )

      throw err
    }
  }

  return {
    ecrs,
    loading,
    error,
    draftCount,
    fetchECRs,
    updateStatus
  }
})