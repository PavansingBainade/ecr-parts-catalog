<script setup>
import { computed, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useEcrStore } from '../stores/ecrStore'

const route = useRoute()
const ecrStore = useEcrStore()

const ecrId = Number(route.params.id)

const ecr = computed(() => {
  return ecrStore.ecrs.find(
    ecr => ecr.id === ecrId
  )
})

const updating = ref(false)
const updateError = ref(null)

async function changeStatus(newStatus) {

  //alert('Button clicked: ' + newStatus)

  updating.value = true
  updateError.value = null

  try {

    console.log('Sending status update:', {
      id: ecrId,
      status: newStatus
    })

    const result = await ecrStore.updateStatus(
      ecrId,
      newStatus
    )

    console.log('Status update successful:', result)

  } catch (err) {

    console.error('Status update failed:', err)

    console.error('Response:', err.response)

    updateError.value =
      err.response?.data?.error ||
      err.message ||
      'Failed to update status'

  } finally {

    updating.value = false
  }
}
</script>

<template>

  <div v-if="ecr">

    <h1>ECR Details</h1>

    <p>
      <strong>ID:</strong>
      {{ ecr.id }}
    </p>

    <p>
      <strong>Title:</strong>
      {{ ecr.title }}
    </p>

    <p>
      <strong>Description:</strong>
      {{ ecr.description }}
    </p>

    <p>
      <strong>Status:</strong>
      {{ ecr.status }}
    </p>

    <p>
      <strong>Priority:</strong>
      {{ ecr.priority }}
    </p>

    <p>
      <strong>Requested By:</strong>
      {{ ecr.requestedBy }}
    </p>

    <p>
      <strong>Date Created:</strong>
      {{ ecr.dateCreated }}
    </p>


    <h2>Change Status</h2>

    <button
      @click="changeStatus('InReview')"
      :disabled="updating"
    >
      Move to InReview
    </button>

    <button
      @click="changeStatus('Approved')"
      :disabled="updating"
    >
      Approve
    </button>

    <button
      @click="changeStatus('Rejected')"
      :disabled="updating"
    >
      Reject
    </button>


    <p v-if="updating">
      Updating status...
    </p>

    <p v-if="updateError">
      {{ updateError }}
    </p>


    <br><br>

    <RouterLink to="/">
      Back to ECR List
    </RouterLink>

  </div>


  <div v-else>

    <h2>ECR not found</h2>

    <RouterLink to="/">
      Back to ECR List
    </RouterLink>

  </div>

</template>