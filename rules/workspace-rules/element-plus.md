# 🎨 ELEMENT PLUS (MANUAL IMPORT)

## Import Rules — Critical
```typescript
// ✅ Components: import from 'element-plus'
import {
  ElButton, ElForm, ElFormItem, ElInput, ElInputNumber,
  ElTable, ElTableColumn, ElPagination,
  ElDialog, ElDrawer,
  ElMessage, ElMessageBox, ElNotification,
  ElSelect, ElOption, ElDatePicker, ElTimePicker,
  ElCard, ElDivider, ElTag, ElBadge, ElAvatar,
  ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElTooltip, ElPopover,
  ElLoading,
} from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// ✅ Icons: ALWAYS import separately from '@element-plus/icons-vue'
import {
  Edit, Delete, Plus, Search, Refresh,
  ArrowDown, ArrowUp, Close, Check,
  Upload, Download, View,
} from '@element-plus/icons-vue'

// ❌ Never import the full bundle
import ElementPlus from 'element-plus'
```

---

## Form Validation — script setup Pattern
```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface FormModel {
  email: string
  fullName: string
  password: string
}

const formRef = ref<FormInstance>()

const form = reactive<FormModel>({
  email: '',
  fullName: '',
  password: '',
})

const rules = reactive<FormRules<FormModel>>({
  email: [
    { required: true, message: 'Email là bắt buộc', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: 'blur' },
  ],
  fullName: [
    { required: true, message: 'Họ tên là bắt buộc', trigger: 'blur' },
    { min: 2, message: 'Tối thiểu 2 ký tự', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Mật khẩu là bắt buộc', trigger: 'blur' },
    { min: 8, message: 'Tối thiểu 8 ký tự', trigger: 'blur' },
  ],
})

const loading = ref(false)

async function handleSubmit() {
  if (!formRef.value) return

  // ✅ Always await validate, always catch
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await submitApi(form)
    ElMessage.success('Tạo thành công!')
    formRef.value.resetFields()  // ✅ Reset after success
    emit('saved')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  formRef.value?.resetFields()  // ✅ Also reset on cancel
  emit('cancelled')
}
</script>

<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="Email" prop="email">
      <el-input v-model="form.email" placeholder="email@example.com" />
    </el-form-item>
    <el-form-item label="Họ tên" prop="fullName">
      <el-input v-model="form.fullName" />
    </el-form-item>
    <el-form-item label="Mật khẩu" prop="password">
      <el-input v-model="form.password" type="password" show-password />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        Xác nhận
      </el-button>
      <el-button @click="handleCancel">Hủy</el-button>
    </el-form-item>
  </el-form>
</template>
```

---

## ElMessageBox — Always try/catch
```typescript
// ✅ Correct — catch prevents cancel from falling through
async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm(
      'Bạn chắc chắn muốn xóa mục này?',
      'Xác nhận xóa',
      {
        type: 'warning',
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy',
        confirmButtonClass: 'el-button--danger',
      }
    )
    await userApi.delete(id)
    ElMessage.success('Đã xóa thành công')
    await fetchList()
  } catch {
    // user clicked Cancel — intentionally do nothing
  }
}

// ❌ Wrong — deleteUser runs even when user clicks Cancel
await ElMessageBox.confirm('Xóa?', 'Cảnh báo')
await userApi.delete(id)  // BUG: executes on cancel!
```

---

## Table + Backend Pagination
```vue
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElPagination } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { userApi } from '@/api/userApi'
import type { UserResponse } from '@/types/user'

const tableData = ref<UserResponse[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,        // Element Plus is 1-indexed
  pageSize: 20,
  total: 0,
})

async function fetchList() {
  loading.value = true
  try {
    const data = await userApi.getList({
      page: pagination.page - 1,   // Spring Boot / FastAPI are 0-indexed
      size: pagination.pageSize,
    })
    tableData.value = data.content
    pagination.total = data.totalElements
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1   // ✅ Reset to page 1 on size change
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <el-table
    v-loading="loading"
    :data="tableData"
    row-key="id"        <!-- ✅ Always use id, never index -->
    stripe
    border
  >
    <el-table-column prop="email" label="Email" min-width="200" />
    <el-table-column prop="fullName" label="Họ tên" min-width="150" />
    <el-table-column label="Thao tác" width="120" fixed="right">
      <template #default="{ row }: { row: UserResponse }">
        <el-button :icon="Edit" circle size="small" @click="handleEdit(row)" />
        <el-button
          :icon="Delete"
          circle
          size="small"
          type="danger"
          @click="handleDelete(row.id)"
        />
      </template>
    </el-table-column>
  </el-table>

  <el-pagination
    v-model:current-page="pagination.page"
    v-model:page-size="pagination.pageSize"
    :total="pagination.total"
    :page-sizes="[10, 20, 50, 100]"
    layout="total, sizes, prev, pager, next, jumper"
    class="mt-4"
    @current-change="handlePageChange"
    @size-change="handleSizeChange"
  />
</template>
```

---

## Dialog Rules
```vue
<!-- ✅ destroy-on-close resets child form state when dialog closes -->
<el-dialog
  v-model="dialogVisible"
  title="Thêm người dùng"
  width="560px"
  destroy-on-close
  @close="selectedId = null"
>
  <UserForm :user-id="selectedId" @saved="handleSaved" @cancelled="dialogVisible = false" />
</el-dialog>

<!-- ❌ Without destroy-on-close, form retains previous values on reopen -->
```

---

## Feedback Components — When to Use Which
| Component | Use for | Don't use for |
|---|---|---|
| `ElMessage` | Quick action result (saved, deleted, error) | Long messages, system events |
| `ElNotification` | System-level events needing more attention | Every small action |
| `ElMessageBox` | Destructive action confirmation only | Info, warnings without action |

- Do not use both `ElMessage` and `console.log` for the same event
- Do not use `ElNotification` for everything — reserve it for events the user needs to act on

---

## Remote Select
```vue
<script setup lang="ts">
const options = ref<Array<{ label: string; value: number }>>([])
const selectLoading = ref(false)

async function remoteSearch(query: string) {
  if (query.trim().length < 2) {
    options.value = []
    return
  }
  selectLoading.value = true
  try {
    options.value = await searchApi.users(query)
  } finally {
    selectLoading.value = false
  }
}
</script>

<template>
  <!-- ✅ remote + remote-method — don't self-filter -->
  <el-select
    v-model="selectedValue"
    filterable
    remote
    :remote-method="remoteSearch"
    :loading="selectLoading"
    placeholder="Nhập để tìm kiếm..."
    clearable
  >
    <el-option
      v-for="opt in options"
      :key="opt.value"
      :label="opt.label"
      :value="opt.value"
    />
  </el-select>
</template>
```

---

## Styling — Never Hardcode Colors
```css
/* ❌ Never */
color: #409EFF;
background-color: #67C23A;
border-color: #E6A23C;

/* ✅ Always use Element Plus CSS variables */
color: var(--el-color-primary);
color: var(--el-color-success);
color: var(--el-color-warning);
color: var(--el-color-danger);
color: var(--el-text-color-primary);
color: var(--el-text-color-secondary);
font-size: var(--el-font-size-base);
font-size: var(--el-font-size-medium);
border-radius: var(--el-border-radius-base);
border: 1px solid var(--el-border-color);
```

---

## Loading States — Don't Mix
```
v-loading directive   → Block-level containers, tables, cards
ElButton :loading     → Individual submit/action buttons

❌ Don't apply both to the same action:
   <el-button :loading="saving" @click="save" />  +  v-loading="saving"  on parent = confusing UX
```