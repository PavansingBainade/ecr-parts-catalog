<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'
import StatusBadge from '../components/StatusBadge.vue'

const ecrList = ref([])
const loading = ref(true)
const error = ref('')

const selectedStatus = ref('ALL')

const filteredEcrs = () => {
  if (selectedStatus.value === 'ALL') {
    return ecrList.value
  }

  return ecrList.value.filter(
    ecr => ecr.status === selectedStatus.value
  )
}

onMounted(async () => {
  try {
    const response = await api.get('/ecrs')
    ecrList.value = response.data
  } catch (err) {
    error.value = 'Failed to load ECRs'
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h1>ECR List</h1>

    <p v-if="loading">Loading ECRs...</p>

    <p v-else-if="error">
      {{ error }}
    </p>
    <div>
    <label for="statusFilter">Filter by Status: </label>

    <select id="statusFilter" v-model="selectedStatus">
        <option value="ALL">All</option>
        <option value="Draft">Draft</option>
        <option value="InReview">InReview</option>
        <option value="Approved">Approved</option>
        <option value="Rejected">Rejected</option>
    </select>
    </div>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Title</th>
          <th>Status</th>
          <th>Priority</th>
          <th>Requested By</th>
          <th>Date Created</th>
        </tr>
      </thead>

      <tbody>
        <tr
            v-for="ecr in filteredEcrs()"
            :key="ecr.id"
        >
          <td>{{ ecr.id }}</td>
          <td>{{ ecr.title }}</td>
          <td><StatusBadge :status="ecr.status" /></td>
          <td>{{ ecr.priority }}</td>
          <td>{{ ecr.requestedBy }}</td>
          <td>{{ ecr.dateCreated }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>