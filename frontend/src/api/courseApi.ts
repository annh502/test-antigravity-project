import api from './index'
import type { CourseResponse } from '@/types/course'
import type { PageResponse } from '@/types/pagination'

export const courseApi = {
  getList: (params: { page: number; size: number }) =>
    api.get<PageResponse<CourseResponse>>('/api/v1/courses', { params }).then(r => r.data),
}
