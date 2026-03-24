package com.lms.controller;

import com.lms.dto.ChapterDto;
import com.lms.dto.CreateChapterRequest;
import com.lms.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @PostMapping("/courses/{courseId}/chapters")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ChapterDto> createChapter(
            @PathVariable UUID courseId,
            @RequestBody CreateChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chapterService.createChapter(courseId, request));
    }

    @GetMapping("/courses/{courseId}/chapters")
    public ResponseEntity<List<ChapterDto>> getChaptersByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    @DeleteMapping("/chapters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteChapter(@PathVariable UUID id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }
}
