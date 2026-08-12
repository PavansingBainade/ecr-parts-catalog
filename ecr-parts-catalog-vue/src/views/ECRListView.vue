<script setup>
import { ref, computed, onMounted } from 'vue'
import { useEcrStore } from '../stores/ecrStore'
import StatusBadge from '../components/StatusBadge.vue'
import { RouterLink } from 'vue-router'

const ecrStore = useEcrStore()

const selectedStatus = ref('ALL')

onMounted(() => {
  ecrStore.fetchECRs()
})

const filteredEcrs = computed(() => {
  if (selectedStatus.value === 'ALL') {
    return ecrStore.ecrs
  }

  return ecrStore.ecrs.filter(
    ecr => ecr.status === selectedStatus.value
  )
})
</script>

<template>

  <h1>ECR List</h1>

  <p v-if="ecrStore.loading">
    Loading ECRs...
  </p>

  <p v-else-if="ecrStore.error">
    {{ ecrStore.error }}
  </p>

  <div v-else>

    <div>
      <label for="statusFilter">
        Filter by Status:
      </label>

      <select
        id="statusFilter"
        v-model="selectedStatus"
      >
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
          v-for="ecr in filteredEcrs"
          :key="ecr.id"
        >
          <td>
            <RouterLink :to="`/ecrs/${ecr.id}`">
              {{ ecr.id }}
            </RouterLink>
          </td>

          <td>{{ ecr.title }}</td>

          <td>
            <StatusBadge :status="ecr.status" />
          </td>

          <td>{{ ecr.priority }}</td>

          <td>{{ ecr.requestedBy }}</td>

          <td>{{ ecr.dateCreated }}</td>
        </tr>

      </tbody>

    </table>

  </div>

</template>