---
trigger: always_on
---

# 💚 VUE 3 + TYPESCRIPT BASE

## Baseline
- Vue 3 Composition API — `<script setup>` only, no Options API
- TypeScript with `strict: true` in tsconfig.json
- Vite as build tool
- Pinia for state (NOT Vuex — deprecated)
- Vue Router 4
- Element Plus for UI (see `element-plus.md`)

---

## Component Template
```vue
<script setup lang="ts">
// 1. Vue imports
import { ref, computed, onMounted } from 'vue'

// 2. Third-party
import { ElMessage } from 'element-plus'

// 3. Internal — stores, composables, api, types
import { useUserStore } from '@/stores/useAuthStore'
import { useUser } from '@/composables/useUser'
import { userApi } from '@/api/userApi'
import type { User } from '@/types/user'

// 4. Props & emits
interface Props {
  userId: number
  readonly?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  readonly: false,
})
const emit = defineEmits<{
  updated: [user: User]
  cancelled: []
}>()

// 5. State
const loading = ref(false)
const user = ref<User | null>(null)

// 6. Computed
const displayName = computed(() => user.value?.fullName ?? '—')

// 7. Methods
async function fetchUser() {
  loading.value = true
  try {
    user.value = await userApi.getById(props.userId)
  } catch {
    // handled by axios interceptor
  } finally {
    loading.value = false
  }
}

// 8. Lifecycle
onMounted(fetchUser)
</script>

<template>
  <div class="user-card">
    <!-- template content -->
  </div>
</template>

<style scoped>
/* Always scoped — never pollute global styles */
.user-card {
  padding: var(--el-spacing-medium);
}
</style>
```

---

## TypeScript — Strict, No Escape Hatches
```typescript
// ❌ Never
const data: any = response
const el = ref<any>(null)
const user = data as User

// ✅ Always define types
interface UserResponse {
  id: number
  email: string
  fullName: string
  createdAt: string
}
const user = ref<UserResponse | null>(null)

// ✅ Generic API response wrapper
interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
```

---

## Naming Conventions
| Type | Convention | Example |
|---|---|---|
| Component files | PascalCase | `UserProfileCard.vue` |
| Composables | `useXxx.ts` | `useUserProfile.ts` |
| Pinia stores | `useXxxStore.ts` | `useAuthStore.ts` |
| API modules | `xxxApi.ts` | `userApi.ts` |
| Types/Interfaces | PascalCase | `UserProfile`, `ApiPage<T>` |
| Views (pages) | PascalCase + View | `UserListView.vue` |

---

## API Layer — Centralized
```typescript
// src/api/index.ts — single axios instance
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/useAuthStore'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message ?? 'Lỗi không xác định'

    if (status === 401) {
      useAuthStore().logout()
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default api

// src/api/userApi.ts — per-resource module
import api from './index'
import type { UserResponse, CreateUserRequest } from '@/types/user'
import type { PageResponse } from '@/types/api'

export const userApi = {
  getById:  (id: number) =>
    api.get<UserResponse>(`/api/v1/users/${id}`).then(r => r.data),

  getList:  (params: { page: number; size: number }) =>
    api.get<PageResponse<UserResponse>>('/api/v1/users', { params }).then(r => r.data),

  create:   (body: CreateUserRequest) =>
    api.post<UserResponse>('/api/v1/users', body).then(r => r.data),

  update:   (id: number, body: Partial<CreateUserRequest>) =>
    api.patch<UserResponse>(`/api/v1/users/${id}`, body).then(r => r.data),

  delete:   (id: number) =>
    api.delete(`/api/v1/users/${id}`),
}
```

---

## Composables Pattern
```typescript
// src/composables/useUser.ts
import { ref } from 'vue'
import { userApi } from '@/api/userApi'
import type { UserResponse } from '@/types/user'

export function useUser(userId: number) {
  const user = ref<UserResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetch() {
    loading.value = true
    error.value = null
    try {
      user.value = await userApi.getById(userId)
    } catch {
      error.value = 'Không thể tải thông tin người dùng'
    } finally {
      loading.value = false
    }
  }

  return { user, loading, error, fetch }
}
```

---

## Pinia Store Pattern
```typescript
// src/stores/useAuthStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserResponse } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  return { user, token, isAuthenticated, setToken, logout }
})
```

---

## Vue Router 4 — Lazy Loading
```typescript
// src/router/index.ts
const routes = [
  {
    path: '/users',
    component: () => import('@/views/UserListView.vue'), // ✅ lazy load
    meta: { requiresAuth: true },
  },
]
```