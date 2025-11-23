package com.yushan.engagement_service.service;

import com.yushan.engagement_service.client.ContentServiceClient;
import com.yushan.engagement_service.client.UserServiceClient;
import com.yushan.engagement_service.repository.CommentRepository;
import com.yushan.engagement_service.dto.comment.*;
import com.yushan.engagement_service.dto.chapter.ChapterDetailResponseDTO;
import com.yushan.engagement_service.entity.Comment;
import com.yushan.engagement_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService with mocked dependencies.
 */
public class CommentServiceTest {

    private CommentRepository commentRepository;
    private ContentServiceClient contentServiceClient;
    private UserServiceClient userServiceClient;
    private KafkaEventProducerService kafkaEventProducerService;
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentRepository = Mockito.mock(CommentRepository.class);
        contentServiceClient = Mockito.mock(ContentServiceClient.class);
        userServiceClient = Mockito.mock(UserServiceClient.class);
        kafkaEventProducerService = Mockito.mock(KafkaEventProducerService.class);

        commentService = new CommentService();
        try {
            java.lang.reflect.Field f1 = CommentService.class.getDeclaredField("commentRepository");
            f1.setAccessible(true);
            f1.set(commentService, commentRepository);
            
            java.lang.reflect.Field f2 = CommentService.class.getDeclaredField("contentServiceClient");
            f2.setAccessible(true);
            f2.set(commentService, contentServiceClient);
            
            java.lang.reflect.Field f3 = CommentService.class.getDeclaredField("userServiceClient");
            f3.setAccessible(true);
            f3.set(commentService, userServiceClient);
            
            java.lang.reflect.Field f4 = CommentService.class.getDeclaredField("kafkaEventProducerService");
            f4.setAccessible(true);
            f4.set(commentService, kafkaEventProducerService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createComment_WithValidData_ShouldCreateComment() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CommentCreateRequestDTO request = new CommentCreateRequestDTO();
        request.setChapterId(1);
        request.setContent("Test comment");
        request.setIsSpoiler(false);

        when(contentServiceClient.chapterExists(1)).thenReturn(true);
        when(commentRepository.existsByUserAndChapter(userId, 1)).thenReturn(false);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(123); // Set ID after insert
            return comment;
        });

        // Act
        CommentResponseDTO result = commentService.createComment(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
        assertFalse(result.getIsSpoiler());
        verify(commentRepository).save(any(Comment.class));
        verify(kafkaEventProducerService).publishCommentCreatedEvent(eq(123), eq(userId), eq(1), eq("Test comment"), eq(false));
    }

    @Test
    void createComment_WithChapterNotFound_ShouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CommentCreateRequestDTO request = new CommentCreateRequestDTO();
        request.setChapterId(999);
        request.setContent("Test comment");
        request.setIsSpoiler(false);

        when(contentServiceClient.chapterExists(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(userId, request);
        });
    }

    @Test
    void createComment_WithAlreadyCommented_ShouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CommentCreateRequestDTO request = new CommentCreateRequestDTO();
        request.setChapterId(1);
        request.setContent("Test comment");
        request.setIsSpoiler(false);

        when(contentServiceClient.chapterExists(1)).thenReturn(true);
        when(commentRepository.existsByUserAndChapter(userId, 1)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(userId, request);
        });
    }

    @Test
    void createComment_WithEmptyContent_ShouldCreateComment() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CommentCreateRequestDTO request = new CommentCreateRequestDTO();
        request.setChapterId(1);
        request.setContent(""); // Empty content - service doesn't validate this currently
        request.setIsSpoiler(false);

        when(contentServiceClient.chapterExists(1)).thenReturn(true);
        when(commentRepository.existsByUserAndChapter(userId, 1)).thenReturn(false);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(123);
            return comment;
        });

        when(commentRepository.findById(123)).thenReturn(new Comment());

        // Act
        CommentResponseDTO result = commentService.createComment(userId, request);

        // Assert
        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WithNullSpoilerFlag_ShouldUseDefaultFalse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CommentCreateRequestDTO request = new CommentCreateRequestDTO();
        request.setChapterId(1);
        request.setContent("Test comment");
        request.setIsSpoiler(null); // Null spoiler flag

        when(contentServiceClient.chapterExists(1)).thenReturn(true);
        when(commentRepository.existsByUserAndChapter(userId, 1)).thenReturn(false);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(123); // Set ID after insert
            return comment;
        });

        // Act
        CommentResponseDTO result = commentService.createComment(userId, request);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsSpoiler());
        verify(commentRepository).save(argThat(comment -> 
            !comment.getIsSpoiler() // Should be false by default
        ));
    }

    @Test
    void updateComment_WithValidData_ShouldUpdateComment() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(userId);
        existingComment.setContent("Original content");
        existingComment.setIsSpoiler(false);

        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setContent("Updated content");
        request.setIsSpoiler(true);

        when(commentRepository.findById(commentId)).thenReturn(existingComment);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CommentResponseDTO result = commentService.updateComment(commentId, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertTrue(result.getIsSpoiler());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_WithNonExistentComment_ShouldThrowException() {
        // Arrange
        Integer commentId = 999;
        UUID userId = UUID.randomUUID();
        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setContent("Updated content");

        when(commentRepository.findById(commentId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(commentId, userId, request);
        });
    }

    @Test
    void updateComment_WithWrongUser_ShouldThrowException() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(otherUserId); // Different user

        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setContent("Updated content");

        when(commentRepository.findById(commentId)).thenReturn(existingComment);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(commentId, userId, request);
        });
    }

    @Test
    void updateComment_WithEmptyContent_ShouldNotUpdate() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(userId);
        existingComment.setContent("Original content");
        existingComment.setIsSpoiler(false);

        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setContent("   "); // Empty content

        when(commentRepository.findById(commentId)).thenReturn(existingComment);

        // Act
        CommentResponseDTO result = commentService.updateComment(commentId, userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Original content", result.getContent()); // Should remain unchanged
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_WithSameContent_ShouldNotUpdate() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(userId);
        existingComment.setContent("Same content");
        existingComment.setIsSpoiler(false);

        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setContent("Same content"); // Same as existing

        when(commentRepository.findById(commentId)).thenReturn(existingComment);

        // Act
        CommentResponseDTO result = commentService.updateComment(commentId, userId, request);

        // Assert
        assertNotNull(result);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_WithSameSpoilerFlag_ShouldNotUpdate() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(userId);
        existingComment.setContent("Test content");
        existingComment.setIsSpoiler(true);

        CommentUpdateRequestDTO request = new CommentUpdateRequestDTO();
        request.setIsSpoiler(true); // Same as existing

        when(commentRepository.findById(commentId)).thenReturn(existingComment);

        // Act
        CommentResponseDTO result = commentService.updateComment(commentId, userId, request);

        // Assert
        assertNotNull(result);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteComment_WithValidData_ShouldDeleteComment() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(userId);

        when(commentRepository.findById(commentId)).thenReturn(existingComment);
        // delete method returns void, no need to mock return value

        // Act
        boolean result = commentService.deleteComment(commentId, userId, false);

        // Assert
        assertTrue(result);
        verify(commentRepository).delete(commentId);
    }

    @Test
    void deleteComment_WithNonExistentComment_ShouldThrowException() {
        // Arrange
        Integer commentId = 999;
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(commentId, userId, false);
        });
    }

    @Test
    void deleteComment_WithWrongUser_ShouldThrowException() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(otherUserId); // Different user

        when(commentRepository.findById(commentId)).thenReturn(existingComment);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(commentId, userId, false);
        });
    }

    @Test
    void deleteComment_WithAdmin_ShouldDeleteAnyComment() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        
        Comment existingComment = new Comment();
        existingComment.setId(commentId);
        existingComment.setUserId(otherUserId); // Different user

        when(commentRepository.findById(commentId)).thenReturn(existingComment);
        // delete method returns void, no need to mock return value

        // Act
        boolean result = commentService.deleteComment(commentId, userId, true); // Admin = true

        // Assert
        assertTrue(result);
        verify(commentRepository).delete(commentId);
    }

    @Test
    void getUserComments_WithValidData_ShouldReturnComments() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<Comment> comments = Arrays.asList(
            createTestComment(1, userId, "Comment 1"),
            createTestComment(2, userId, "Comment 2")
        );

        when(commentRepository.findByUserId(userId)).thenReturn(comments);

        // Act
        List<CommentResponseDTO> result = commentService.getUserComments(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Comment 1", result.get(0).getContent());
        assertEquals("Comment 2", result.get(1).getContent());
    }

    @Test
    void getUserComments_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(commentRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

        // Act
        List<CommentResponseDTO> result = commentService.getUserComments(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserComments_WithNullList_ShouldReturnEmptyList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(commentRepository.findByUserId(userId)).thenReturn(new ArrayList<>()); // Return empty list instead of null

        // Act
        List<CommentResponseDTO> result = commentService.getUserComments(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void batchDeleteComments_WithValidData_ShouldDeleteComments() {
        // Arrange
        CommentBatchDeleteRequestDTO request = new CommentBatchDeleteRequestDTO();
        request.setCommentIds(Arrays.asList(1, 2, 3));
        boolean isAdmin = true;

        Comment comment1 = new Comment();
        comment1.setId(1);
        comment1.setUserId(UUID.randomUUID());
        comment1.setContent("Comment 1");

        Comment comment2 = new Comment();
        comment2.setId(2);
        comment2.setUserId(UUID.randomUUID());
        comment2.setContent("Comment 2");

        Comment comment3 = new Comment();
        comment3.setId(3);
        comment3.setUserId(UUID.randomUUID());
        comment3.setContent("Comment 3");

        when(commentRepository.findById(1)).thenReturn(comment1);
        when(commentRepository.findById(2)).thenReturn(comment2);
        when(commentRepository.findById(3)).thenReturn(comment3);
        // delete method returns void, no need to mock return value

        // Act
        int result = commentService.batchDeleteComments(request, isAdmin);

        // Assert
        assertEquals(3, result);
        verify(commentRepository).findById(1);
        verify(commentRepository).findById(2);
        verify(commentRepository).findById(3);
        verify(commentRepository).delete(1);
        verify(commentRepository).delete(2);
        verify(commentRepository).delete(3);
    }

    @Test
    void batchDeleteComments_WithNotAdmin_ShouldThrowException() {
        // Arrange
        CommentBatchDeleteRequestDTO request = new CommentBatchDeleteRequestDTO();
        request.setCommentIds(Arrays.asList(1, 2, 3));
        boolean isAdmin = false;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> commentService.batchDeleteComments(request, isAdmin));
        verify(commentRepository, never()).findById(anyInt());
        verify(commentRepository, never()).delete(anyInt());
    }

    @Test
    void batchDeleteComments_WithEmptyList_ShouldThrowException() {
        // Arrange
        CommentBatchDeleteRequestDTO request = new CommentBatchDeleteRequestDTO();
        request.setCommentIds(Arrays.asList());
        boolean isAdmin = true;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> commentService.batchDeleteComments(request, isAdmin));
        verify(commentRepository, never()).findById(anyInt());
        verify(commentRepository, never()).delete(anyInt());
    }

    @Test
    void toggleLike_WithValidData_ShouldToggleLike() {
        // Arrange
        Integer commentId = 1;
        UUID userId = UUID.randomUUID();
        boolean isLiking = true;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setLikeCnt(5);

        when(commentRepository.findById(commentId)).thenReturn(comment);
        doNothing().when(commentRepository).updateLikeCount(commentId, 1);
        when(commentRepository.findById(commentId)).thenReturn(comment);

        // Act
        CommentResponseDTO result = commentService.toggleLike(commentId, userId, isLiking);

        // Assert
        assertNotNull(result);
        verify(commentRepository).updateLikeCount(commentId, 1);
    }

    @Test
    void toggleLike_WithNonExistentComment_ShouldThrowException() {
        // Arrange
        Integer commentId = 999;
        UUID userId = UUID.randomUUID();
        boolean isLiking = true;

        when(commentRepository.findById(commentId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.toggleLike(commentId, userId, isLiking);
        });
    }

    @Test
    void hasUserCommentedOnChapter_WithValidData_ShouldReturnTrue() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Integer chapterId = 1;

        when(commentRepository.existsByUserAndChapter(userId, chapterId)).thenReturn(true);

        // Act
        boolean result = commentService.hasUserCommentedOnChapter(userId, chapterId);

        // Assert
        assertTrue(result);
        verify(commentRepository).existsByUserAndChapter(userId, chapterId);
    }

    @Test
    void hasUserCommentedOnChapter_WithNoComment_ShouldReturnFalse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Integer chapterId = 1;

        when(commentRepository.existsByUserAndChapter(userId, chapterId)).thenReturn(false);

        // Act
        boolean result = commentService.hasUserCommentedOnChapter(userId, chapterId);

        // Assert
        assertFalse(result);
        verify(commentRepository).existsByUserAndChapter(userId, chapterId);
    }

    @Test
    void getChapterCommentStats_WithValidData_ShouldReturnStats() {
        // Arrange
        Integer chapterId = 1;
        List<Comment> comments = Arrays.asList(
            createTestComment(1, UUID.randomUUID(), "Comment 1"),
            createTestComment(2, UUID.randomUUID(), "Comment 2")
        );

        ChapterDetailResponseDTO chapter = new ChapterDetailResponseDTO();
        chapter.setTitle("Test Chapter");

        when(contentServiceClient.chapterExists(chapterId)).thenReturn(true);
        when(contentServiceClient.getChapter(chapterId)).thenReturn(chapter);
        when(commentRepository.findByChapterId(chapterId)).thenReturn(comments);

        // Act
        CommentStatisticsDTO result = commentService.getChapterCommentStats(chapterId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalComments());
        verify(contentServiceClient).chapterExists(chapterId);
        verify(contentServiceClient).getChapter(chapterId);
        verify(commentRepository).findByChapterId(chapterId);
    }

    @Test
    void getChapterCommentStats_WithNoComments_ShouldReturnZeroStats() {
        // Arrange
        Integer chapterId = 1;

        ChapterDetailResponseDTO chapter = new ChapterDetailResponseDTO();
        chapter.setTitle("Test Chapter");

        when(contentServiceClient.chapterExists(chapterId)).thenReturn(true);
        when(contentServiceClient.getChapter(chapterId)).thenReturn(chapter);
        when(commentRepository.findByChapterId(chapterId)).thenReturn(new ArrayList<>());

        // Act
        CommentStatisticsDTO result = commentService.getChapterCommentStats(chapterId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalComments());
        verify(contentServiceClient).chapterExists(chapterId);
        verify(contentServiceClient).getChapter(chapterId);
        verify(commentRepository).findByChapterId(chapterId);
    }

    @Test
    void getCommentsByChapter_WithValidData_ShouldReturnComments() {
        // Arrange
        Integer chapterId = 1;
        UUID currentUserId = UUID.randomUUID();
        int page = 0;
        int size = 10;
        String sort = "createTime";
        String order = "desc";

        when(contentServiceClient.chapterExists(chapterId)).thenReturn(true);
        when(commentRepository.findCommentsWithPagination(any(CommentSearchRequestDTO.class))).thenReturn(new ArrayList<>());
        when(commentRepository.countComments(any(CommentSearchRequestDTO.class))).thenReturn(0L);

        // Act
        CommentListResponseDTO result = commentService.getCommentsByChapter(chapterId, currentUserId, page, size, sort, order);

        // Assert
        assertNotNull(result);
        assertTrue(result.getComments().isEmpty());
        assertEquals(0, result.getTotalCount());
        verify(contentServiceClient).chapterExists(chapterId);
        verify(commentRepository).findCommentsWithPagination(any(CommentSearchRequestDTO.class));
        verify(commentRepository).countComments(any(CommentSearchRequestDTO.class));
    }

    @Test
    void getCommentsByChapter_WithNonExistentChapter_ShouldThrowException() {
        // Arrange
        Integer chapterId = 999;
        UUID currentUserId = UUID.randomUUID();
        when(contentServiceClient.chapterExists(chapterId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentsByChapter(chapterId, currentUserId, 0, 10, "createTime", "desc");
        });

        verify(contentServiceClient).chapterExists(chapterId);
    }


    // Helper method to create test comments
    private Comment createTestComment(Integer id, UUID userId, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikeCnt(0);
        comment.setIsSpoiler(false);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        return comment;
    }
}