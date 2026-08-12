<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'

const router = useRouter()

const title = ref('')
const description = ref('')
const priority = ref('MEDIUM')

const error = ref(null)
const submitting = ref(false)

async function submitForm() {

  // Basic validation
  if (!title.value.trim()) {
    error.value = 'Title is required'
    return
  }

  error.value = null
  submitting.value = true

  try {

    await api.post('/ecrs', {
      title: title.value,
      description: description.value,
      priority: priority.value,
      status: 'Draft',
      requestedBy: 'Pavan'
    })

    // Go back to ECR list after successful creation
    router.push('/')

  } catch (err) {

    console.error('Failed to create ECR:', err)

    error.value =
      err.response?.data?.error ||
      'Failed to create ECR'

  } finally {

    submitting.value = false
  }
}
</script>

<template>

  <div>

    <h1>Create ECR</h1>

    <p v-if="error">
      {{ error }}
    </p>

    <form @submit.prevent="submitForm">

      <div>
        <label for="title">Title</label>

        <input
          id="title"
          v-model="title"
          type="text"
        />
      </div>

      <div>
        <label for="description">Description</label>

        <textarea
          id="description"
          v-model="description"
        ></textarea>
      </div>

      <div>
        <label for="priority">Priority</label>

        <select
          id="priority"
          v-model="priority"
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
      </div>

      <button
        type="submit"
        :disabled="submitting"
      >
        {{ submitting ? 'Creating...' : 'Create ECR' }}
      </button>

    </form>

  </div>

</template>