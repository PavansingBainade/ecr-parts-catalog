<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useEcrStore } from '../stores/ecrStore'
import StatusBadge from '../components/StatusBadge.vue'

const ecrStore = useEcrStore()

const selectedStatus = ref('ALL')
const searchQuery = ref('')

onMounted(() => {
  ecrStore.fetchECRs()
})

const filteredEcrs = computed(() => {

  let result = ecrStore.ecrs

  // Filter by status
  if (selectedStatus.value !== 'ALL') {
    result = result.filter(
      ecr => ecr.status === selectedStatus.value
    )
  }

  // Search
  if (searchQuery.value.trim()) {

    const query = searchQuery.value.toLowerCase()

    result = result.filter(ecr =>
      String(ecr.id).includes(query) ||
      ecr.title?.toLowerCase().includes(query) ||
      ecr.requestedBy?.toLowerCase().includes(query)
    )
  }

  return result
})

const totalCount = computed(() => ecrStore.ecrs.length)

const draftCount = computed(() =>
  ecrStore.ecrs.filter(
    ecr => ecr.status === 'Draft'
  ).length
)

const reviewCount = computed(() =>
  ecrStore.ecrs.filter(
    ecr => ecr.status === 'InReview'
  ).length
)

const approvedCount = computed(() =>
  ecrStore.ecrs.filter(
    ecr => ecr.status === 'Approved'
  ).length
)

const rejectedCount = computed(() =>
  ecrStore.ecrs.filter(
    ecr => ecr.status === 'Rejected'
  ).length
)

function clearFilters() {
  selectedStatus.value = 'ALL'
  searchQuery.value = ''
}
</script>

<template>

  <div class="app-shell">

    <!-- Sidebar -->

    <aside class="sidebar">

      <div class="brand">

        <div class="brand-icon">
          E
        </div>

        <div>
          <h2>ECR Tracker</h2>
          <span>Engineering Change</span>
        </div>

      </div>

      <nav class="navigation">

        <RouterLink
          to="/"
          class="nav-item active"
        >
          <span class="nav-icon">▦</span>
          Dashboard
        </RouterLink>

        <RouterLink
          to="/"
          class="nav-item"
        >
          <span class="nav-icon">▤</span>
          ECRs
        </RouterLink>

        <div class="nav-item disabled">
          <span class="nav-icon">⚙</span>
          Parts
        </div>

        <div class="nav-item disabled">
          <span class="nav-icon">♙</span>
          Suppliers
        </div>

      </nav>

      <div class="sidebar-bottom">

        <div class="system-status">
          <span class="status-dot"></span>

          <div>
            <strong>System Online</strong>
            <small>Backend connected</small>
          </div>
        </div>

      </div>

    </aside>


    <!-- Main content -->

    <main class="main-content">

      <!-- Header -->

      <header class="topbar">

        <div>

          <p class="breadcrumb">
            Engineering Change Management
          </p>

          <h1>ECR Dashboard</h1>

        </div>

        <RouterLink
          to="/ecrs/new"
          class="create-button"
        >
          <span>+</span>
          Create ECR
        </RouterLink>

      </header>


      <!-- Loading -->

      <div
        v-if="ecrStore.loading"
        class="state-message"
      >
        <div class="loader"></div>
        Loading ECRs...
      </div>


      <!-- Error -->

      <div
        v-else-if="ecrStore.error"
        class="error-message"
      >
        <strong>Unable to load ECRs</strong>
        <span>{{ ecrStore.error }}</span>
      </div>


      <template v-else>

        <!-- Statistics -->

        <section class="stats-grid">

          <div class="stat-card">

            <div class="stat-icon total">
              #
            </div>

            <div>
              <span class="stat-label">Total ECRs</span>
              <strong>{{ totalCount }}</strong>
            </div>

          </div>


          <div class="stat-card">

            <div class="stat-icon draft">
              ◷
            </div>

            <div>
              <span class="stat-label">Draft</span>
              <strong>{{ draftCount }}</strong>
            </div>

          </div>


          <div class="stat-card">

            <div class="stat-icon review">
              ◌
            </div>

            <div>
              <span class="stat-label">In Review</span>
              <strong>{{ reviewCount }}</strong>
            </div>

          </div>


          <div class="stat-card">

            <div class="stat-icon approved">
              ✓
            </div>

            <div>
              <span class="stat-label">Approved</span>
              <strong>{{ approvedCount }}</strong>
            </div>

          </div>


          <div class="stat-card">

            <div class="stat-icon rejected">
              ×
            </div>

            <div>
              <span class="stat-label">Rejected</span>
              <strong>{{ rejectedCount }}</strong>
            </div>

          </div>

        </section>


        <!-- ECR section -->

        <section class="ecr-section">

          <div class="section-header">

            <div>
              <h2>Engineering Change Requests</h2>

              <p>
                Manage and track engineering changes
              </p>
            </div>

          </div>


          <!-- Filters -->

          <div class="filters">

            <div class="search-box">

              <span>⌕</span>

              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search by ID, title or requester..."
              />

            </div>


            <div class="filter-group">

              <label for="statusFilter">
                Status
              </label>

              <select
                id="statusFilter"
                v-model="selectedStatus"
              >
                <option value="ALL">
                  All statuses
                </option>

                <option value="Draft">
                  Draft
                </option>

                <option value="InReview">
                  In Review
                </option>

                <option value="Approved">
                  Approved
                </option>

                <option value="Rejected">
                  Rejected
                </option>

              </select>

            </div>


            <button
              v-if="selectedStatus !== 'ALL' || searchQuery"
              class="clear-button"
              @click="clearFilters"
            >
              Clear
            </button>

          </div>


          <!-- Empty state -->

          <div
            v-if="filteredEcrs.length === 0"
            class="empty-state"
          >

            <div class="empty-icon">
              ⊘
            </div>

            <h3>No ECRs found</h3>

            <p>
              Try changing your filters or create a new ECR.
            </p>

            <RouterLink
              to="/ecrs/new"
              class="create-button small"
            >
              + Create ECR
            </RouterLink>

          </div>


          <!-- Table -->

          <div
            v-else
            class="table-wrapper"
          >

            <table>

              <thead>

                <tr>
                  <th>ECR</th>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Requested By</th>
                  <th>Created</th>
                </tr>

              </thead>


              <tbody>

                <tr
                  v-for="ecr in filteredEcrs"
                  :key="ecr.id"
                >

                  <td>

                    <RouterLink
                      :to="`/ecrs/${ecr.id}`"
                      class="ecr-id"
                    >
                      #{{ ecr.id }}
                    </RouterLink>

                  </td>


                  <td>

                    <RouterLink
                      :to="`/ecrs/${ecr.id}`"
                      class="title-link"
                    >
                      <strong>
                        {{ ecr.title }}
                      </strong>

                      <small>
                        {{ ecr.description || 'No description' }}
                      </small>
                    </RouterLink>

                  </td>


                  <td>
                    <StatusBadge
                      :status="ecr.status"
                    />
                  </td>


                  <td>

                    <span
                      class="priority"
                      :class="ecr.priority?.toLowerCase()"
                    >
                      {{ ecr.priority }}
                    </span>

                  </td>


                  <td>

                    <div class="requester">
                      <span class="avatar">
                        {{ ecr.requestedBy?.charAt(0) || '?' }}
                      </span>

                      {{ ecr.requestedBy || 'Unknown' }}
                    </div>

                  </td>


                  <td class="date">
                    {{ ecr.dateCreated || '—' }}
                  </td>

                </tr>

              </tbody>

            </table>

          </div>

        </section>

      </template>

    </main>

  </div>

</template>


<style scoped>

* {
  box-sizing: border-box;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
  color: #1f2937;
  font-family:
    Inter,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
}


/* Sidebar */

.sidebar {
  width: 245px;
  min-height: 100vh;
  background: #111827;
  color: white;
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 10px 32px;
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 20px;
}

.brand h2 {
  margin: 0;
  font-size: 16px;
}

.brand span {
  display: block;
  color: #9ca3af;
  font-size: 11px;
  margin-top: 3px;
}

.navigation {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.nav-item {
  color: #9ca3af;
  text-decoration: none;
  padding: 12px 13px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  transition: 0.2s;
}

.nav-item:hover {
  background: #1f2937;
  color: white;
}

.nav-item.active {
  background: #2563eb;
  color: white;
}

.nav-item.disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.nav-icon {
  width: 20px;
  text-align: center;
}

.sidebar-bottom {
  margin-top: auto;
  padding: 10px;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid #263244;
  border-radius: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
}

.system-status strong {
  display: block;
  font-size: 12px;
}

.system-status small {
  display: block;
  color: #9ca3af;
  font-size: 10px;
  margin-top: 2px;
}


/* Main */

.main-content {
  flex: 1;
  padding: 32px 40px;
  max-width: 1600px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.breadcrumb {
  color: #6b7280;
  font-size: 12px;
  margin: 0 0 5px;
}

.topbar h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.5px;
}

.create-button {
  background: #2563eb;
  color: white;
  text-decoration: none;
  border: none;
  border-radius: 8px;
  padding: 11px 17px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  transition: 0.2s;
}

.create-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.create-button.small {
  margin-top: 12px;
}


/* Stats */

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 26px;
}

.stat-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 19px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.stat-icon.total {
  background: #eff6ff;
  color: #2563eb;
}

.stat-icon.draft {
  background: #fff7ed;
  color: #ea580c;
}

.stat-icon.review {
  background: #f5f3ff;
  color: #7c3aed;
}

.stat-icon.approved {
  background: #f0fdf4;
  color: #16a34a;
}

.stat-icon.rejected {
  background: #fef2f2;
  color: #dc2626;
}

.stat-label {
  display: block;
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 4px;
}

.stat-card strong {
  font-size: 23px;
}


/* ECR section */

.ecr-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.section-header {
  padding: 23px 24px 18px;
}

.section-header h2 {
  margin: 0;
  font-size: 17px;
}

.section-header p {
  margin: 5px 0 0;
  color: #6b7280;
  font-size: 13px;
}


/* Filters */

.filters {
  padding: 0 24px 18px;
  display: flex;
  align-items: end;
  gap: 12px;
}

.search-box {
  flex: 1;
  max-width: 420px;
  height: 39px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  gap: 8px;
}

.search-box span {
  color: #9ca3af;
  font-size: 19px;
}

.search-box input {
  border: none;
  outline: none;
  width: 100%;
  font-size: 13px;
}

.filter-group label {
  display: block;
  font-size: 11px;
  color: #6b7280;
  margin-bottom: 5px;
}

.filter-group select {
  height: 39px;
  min-width: 145px;
  border: 1px solid #d1d5db;
  border-radius: 7px;
  padding: 0 10px;
  background: white;
  color: #374151;
}

.clear-button {
  height: 39px;
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font-size: 13px;
}


/* Table */

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f9fafb;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #e5e7eb;
}

th {
  text-align: left;
  padding: 12px 20px;
  color: #6b7280;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

td {
  padding: 15px 20px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}

tbody tr {
  transition: background 0.15s;
}

tbody tr:hover {
  background: #f8fafc;
}

.ecr-id {
  color: #2563eb;
  font-weight: 700;
  text-decoration: none;
}

.title-link {
  text-decoration: none;
  color: #1f2937;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-link strong {
  font-size: 13px;
}

.title-link small {
  color: #9ca3af;
  font-size: 11px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.priority {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 5px;
}

.priority.high {
  color: #dc2626;
  background: #fef2f2;
}

.priority.medium {
  color: #d97706;
  background: #fffbeb;
}

.priority.low {
  color: #2563eb;
  background: #eff6ff;
}

.requester {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 27px;
  height: 27px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #4338ca;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.date {
  color: #6b7280;
  white-space: nowrap;
}


/* States */

.state-message {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  color: #6b7280;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid #dbeafe;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  border-radius: 9px;
  padding: 18px;
}

.error-message strong {
  display: block;
  margin-bottom: 4px;
}

.error-message span {
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  border-top: 1px solid #f1f5f9;
}

.empty-icon {
  font-size: 35px;
  color: #9ca3af;
}

.empty-state h3 {
  margin: 12px 0 5px;
}

.empty-state p {
  color: #6b7280;
  font-size: 13px;
}


/* Responsive */

@media (max-width: 1100px) {

  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }

}

@media (max-width: 800px) {

  .sidebar {
    width: 70px;
    padding: 20px 10px;
  }

  .brand div:not(.brand-icon),
  .nav-item:not(.active)::after,
  .nav-item {
    font-size: 0;
  }

  .brand {
    justify-content: center;
    padding-left: 0;
    padding-right: 0;
  }

  .nav-item {
    justify-content: center;
  }

  .main-content {
    padding: 24px 18px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .topbar {
    align-items: flex-start;
    gap: 15px;
  }

}

@media (max-width: 550px) {

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

}
</style>