<template>
  <div id="app">
    <nav v-if="authStore.isAuthenticated" class="navbar">
      <div class="nav-container">
        <div class="nav-brand">
          <h2>📮 USPS Customer Portal</h2>
        </div>
        <div class="nav-links">
          <router-link to="/tracking" class="nav-link">Tracking</router-link>
          <router-link to="/reports" class="nav-link">Reports</router-link>
          <span class="nav-user">{{ authStore.user?.firstName }} {{ authStore.user?.lastName }}</span>
          <button @click="handleLogout" class="btn btn-secondary">Logout</button>
        </div>
      </div>
    </nav>
    <router-view />
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 16px 0;
  margin-bottom: 30px;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-brand h2 {
  color: #667eea;
  font-size: 24px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #333;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: #667eea;
  color: white;
}

.nav-user {
  color: #666;
  font-weight: 500;
}
</style>


