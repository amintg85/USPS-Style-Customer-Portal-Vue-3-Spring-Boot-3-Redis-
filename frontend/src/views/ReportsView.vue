<template>
  <div class="container">
    <h1>Reports & Statistics</h1>
    
    <div class="card">
      <h2>Overall Statistics</h2>
      <button @click="loadStatistics" class="btn btn-secondary" :disabled="loadingStats">
        {{ loadingStats ? 'Loading...' : 'Refresh Statistics' }}
      </button>
      
      <div v-if="statistics" class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ statistics.totalShipments }}</div>
          <div class="stat-label">Total Shipments</div>
        </div>
        
        <div v-for="(count, status) in statistics.statusCounts" :key="status" class="stat-card">
          <div class="stat-value">{{ count }}</div>
          <div class="stat-label">{{ formatStatus(status) }}</div>
        </div>
      </div>
      
      <div v-if="statsError" class="error">{{ statsError }}</div>
    </div>
    
    <div class="card">
      <h2>Shipment Report</h2>
      <form @submit.prevent="generateReport" class="report-form">
        <div class="form-row">
          <div class="form-group">
            <label for="startDate">Start Date</label>
            <input
              id="startDate"
              v-model="startDate"
              type="datetime-local"
              required
            />
          </div>
          <div class="form-group">
            <label for="endDate">End Date</label>
            <input
              id="endDate"
              v-model="endDate"
              type="datetime-local"
              required
            />
          </div>
        </div>
        <button type="submit" class="btn btn-primary" :disabled="generatingReport">
          {{ generatingReport ? 'Generating...' : 'Generate Report' }}
        </button>
      </form>
      
      <div v-if="reportError" class="error">{{ reportError }}</div>
    </div>
    
    <div v-if="report" class="card">
      <h2>Report Results</h2>
      <div class="report-summary">
        <div class="summary-item">
          <strong>Total Shipments:</strong> {{ report.totalShipments }}
        </div>
        <div class="summary-item">
          <strong>Delivery Rate:</strong> {{ report.deliveryRate?.toFixed(2) }}%
        </div>
        <div class="summary-item">
          <strong>Period:</strong> {{ formatDate(report.period?.start) }} - {{ formatDate(report.period?.end) }}
        </div>
      </div>
      
      <div v-if="report.statusCounts" class="status-breakdown">
        <h3>Status Breakdown</h3>
        <div class="status-list">
          <div v-for="(count, status) in report.statusCounts" :key="status" class="status-item">
            <span class="status-label">{{ formatStatus(status) }}:</span>
            <span class="status-count">{{ count }}</span>
          </div>
        </div>
      </div>
      
      <div v-if="report.shipments && report.shipments.length > 0" class="report-shipments">
        <h3>Shipments in Period</h3>
        <div class="shipments-table">
          <div class="table-header">
            <div>Tracking #</div>
            <div>Recipient</div>
            <div>Status</div>
            <div>Created</div>
          </div>
          <div v-for="ship in report.shipments" :key="ship.id" class="table-row">
            <div>{{ ship.trackingNumber }}</div>
            <div>{{ ship.recipientName }}</div>
            <div>
              <span :class="['status-badge', getStatusClass(ship.status)]">
                {{ ship.status }}
              </span>
            </div>
            <div>{{ formatDate(ship.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const statistics = ref(null)
const loadingStats = ref(false)
const statsError = ref('')

const startDate = ref('')
const endDate = ref('')
const report = ref(null)
const generatingReport = ref(false)
const reportError = ref('')

const loadStatistics = async () => {
  loadingStats.value = true
  statsError.value = ''
  
  try {
    const response = await api.get('/reports/statistics')
    statistics.value = response.data
  } catch (error) {
    statsError.value = error.response?.data?.error || 'Failed to load statistics'
  } finally {
    loadingStats.value = false
  }
}

const generateReport = async () => {
  reportError.value = ''
  generatingReport.value = true
  report.value = null
  
  try {
    const response = await api.get('/reports/shipment-report', {
      params: {
        startDate: new Date(startDate.value).toISOString(),
        endDate: new Date(endDate.value).toISOString()
      }
    })
    report.value = response.data
  } catch (error) {
    reportError.value = error.response?.data?.error || 'Failed to generate report'
  } finally {
    generatingReport.value = false
  }
}

const formatStatus = (status) => {
  return status.replace(/_/g, ' ').toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

const getStatusClass = (status) => {
  const statusMap = {
    'PENDING': 'status-pending',
    'IN_TRANSIT': 'status-transit',
    'OUT_FOR_DELIVERY': 'status-out',
    'DELIVERED': 'status-delivered',
    'EXCEPTION': 'status-exception'
  }
  return statusMap[status] || ''
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  loadStatistics()
  
  // Set default dates (last 30 days)
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  
  endDate.value = end.toISOString().slice(0, 16)
  startDate.value = start.toISOString().slice(0, 16)
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.report-form {
  margin-bottom: 20px;
}

.report-summary {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-item {
  padding: 12px 0;
  font-size: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.summary-item:last-child {
  border-bottom: none;
}

.status-breakdown {
  margin-top: 30px;
}

.status-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.status-label {
  font-weight: 600;
  color: #333;
}

.status-count {
  font-weight: 700;
  color: #667eea;
}

.report-shipments {
  margin-top: 30px;
}

.shipments-table {
  margin-top: 16px;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 2fr 1.5fr 2fr;
  gap: 16px;
  padding: 12px;
  background: #667eea;
  color: white;
  font-weight: 600;
  border-radius: 6px 6px 0 0;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 2fr 1.5fr 2fr;
  gap: 16px;
  padding: 12px;
  border-bottom: 1px solid #e0e0e0;
  transition: background 0.2s ease;
}

.table-row:hover {
  background: #f8f9fa;
}

.table-row:last-child {
  border-bottom: none;
  border-radius: 0 0 6px 6px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-pending {
  background: #ffc107;
  color: #333;
}

.status-transit {
  background: #17a2b8;
  color: white;
}

.status-out {
  background: #007bff;
  color: white;
}

.status-delivered {
  background: #28a745;
  color: white;
}

.status-exception {
  background: #dc3545;
  color: white;
}
</style>

