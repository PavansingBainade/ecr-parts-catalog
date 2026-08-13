<template>
  <div class="detail-page">

    <!-- Top bar -->
    <div class="top-bar">
      <RouterLink to="/" class="back-link">
        ← Back to ECRs
      </RouterLink>

      <span v-if="ecr" class="ecr-number">
        ECR #{{ ecr.id }}
      </span>
    </div>

    <!-- ECR exists -->
    <div v-if="ecr">

      <!-- Header -->
      <div class="page-header">

        <div>
          <div class="title-row">
            <h1>{{ ecr.title }}</h1>

            <span
              class="status-badge"
              :class="statusClass(ecr.status)"
            >
              {{ ecr.status }}
            </span>

            <span class="priority-badge">
              {{ ecr.priority }}
            </span>
          </div>

          <p class="description">
            {{ ecr.description }}
          </p>
        </div>

      </div>


      <!-- Information cards -->
      <div class="info-grid">

        <div class="info-card">
          <span class="info-label">ECR ID</span>
          <strong>#{{ ecr.id }}</strong>
        </div>

        <div class="info-card">
          <span class="info-label">Requested By</span>
          <strong>{{ ecr.requestedBy || 'Not specified' }}</strong>
        </div>

        <div class="info-card">
          <span class="info-label">Date Created</span>
          <strong>{{ ecr.dateCreated || 'Not specified' }}</strong>
        </div>

        <div class="info-card">
          <span class="info-label">Priority</span>
          <strong>{{ ecr.priority }}</strong>
        </div>

      </div>


      <!-- Workflow -->
      <div class="section-card">

        <h2>Status Workflow</h2>

        <div class="workflow">

          <div
            class="workflow-step"
            :class="{ active: ecr.status === 'Draft' }"
          >
            <div class="step-circle">1</div>
            <span>Draft</span>
          </div>

          <div class="workflow-line"></div>

          <div
            class="workflow-step"
            :class="{ active: ecr.status === 'InReview' }"
          >
            <div class="step-circle">2</div>
            <span>In Review</span>
          </div>

          <div class="workflow-line"></div>

          <div
            class="workflow-step"
            :class="{ active: ecr.status === 'Approved' }"
          >
            <div class="step-circle">3</div>
            <span>Approved</span>
          </div>

          <div class="workflow-line"></div>

          <div
            class="workflow-step rejected"
            :class="{ active: ecr.status === 'Rejected' }"
          >
            <div class="step-circle">!</div>
            <span>Rejected</span>
          </div>

        </div>

      </div>


      <!-- Status actions -->
      <div class="section-card">

        <h2>Change Status</h2>

        <p class="section-description">
          Update the ECR status using the workflow rules configured in the
          backend.
        </p>

        <div class="action-buttons">

          <button
  v-if="canTransitionTo('InReview')"
  class="btn btn-review"
  @click="changeStatus('InReview')"
  :disabled="updating"
>
  Move to InReview
</button>

<button
  v-if="canTransitionTo('Approved')"
  class="btn btn-approve"
  @click="changeStatus('Approved')"
  :disabled="updating"
>
  Approve
</button>

<button
  v-if="canTransitionTo('Rejected')"
  class="btn btn-reject"
  @click="changeStatus('Rejected')"
  :disabled="updating"
>
  Reject
</button>

<button
  v-if="canTransitionTo('Draft')"
  class="btn btn-review"
  @click="changeStatus('Draft')"
  :disabled="updating"
>
  Move back to Draft
</button>

        </div>


        <div v-if="updating" class="message loading-message">
          Updating status...
        </div>

        <div v-if="updateError" class="message error-message">
          {{ updateError }}
        </div>

      </div>

    </div>


    <!-- ECR not found -->
    <div v-else class="not-found">

      <h2>ECR not found</h2>

      <p>
        The requested ECR could not be found.
      </p>

      <RouterLink to="/" class="back-button">
        Back to ECR List
      </RouterLink>

    </div>

  </div>
</template>


<script setup>

import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useEcrStore } from '../stores/ecrStore'

const route = useRoute()
const ecrStore = useEcrStore()

const updating = ref(false)
const updateError = ref(null)

const ecr = computed(() => {

  const id = Number(route.params.id)

  return ecrStore.ecrs.find(
    item => Number(item.id) === id
  )

})


onMounted(async () => {

  const id = Number(route.params.id)

  const existingEcr = ecrStore.ecrs.find(
    item => Number(item.id) === id
  )

  if (!existingEcr) {
    await ecrStore.fetchECRs()
  }

})

function canTransitionTo(status) {

  if (!ecr.value) {
    return false
  }

  const transitions = {

    Draft: [
      'InReview'
    ],

    InReview: [
      'Approved',
      'Rejected'
    ],

    Rejected: [
      'Draft'
    ],

    Approved: []

  }

  return transitions[ecr.value.status]?.includes(status) ?? false

}

function statusClass(status) {

  return {
    Draft: 'status-draft',
    InReview: 'status-review',
    Approved: 'status-approved',
    Rejected: 'status-rejected'
  }[status] || ''

}

async function changeStatus(newStatus) {

  if (!ecr.value) {
    return
  }

  updating.value = true
  updateError.value = null

  try {

    await ecrStore.updateStatus(
      ecr.value.id,
      newStatus
    )

  } catch (err) {

    console.error(err)

    updateError.value =
      err.response?.data?.error ||
      'Failed to update status'

  } finally {

    updating.value = false

  }

}

</script>

<style scoped>

.detail-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 32px 50px;
  color: #1f2937;
}


/* Top bar */

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.back-link {
  text-decoration: none;
  color: #475569;
  font-weight: 600;
}

.back-link:hover {
  color: #2563eb;
}

.ecr-number {
  font-weight: 700;
  color: #64748b;
}


/* Header */

.page-header {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.06);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  font-size: 30px;
  color: #111827;
}

.description {
  margin-top: 12px;
  color: #64748b;
  font-size: 16px;
}


/* Badges */

.status-badge,
.priority-badge {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.status-draft {
  background: #fef3c7;
  color: #92400e;
}

.status-review {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-approved {
  background: #dcfce7;
  color: #166534;
}

.status-rejected {
  background: #fee2e2;
  color: #991b1b;
}

.priority-badge {
  background: #f1f5f9;
  color: #475569;
}


/* Information */

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.info-card {
  background: white;
  padding: 22px;
  border-radius: 14px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.05);
}

.info-label {
  display: block;
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 8px;
}

.info-card strong {
  font-size: 16px;
}


/* Sections */

.section-card {
  background: white;
  padding: 28px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.05);
}

.section-card h2 {
  margin-top: 0;
  margin-bottom: 8px;
}

.section-description {
  color: #64748b;
}


/* Workflow */

.workflow {
  display: flex;
  align-items: center;
  margin-top: 30px;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-weight: 600;
  min-width: 90px;
}

.step-circle {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  color: #64748b;
  font-weight: 700;
}

.workflow-step.active {
  color: #2563eb;
}

.workflow-step.active .step-circle {
  background: #2563eb;
  color: white;
}

.workflow-step.rejected.active {
  color: #dc2626;
}

.workflow-step.rejected.active .step-circle {
  background: #dc2626;
}

.workflow-line {
  height: 2px;
  background: #e2e8f0;
  flex: 1;
  margin: 0 10px;
}


/* Buttons */

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn {
  border: none;
  padding: 12px 20px;
  border-radius: 9px;
  font-weight: 700;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-review {
  background: #dbeafe;
  color: #1d4ed8;
}

.btn-approve {
  background: #dcfce7;
  color: #166534;
}

.btn-reject {
  background: #fee2e2;
  color: #991b1b;
}


/* Messages */

.message {
  margin-top: 18px;
  padding: 12px 16px;
  border-radius: 8px;
}

.loading-message {
  background: #eff6ff;
  color: #1d4ed8;
}

.error-message {
  background: #fee2e2;
  color: #991b1b;
  font-weight: 600;
}


/* Not found */

.not-found {
  background: white;
  padding: 50px;
  border-radius: 16px;
  text-align: center;
}

.back-button {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 18px;
  background: #2563eb;
  color: white;
  text-decoration: none;
  border-radius: 8px;
}


/* Responsive */

@media (max-width: 900px) {

  .detail-page {
    padding: 24px;
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

}

@media (max-width: 600px) {

  .info-grid {
    grid-template-columns: 1fr;
  }

  .workflow {
    flex-direction: column;
    gap: 15px;
  }

  .workflow-line {
    width: 2px;
    height: 30px;
    flex: none;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

}

</style>