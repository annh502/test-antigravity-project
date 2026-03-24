<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElCard, ElPagination } from 'element-plus'
import { courseApi } from '@/api/courseApi'
import type { CourseResponse } from '@/types/course'

const courses = ref<CourseResponse[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1, // Element Plus uses 1-indexes internally
  pageSize: 20,
  total: 0,
})

async function fetchCourses() {
  loading.value = true
  try {
    const data = await courseApi.getList({
      page: pagination.page - 1, // FastAPI/SpringBoot are 0-indexed typically
      size: pagination.pageSize,
    })
    courses.value = data.content
    pagination.total = data.totalElements
  } catch {
    // Errors handled gracefully by central axios interceptor ElMessage logic
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchCourses()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchCourses()
}

onMounted(fetchCourses)
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <h1 class="text-3xl font-bold mb-8 text-gray-800">Danh sách Khóa học</h1>
    
    <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-8">
      <el-card 
        v-for="course in courses" 
        :key="course.id" 
        class="hover:shadow-xl transition-shadow duration-300 border-none rounded-xl overflow-hidden"
        :body-style="{ padding: '0px' }"
      >
        <img 
          :src="course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&q=80&w=800'" 
          class="w-full h-52 object-cover" 
          alt="Course thumbnail" 
        />
        <div class="p-6">
          <h2 class="text-xl font-bold mb-2 text-gray-900 line-clamp-1">
            {{ course.title }}
          </h2>
          <p class="text-gray-500 mb-4 line-clamp-2 min-h-[3rem]">
            {{ course.description || 'Chưa có thông tin mô tả chi tiết cho khóa học này.' }}
          </p>
          <div class="flex items-center justify-between text-sm text-gray-400">
            <span>Ngày tạo: {{ new Date(course.createdAt).toLocaleDateString('vi-VN') }}</span>
          </div>
        </div>
      </el-card>

      <!-- Empty State -->
      <div v-if="!loading && courses.length === 0" class="col-span-full text-center py-20 text-gray-500">
        Hiện tại chưa có khóa học nào trên hệ thống.
      </div>
    </div>

    <!-- Pagination Settings -->
    <div class="flex justify-center" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>
