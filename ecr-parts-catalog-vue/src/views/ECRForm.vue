<template>

  <div class="form-page">

    <div class="form-header">
      <RouterLink to="/" class="back-link">
        ← Back to ECRs
      </RouterLink>

      <h1>Create ECR</h1>

      <p>
        Create a new Engineering Change Request
      </p>
    </div>


    <div class="form-card">

      <form @submit.prevent="submitForm">

        <!-- Title -->

        <div class="form-group">

          <label for="title">
            Title <span>*</span>
          </label>

          <input
            id="title"
            v-model="title"
            type="text"
            placeholder="Enter ECR title"
            :class="{ invalid: titleError }"
          />

          <small v-if="titleError" class="field-error">
            {{ titleError }}
          </small>

        </div>
        <div class="form-group">

            <label for="requestedBy">
                Requested By <span>*</span>
            </label>

            <input
                id="requestedBy"
                v-model="requestedBy"
                type="text"
                placeholder="Enter creator name"
            />

        </div>

        <!-- Description -->

        <div class="form-group">

          <label for="description">
            Description
          </label>

          <textarea
            id="description"
            v-model="description"
            rows="5"
            placeholder="Describe the engineering change..."
          ></textarea>

        </div>


        <!-- Priority -->

        <div class="form-group">

          <label for="priority">
            Priority
          </label>

          <select
            id="priority"
            v-model="priority"
          >

            <option value="LOW">
              Low
            </option>

            <option value="MEDIUM">
              Medium
            </option>

            <option value="HIGH">
              High
            </option>

          </select>

        </div>


        <!-- Initial status -->

        <div class="info-box">

          <strong>Initial Status</strong>

          <span class="draft-badge">
            Draft
          </span>

          <p>
            Every new ECR automatically starts in Draft.
          </p>

        </div>


        <!-- Backend error -->

        <div
          v-if="error"
          class="error-message"
        >
          {{ error }}
        </div>


        <!-- Buttons -->

        <div class="form-actions">

          <RouterLink
            to="/"
            class="cancel-button"
          >
            Cancel
          </RouterLink>

          <button
            type="submit"
            class="submit-button"
            :disabled="submitting"
          >
            {{ submitting ? 'Creating...' : 'Create ECR' }}
          </button>

        </div>

      </form>

    </div>

  </div>

</template>


<script setup>

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'

const router = useRouter()

const title = ref('')
const description = ref('')
const priority = ref('MEDIUM')
const requestedBy = ref('')
const submitting = ref(false)
const error = ref(null)
const titleError = ref(null)


async function submitForm() {

  titleError.value = null
  error.value = null

  // Frontend validation
  if (!title.value.trim()) {

    titleError.value = 'Title is required'

    return
  }

  submitting.value = true

  try {

    const response = await api.post('/ecrs', {

  title: title.value.trim(),

  description: description.value.trim(),

  priority: priority.value,

  requestedBy: requestedBy.value.trim()

})

    console.log('ECR created:', response.data)

    // Return to dashboard
    router.push('/')

  } catch (err) {

    console.error(err)

    if (err.response?.data?.error) {

      error.value = err.response.data.error

    } else {

      error.value =
        'Failed to create ECR. Please try again.'

    }

  } finally {

    submitting.value = false

  }

}

</script>


<style scoped>

.form-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 40px 50px;
}

.form-header {
  max-width: 800px;
  margin: 0 auto 25px;
}

.back-link {
  text-decoration: none;
  color: #475569;
  font-weight: 600;
}

.back-link:hover {
  color: #2563eb;
}

.form-header h1 {
  margin: 25px 0 5px;
  font-size: 32px;
  color: #111827;
}

.form-header p {
  margin: 0;
  color: #64748b;
}

.form-card {
  max-width: 800px;
  margin: auto;
  background: white;
  padding: 35px;
  border-radius: 16px;
  box-shadow: 0 5px 20px rgba(15, 23, 42, 0.07);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 700;
  color: #334155;
}

.form-group label span {
  color: #dc2626;
}

input,
textarea,
select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 15px;
  font-family: inherit;
  outline: none;
}

input:focus,
textarea:focus,
select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

input.invalid {
  border-color: #dc2626;
}

textarea {
  resize: vertical;
}

.field-error {
  display: block;
  margin-top: 6px;
  color: #dc2626;
}

.info-box {
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  margin-bottom: 24px;
}

.info-box strong {
  margin-right: 10px;
}

.info-box p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.draft-badge {
  background: #fef3c7;
  color: #92400e;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.error-message {
  padding: 12px 15px;
  margin-bottom: 20px;
  border-radius: 8px;
  background: #fee2e2;
  color: #991b1b;
  font-weight: 600;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
}

.cancel-button,
.submit-button {
  padding: 12px 22px;
  border-radius: 8px;
  font-weight: 700;
  text-decoration: none;
  cursor: pointer;
  font-size: 14px;
}

.cancel-button {
  background: #f1f5f9;
  color: #475569;
}

.submit-button {
  border: none;
  background: #2563eb;
  color: white;
}

.submit-button:hover {
  background: #1d4ed8;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 600px) {

  .form-page {
    padding: 25px 15px;
  }

  .form-card {
    padding: 25px 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-button,
  .submit-button {
    text-align: center;
    width: 100%;
    box-sizing: border-box;
  }

}

</style>