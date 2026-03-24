package com.lms.service;

import com.lms.dto.ChapterDto;
import com.lms.dto.CreateChapterRequest;
import com.lms.entity.Chapter;
import com.lms.entity.Course;
import com.lms.repository.ChapterRepository;
import com.lms.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    private ChapterDto mapToDto(Chapter chapter) {
        return ChapterDto.builder()
                .id(chapter.getId())
                .courseId(chapter.getCourse().getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .orderIndex(chapter.getOrderIndex())
                .createdAt(chapter.getCreatedAt())
                .build();
    }

    public ChapterDto createChapter(UUID courseId, CreateChapterRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Chapter chapter = Chapter.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .build();

        return mapToDto(chapterRepository.save(chapter));
    }

    public List<ChapterDto> getChaptersByCourse(UUID courseId) {
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return chapters.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public void deleteChapter(UUID id) {
        if (!chapterRepository.existsById(id)) {
            throw new IllegalArgumentException("Chapter not found");
        }
        chapterRepository.deleteById(id);
    }
}
