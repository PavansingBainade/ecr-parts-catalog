<template>
  <span
    class="status-badge"
    :class="statusClass"
  >
    {{ status }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusClass = computed(() => {
  switch (props.status) {
    case 'Draft':
      return 'draft'

    case 'InReview':
      return 'in-review'

    case 'Approved':
      return 'approved'

    case 'Rejected':
      return 'rejected'

    default:
      return ''
  }
})
</script>

<style scoped>
.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: bold;
}

.draft {
  background-color: #fff3cd;
  color: #856404;
}

.in-review {
  background-color: #cfe2ff;
  color: #084298;
}

.approved {
  background-color: #d1e7dd;
  color: #0f5132;
}

.rejected {
  background-color: #f8d7da;
  color: #842029;
}
</style>