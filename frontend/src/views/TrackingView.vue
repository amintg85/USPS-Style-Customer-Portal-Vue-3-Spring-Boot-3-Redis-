<template>
  <div class="container">
    <h1>Package Tracking</h1>
    
    <div class="card">
      <h2>Track a Package</h2>
      <form @submit.prevent="trackPackage" class="track-form">
        <div class="form-group">
          <label for="trackingNumber">Tracking Number</label>
          <input
            id="trackingNumber"
            v-model="trackingNumber"
            type="text"
            required
            placeholder="Enter tracking number (e.g., USPS1234567890)"
          />
        </div>
        <button type="submit" class="btn btn-primary" :disabled="trackingLoading">
          {{ trackingLoading ? 'Tracking...' : 'Track Package' }}
        </button>
      </form>
      
      <div v-if="trackingError" class="error">{{ trackingError }}</div>
    </div>
    
    <div v-if="shipment" class="card">
      <h2>Shipment Details</h2>
      <div class="shipment-info">
        <div class="info-row">
          <strong>Tracking Number:</strong> {{ shipment.trackingNumber }}
        </div>
        <div class="info-row">
          <strong>Status:</strong> 
          <span :class="['status-badge', getStatusClass(shipment.status)]">
            {{ shipment.status }}
          </span>
        </div>
        <div class="info-row">
          <strong>Recipient:</strong> {{ shipment.recipientName }}
        </div>
        <div class="info-row">
          <strong>Address:</strong> {{ shipment.recipientAddress }}, {{ shipment.recipientCity }}, {{ shipment.recipientState }} {{ shipment.recipientZipCode }}
        </div>
        <div class="info-row">
          <strong>Created:</strong> {{ formatDate(shipment.createdAt) }}
        </div>
        <div v-if="shipment.deliveredAt" class="info-row">
          <strong>Delivered:</strong> {{ formatDate(shipment.deliveredAt) }}
        </div>
      </div>
      
      <div v-if="trackingEvents && trackingEvents.length > 0" class="tracking-events">
        <h3>Tracking History</h3>
        <div class="event-timeline">
          <div v-for="(event, index) in trackingEvents" :key="event.id" class="event-item">
            <div class="event-dot"></div>
            <div class="event-content">
              <div class="event-time">{{ formatDate(event.eventTime) }}</div>
              <div class="event-location">{{ event.location }}</div>
              <div class="event-description">{{ event.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card">
      <h2>Create New Shipment</h2>
      <form @submit.prevent="createShipment" class="create-form">
        <div class="form-row">
          <div class="form-group">
            <label for="recipientName">Recipient Name</label>
            <input
              id="recipientName"
              v-model="newShipment.recipientName"
              type="text"
              required
              placeholder="John Doe"
            />
          </div>
          <div class="form-group">
            <label for="recipientAddress">Address</label>
            <input
              id="recipientAddress"
              v-model="newShipment.recipientAddress"
              type="text"
              required
              placeholder="123 Main St"
            />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label for="recipientCity">City</label>
            <input
              id="recipientCity"
              v-model="newShipment.recipientCity"
              type="text"
              required
              placeholder="New York"
            />
          </div>
          <div class="form-group">
            <label for="recipientState">State</label>
            <input
              id="recipientState"
              v-model="newShipment.recipientState"
              type="text"
              required
              placeholder="NY"
              maxlength="2"
            />
          </div>
          <div class="form-group">
            <label for="recipientZipCode">Zip Code</label>
            <input
              id="recipientZipCode"
              v-model="newShipment.recipientZipCode"
              type="text"
              required
              placeholder="10001"
            />
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="creating">
          {{ creating ? 'Creating...' : 'Create Shipment' }}
        </button>
      </form>
      
      <div v-if="createError" class="error">{{ createError }}</div>
      <div v-if="createSuccess" class="success">{{ createSuccess }}</div>
    </div>
    
    <div class="card">
      <h2>My Shipments</h2>
      <button @click="loadMyShipments" class="btn btn-secondary" :disabled="loadingShipments">
        {{ loadingShipments ? 'Loading...' : 'Refresh List' }}
      </button>
      
      <div v-if="myShipments.length > 0" class="shipments-list">
        <div v-for="ship in myShipments" :key="ship.id" class="shipment-item" @click="selectShipment(ship.trackingNumber)">
          <div class="shipment-header">
            <strong>{{ ship.trackingNumber }}</strong>
            <span :class="['status-badge', getStatusClass(ship.status)]">
              {{ ship.status }}
            </span>
          </div>
          <div class="shipment-details">
            <div>To: {{ ship.recipientName }}</div>
            <div>{{ ship.recipientCity }}, {{ ship.recipientState }}</div>
          </div>
        </div>
      </div>
      <div v-else-if="!loadingShipments" class="no-shipments">
        No shipments found. Create a new shipment above.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const trackingNumber = ref('')
const shipment = ref(null)
const trackingEvents = ref([])
const trackingError = ref('')
const trackingLoading = ref(false)

const newShipment = ref({
  recipientName: '',
  recipientAddress: '',
  recipientCity: '',
  recipientState: '',
  recipientZipCode: ''
})

const creating = ref(false)
const createError = ref('')
const createSuccess = ref('')

const myShipments = ref([])
const loadingShipments = ref(false)

const trackPackage = async () => {
  trackingError.value = ''
  trackingLoading.value = true
  shipment.value = null
  trackingEvents.value = []
  
  try {
    const response = await api.get(`/tracking/${trackingNumber.value}`)
    shipment.value = response.data.shipment
    trackingEvents.value = response.data.events
  } catch (error) {
    trackingError.value = error.response?.data?.error || 'Failed to track package'
  } finally {
    trackingLoading.value = false
  }
}

const createShipment = async () => {
  createError.value = ''
  createSuccess.value = ''
  creating.value = true
  
  try {
    const response = await api.post('/tracking/create', newShipment.value)
    createSuccess.value = `Shipment created! Tracking Number: ${response.data.trackingNumber}`
    newShipment.value = {
      recipientName: '',
      recipientAddress: '',
      recipientCity: '',
      recipientState: '',
      recipientZipCode: ''
    }
    trackingNumber.value = response.data.trackingNumber
    await trackPackage()
    await loadMyShipments()
  } catch (error) {
    createError.value = error.response?.data?.error || 'Failed to create shipment'
  } finally {
    creating.value = false
  }
}

const loadMyShipments = async () => {
  loadingShipments.value = true
  try {
    const response = await api.get('/tracking/my-shipments')
    myShipments.value = response.data.shipments
  } catch (error) {
    console.error('Failed to load shipments:', error)
  } finally {
    loadingShipments.value = false
  }
}

const selectShipment = (trackingNum) => {
  trackingNumber.value = trackingNum
  trackPackage()
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
  loadMyShipments()
})
</script>

<style scoped>
.track-form,
.create-form {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.shipment-info {
  margin: 20px 0;
}

.info-row {
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;
}

.info-row:last-child {
  border-bottom: none;
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

.tracking-events {
  margin-top: 30px;
}

.event-timeline {
  position: relative;
  padding-left: 30px;
}

.event-item {
  position: relative;
  padding-bottom: 20px;
  border-left: 2px solid #e0e0e0;
  padding-left: 20px;
}

.event-item:last-child {
  border-left: none;
}

.event-dot {
  position: absolute;
  left: -6px;
  top: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #667eea;
  border: 2px solid white;
}

.event-content {
  margin-top: -5px;
}

.event-time {
  font-weight: 600;
  color: #667eea;
  margin-bottom: 4px;
}

.event-location {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.event-description {
  color: #666;
  font-size: 14px;
}

.shipments-list {
  margin-top: 20px;
}

.shipment-item {
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.shipment-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.shipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.shipment-details {
  color: #666;
  font-size: 14px;
}

.no-shipments {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>

