package com.yushan.engagement_service.service;

import com.yushan.engagement_service.client.ContentServiceClient;
import com.yushan.engagement_service.client.UserServiceClient;
import com.yushan.engagement_service.repository.ReviewRepository;
import com.yushan.engagement_service.dto.common.ApiResponse;
import com.yushan.engagement_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.engagement_service.dto.review.*;
import com.yushan.engagement_service.entity.Review;
import com.yushan.engagement_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ContentServiceClient contentServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @Mock
    private TransactionAwareKafkaPublisher transactionAwareKafkaPublisher;

    @InjectMocks
    private ReviewService reviewService;

    private UUID testUserId;
    private Integer testNovelId;
    private Review testReview;
    private ReviewCreateRequestDTO testCreateRequest;
    private ReviewUpdateRequestDTO testUpdateRequest;
    private NovelDetailResponseDTO testNovel;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNovelId = 1;
        
        testNovel = new NovelDetailResponseDTO();
        testNovel.setId(testNovelId);
        testNovel.setTitle("Test Novel");
        testNovel.setAvgRating(4.5f);

        testReview = new Review();
        testReview.setId(1);
        testReview.setUuid(UUID.randomUUID());
        testReview.setUserId(testUserId);
        testReview.setNovelId(testNovelId);
        testReview.setRating(5);
        testReview.setTitle("Great novel!");
        testReview.setContent("I really enjoyed this novel.");
        testReview.setLikeCnt(10);
        testReview.setIsSpoiler(false);
        testReview.setCreateTime(new Date());
        testReview.setUpdateTime(new Date());

        testCreateRequest = new ReviewCreateRequestDTO();
        testCreateRequest.setNovelId(testNovelId);
        testCreateRequest.setRating(5);
        testCreateRequest.setTitle("Great novel!");
        testCreateRequest.setContent("I really enjoyed this novel.");
        testCreateRequest.setIsSpoiler(false);

        testUpdateRequest = new ReviewUpdateRequestDTO();
        testUpdateRequest.setRating(4);
        testUpdateRequest.setTitle("Updated title");
        testUpdateRequest.setContent("Updated content");
        testUpdateRequest.setIsSpoiler(true);
    }

    @Test
    void createReview_WithValidData_ShouldCreateReview() {
        // Arrange
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(null);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1);
            return review;
        });
        when(reviewRepository.findByNovelId(testNovelId)).thenReturn(Arrays.asList(testReview));
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        ReviewResponseDTO result = reviewService.createReview(testUserId, testCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testNovelId, result.getNovelId());
        assertEquals(5, result.getRating());
        assertEquals("Great novel!", result.getTitle());
        assertEquals("I really enjoyed this novel.", result.getContent());
        assertFalse(result.getIsSpoiler());
        
        verify(contentServiceClient, times(2)).getNovelById(testNovelId);
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
        verify(reviewRepository).save(any(Review.class));
        // Verify publishAfterCommit is called (may be called multiple times: once for review event, once for rating update)
        verify(transactionAwareKafkaPublisher, atLeastOnce()).publishAfterCommit(any(Runnable.class));
    }

    @Test
    void createReview_WithNonExistentNovel_ShouldThrowException() {
        // Arrange
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(testUserId, testCreateRequest);
        });
        
        verify(contentServiceClient).getNovelById(testNovelId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_WithExistingReview_ShouldThrowException() {
        // Arrange
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(testReview);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(testUserId, testCreateRequest);
        });
        
        verify(contentServiceClient).getNovelById(testNovelId);
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_WithValidData_ShouldUpdateReview() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(testReview);

        // Act
        ReviewResponseDTO result = reviewService.updateReview(1, testUserId, testUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated content", result.getContent());
        assertTrue(result.getIsSpoiler());
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_WithNonExistentReview_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.updateReview(1, testUserId, testUpdateRequest);
        });
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_WithWrongUser_ShouldThrowException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(reviewRepository.findById(1)).thenReturn(testReview);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(1, otherUserId, testUpdateRequest);
        });
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_WithValidData_ShouldDeleteReview() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(testReview);
        // delete method returns void, no need to mock return value
        when(reviewRepository.findByNovelId(testNovelId)).thenReturn(Arrays.asList(testReview));

        // Act
        boolean result = reviewService.deleteReview(1, testUserId, false);

        // Assert
        assertTrue(result);
        verify(reviewRepository).findById(1);
        verify(reviewRepository).delete(1);
    }

    @Test
    void deleteReview_WithNonExistentReview_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.deleteReview(1, testUserId, false);
        });
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository, never()).delete(anyInt());
    }

    @Test
    void deleteReview_WithWrongUser_ShouldThrowException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(reviewRepository.findById(1)).thenReturn(testReview);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1, otherUserId, false);
        });
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository, never()).delete(anyInt());
    }

    @Test
    void deleteReview_WithAdmin_ShouldDeleteReview() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(reviewRepository.findById(1)).thenReturn(testReview);
        // delete method returns void, no need to mock return value
        when(reviewRepository.findByNovelId(testNovelId)).thenReturn(Arrays.asList(testReview));

        // Act
        boolean result = reviewService.deleteReview(1, otherUserId, true);

        // Assert
        assertTrue(result);
        verify(reviewRepository).findById(1);
        verify(reviewRepository).delete(1);
    }

    @Test
    void getReview_WithValidId_ShouldReturnReview() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(testReview);
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        ReviewResponseDTO result = reviewService.getReview(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(testNovelId, result.getNovelId());
        assertEquals(5, result.getRating());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test Novel", result.getNovelTitle());
        
        verify(reviewRepository).findById(1);
    }

    @Test
    void getReview_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReview(1);
        });
        
        verify(reviewRepository).findById(1);
    }

    @Test
    void getReviewsByNovel_WithValidData_ShouldReturnReviews() {
        // Arrange
        when(reviewRepository.findReviewsWithPagination(any(ReviewSearchRequestDTO.class))).thenReturn(Arrays.asList(testReview));
        when(reviewRepository.countReviews(any(ReviewSearchRequestDTO.class))).thenReturn(1L);
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        var result = reviewService.getReviewsByNovel(testNovelId, 0, 10, "createTime", "desc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getSize());
        
        verify(reviewRepository).findReviewsWithPagination(any(ReviewSearchRequestDTO.class));
        verify(reviewRepository).countReviews(any(ReviewSearchRequestDTO.class));
    }

    @Test
    void getAllReviews_WithValidData_ShouldReturnReviews() {
        // Arrange
        ReviewSearchRequestDTO request = new ReviewSearchRequestDTO(0, 10, "createTime", "desc", testNovelId, null, null, null);
        when(reviewRepository.findReviewsWithPagination(request)).thenReturn(Arrays.asList(testReview));
        when(reviewRepository.countReviews(request)).thenReturn(1L);
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        var result = reviewService.getAllReviews(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getSize());
        
        verify(reviewRepository).findReviewsWithPagination(request);
        verify(reviewRepository).countReviews(request);
    }

    @Test
    void toggleLike_WithValidData_ShouldToggleLike() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(testReview);
        doNothing().when(reviewRepository).updateLikeCount(1, 1);
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        ReviewResponseDTO result = reviewService.toggleLike(1, testUserId, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        
        verify(reviewRepository, times(2)).findById(1);
        verify(reviewRepository).updateLikeCount(1, 1);
    }

    @Test
    void toggleLike_WithNonExistentReview_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.toggleLike(1, testUserId, true);
        });
        
        verify(reviewRepository).findById(1);
        verify(reviewRepository, never()).updateLikeCount(anyInt(), anyInt());
    }

    @Test
    void getUserReviews_WithValidData_ShouldReturnReviews() {
        // Arrange
        when(reviewRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(testReview));
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        List<ReviewResponseDTO> result = reviewService.getUserReviews(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        
        verify(reviewRepository).findByUserId(testUserId);
    }

    @Test
    void hasUserReviewedNovel_WithExistingReview_ShouldReturnTrue() {
        // Arrange
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(testReview);

        // Act
        boolean result = reviewService.hasUserReviewedNovel(testUserId, testNovelId);

        // Assert
        assertTrue(result);
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
    }

    @Test
    void hasUserReviewedNovel_WithNoReview_ShouldReturnFalse() {
        // Arrange
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(null);

        // Act
        boolean result = reviewService.hasUserReviewedNovel(testUserId, testNovelId);

        // Assert
        assertFalse(result);
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
    }

    @Test
    void getUserReviewForNovel_WithExistingReview_ShouldReturnReview() {
        // Arrange
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(testReview);
        when(userServiceClient.getUsernameById(testUserId)).thenReturn("testuser");
        
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        // Act
        ReviewResponseDTO result = reviewService.getUserReviewForNovel(testUserId, testNovelId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(testNovelId, result.getNovelId());
        
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
    }

    @Test
    void getUserReviewForNovel_WithNoReview_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findByUserAndNovel(testUserId, testNovelId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getUserReviewForNovel(testUserId, testNovelId);
        });
        
        verify(reviewRepository).findByUserAndNovel(testUserId, testNovelId);
    }

    @Test
    void getNovelRatingStats_WithValidData_ShouldReturnStats() {
        // Arrange
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);
        when(reviewRepository.findByNovelId(testNovelId)).thenReturn(Arrays.asList(testReview));

        // Act
        NovelRatingStatsDTO result = reviewService.getNovelRatingStats(testNovelId);

        // Assert
        assertNotNull(result);
        assertEquals(testNovelId, result.getNovelId());
        assertEquals("Test Novel", result.getNovelTitle());
        assertEquals(1, result.getTotalReviews());
        assertEquals(4.5f, result.getAverageRating());
        
        verify(contentServiceClient).getNovelById(testNovelId);
        verify(reviewRepository).findByNovelId(testNovelId);
    }

    @Test
    void getNovelRatingStats_WithNoReviews_ShouldReturnZeroStats() {
        // Arrange
        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setData(testNovel);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);
        when(reviewRepository.findByNovelId(testNovelId)).thenReturn(new ArrayList<>());

        // Act
        NovelRatingStatsDTO result = reviewService.getNovelRatingStats(testNovelId);

        // Assert
        assertNotNull(result);
        assertEquals(testNovelId, result.getNovelId());
        assertEquals("Test Novel", result.getNovelTitle());
        assertEquals(0, result.getTotalReviews());
        assertEquals(4.5f, result.getAverageRating());
        
        verify(contentServiceClient).getNovelById(testNovelId);
        verify(reviewRepository).findByNovelId(testNovelId);
    }
}