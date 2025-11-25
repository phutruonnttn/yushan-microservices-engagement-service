# Yushan Engagement Service

> 💬 **Engagement Service for Yushan Platform (Phase 2 - Microservices)** - Manages user interactions including comments, reviews, ratings, votes, and social features to foster community engagement.

## 📋 Overview

Engagement Service is one of the main microservices of Yushan Platform (Phase 2), responsible for managing all user social interactions. This service uses Kafka to publish events for Gamification Service and integrates with other services via Feign clients.

## Architecture Overview

```
┌─────────────────────────────┐
│   Eureka Service Registry   │
│       localhost:8761        │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│   Service Registration &     │
│      Discovery Layer         │
└──────────────┬──────────────┘
               │
    ┌──────────┴──────────┬───────────────┬──────────┬──────────┐
    │                     │               │          │          │
    ▼                     ▼               ▼          ▼          ▼
┌────────┐          ┌─────────┐  ┌────────────┐ ┌──────────┐ ┌──────────┐
│  User  │          │ Content │  │ Engagement │ │Gamifica- │ │Analytics │
│Service │◄────────►│ Service │◄─┤  Service   ├─►│  tion    │ │ Service  │
│ :8081  │          │  :8082  │  │   :8084    ├─►│ Service  │ │  :8083   │
└────────┘          └─────────┘  └─────┬──────┘ │  :8085   │ └──────────┘
    │                     │             │        └──────────┘      │
    └─────────────────────┴─────────────┴──────────────────────────┘
                    Inter-service Communication
                      (via Feign Clients)
                              │
                    ┌─────────┴─────────┐
                    │   Social Layer     │
                    │   Comments, Likes  │
                    │   Reviews, Follows │
                    └───────────────────┘
```

---
## Prerequisites

Before setting up the Engagement Service, ensure you have:
1. **Java 21** installed
2. **Maven 3.8+** or use the included Maven wrapper
3. **Eureka Service Registry** running
4. **PostgreSQL 15+** (for engagement data storage)
5. **Redis** (for caching and real-time notifications)
6. **Elasticsearch** (optional, for advanced comment/review search)

---
## Step 1: Start Eureka Service Registry

**IMPORTANT**: The Eureka Service Registry must be running before starting any microservice.

```bash
# Clone the service registry repository
git clone https://github.com/phutruonnttn/yushan-microservices-service-registry
cd yushan-microservices-service-registry

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally
./mvnw spring-boot:run
```

### Verify Eureka is Running

- Open: http://localhost:8761
- You should see the Eureka dashboard

---

## Step 2: Clone the Engagement Service Repository

```bash
git clone https://github.com/phutruonnttn/yushan-microservices-engagement-service.git
cd yushan-microservices-engagement-service

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally (requires PostgreSQL 15 and Redis to be running beforehand)
./mvnw spring-boot:run
```

---

## Expected Output

### Console Logs (Success)

```
2024-10-16 10:30:15 - Starting EngagementServiceApplication
2024-10-16 10:30:18 - Tomcat started on port(s): 8084 (http)
2024-10-16 10:30:20 - DiscoveryClient_ENGAGEMENT-SERVICE/engagement-service:8084 - registration status: 204
2024-10-16 10:30:20 - Started EngagementServiceApplication in 9.1 seconds
```

### Eureka Dashboard

```
Instances currently registered with Eureka:
✅ ENGAGEMENT-SERVICE - 1 instance(s)
   Instance ID: engagement-service:8084
   Status: UP (1)
```

---

## API Endpoints

### Health Check
- **GET** `/api/v1/health` - Service health status

### Comments
- **POST** `/api/v1/comments` - Create a comment (one per chapter per user)
- **GET** `/api/v1/comments/{id}` - Get comment details
- **PUT** `/api/v1/comments/{id}` - Update comment (author only)
- **DELETE** `/api/v1/comments/{id}` - Delete comment (author or admin)
- **GET** `/api/v1/comments/chapter/{chapterId}` - Get chapter comments (with pagination)
- **GET** `/api/v1/comments/novel/{novelId}` - Get novel comments (across all chapters)
- **POST** `/api/v1/comments/{id}/like` - Like a comment
- **POST** `/api/v1/comments/{id}/unlike` - Unlike a comment
- **GET** `/api/v1/comments/my-comments` - Get current user's comments
- **GET** `/api/v1/comments/check/chapter/{chapterId}` - Check if user has commented on chapter
- **GET** `/api/v1/comments/chapter/{chapterId}/statistics` - Get chapter comment statistics

#### Admin Comment Endpoints
- **GET** `/api/v1/comments/admin/moderation` - List comments for moderation (with filters)
- **GET** `/api/v1/comments/admin/all` - List all comments (admin search/filter)
- **GET** `/api/v1/comments/admin/search` - Advanced search for comments
- **GET** `/api/v1/comments/admin/user/{userId}` - Get comments by specific user
- **GET** `/api/v1/comments/admin/statistics` - Get moderation statistics
- **DELETE** `/api/v1/comments/admin/{id}` - Delete any comment (admin only)
- **POST** `/api/v1/comments/admin/batch-delete` - Batch delete comments
- **DELETE** `/api/v1/comments/admin/user/{userId}/all` - Delete all user's comments
- **DELETE** `/api/v1/comments/admin/chapter/{chapterId}/all` - Delete all chapter comments
- **PATCH** `/api/v1/comments/admin/bulk-spoiler` - Bulk update spoiler status

### Reviews
- **POST** `/api/v1/reviews` - Create a review (one per novel per user)
- **GET** `/api/v1/reviews/{id}` - Get review details
- **PUT** `/api/v1/reviews/{id}` - Update review (author only)
- **DELETE** `/api/v1/reviews/{id}` - Delete review (author or admin)
- **GET** `/api/v1/reviews/novel/{novelId}` - Get novel reviews (with pagination)
- **GET** `/api/v1/reviews` - List all reviews (with filters and pagination)
- **POST** `/api/v1/reviews/{id}/like` - Like a review
- **POST** `/api/v1/reviews/{id}/unlike` - Unlike a review
- **GET** `/api/v1/reviews/my-reviews` - Get current user's reviews
- **GET** `/api/v1/reviews/my-reviews/novel/{novelId}` - Get user's review for a novel
- **GET** `/api/v1/reviews/check/{novelId}` - Check if user has reviewed a novel
- **GET** `/api/v1/reviews/novel/{novelId}/rating-stats` - Get novel rating statistics (admin only)

#### Admin Review Endpoints
- **GET** `/api/v1/reviews/admin/all` - List all reviews (admin view)
- **DELETE** `/api/v1/reviews/admin/{id}` - Delete any review (admin only)

### Votes
- **POST** `/api/v1/votes/novels/{novelId}` - Vote for a novel (can vote multiple times)
- **GET** `/api/v1/votes/users` - Get current user's vote history (with pagination)

### Reports
- **POST** `/api/v1/reports/novel/{novelId}` - Report a novel (one per novel per user)
- **POST** `/api/v1/reports/comment/{commentId}` - Report a comment (one per comment per user)
- **GET** `/api/v1/reports/my-reports` - Get current user's reports

#### Admin Report Endpoints
- **GET** `/api/v1/reports/admin` - Get all reports (with pagination and filtering)
- **GET** `/api/v1/reports/admin/{reportId}` - Get report details
- **PUT** `/api/v1/reports/admin/{reportId}/resolve` - Resolve a report

---

## Key Features

### 💬 Comment System
- One comment per chapter per user
- Comment editing and deletion (author only)
- Spoiler tags
- Comment moderation (admin)
- Like/unlike comments
- Comment statistics
- Advanced admin moderation tools
- Batch operations for moderation

### ⭐ Review System
- One review per novel per user
- Star ratings (1-5) integrated in reviews
- Written reviews with content
- Review editing and deletion (author only)
- Like/unlike reviews
- Review moderation (admin)
- Rating statistics aggregation

### 🗳️ Vote System
- Vote for novels (can vote multiple times)
- Vote history tracking
- Integration with Gamification Service (Yuan deduction)

### 🚨 Report System
- Report novels for inappropriate content
- Report comments for inappropriate content
- One report per content per user
- Admin report resolution workflow

---

## Database Schema

The Engagement Service uses the following key entities:

- **Comment** - User comments on chapters (one per chapter per user)
- **Review** - User reviews with ratings (one per novel per user)
- **Vote** - User votes for novels
- **Report** - Content reports for moderation (novels and comments)

---

## Next Steps

Once this basic setup is working:
1. ✅ Create database entities (Comment, Review, Rating, etc.)
2. ✅ Set up Flyway migrations
3. ✅ Create repositories and services
4. ✅ Implement API endpoints
5. ✅ Add Feign clients for inter-service communication
6. ✅ Set up Redis caching for hot content
7. ✅ Implement WebSocket for real-time notifications
8. ✅ Add content moderation system
9. ✅ Implement recommendation algorithm
10. ✅ Set up Elasticsearch for search (optional)

---

## Troubleshooting

**Problem: Service won't register with Eureka**
- Ensure Eureka is running: `docker ps`
- Check logs: Look for "DiscoveryClient" messages
- Verify defaultZone URL is correct

**Problem: Port 8084 already in use**
- Find process: `lsof -i :8084` (Mac/Linux) or `netstat -ano | findstr :8084` (Windows)
- Kill process or change port in application.yml

**Problem: Database connection fails**
- Verify PostgreSQL is running: `docker ps | grep yushan-postgres`
- Check database credentials in application.yml
- Test connection: `psql -h localhost -U yushan_engagement -d yushan_engagement`

**Problem: Redis connection fails**
- Verify Redis is running: `docker ps | grep redis`
- Check Redis connection: `redis-cli ping`
- Verify Redis host and port in application.yml

**Problem: Build fails**
- Ensure Java 21 is installed: `java -version`
- Check Maven: `./mvnw -version`
- Clean and rebuild: `./mvnw clean install -U`

**Problem: Notifications not working**
- Check WebSocket connection logs
- Verify Redis pub/sub is working
- Review notification service logs
- Check user notification preferences

**Problem: Comments not loading**
- Check database indexes on foreign keys
- Verify caching is working properly
- Review pagination parameters
- Check for query performance issues

---

## Performance Tips
1. **Caching**: Cache popular content (hot comments, top reviews) in Redis
2. **Pagination**: Always use pagination for lists and feeds
3. **Indexing**: Index foreign keys, timestamps, and user_id columns
4. **Rate Limiting**: Limit comment/review creation frequency
5. **Async Processing**: Use async for notifications and analytics events
6. **Read Replicas**: Use database read replicas for heavy read operations

---

## Content Moderation
The Engagement Service includes moderation features:
- **Automated Filtering**: Profanity filter, spam detection
- **User Reports**: Allow users to report inappropriate content
- **Admin Tools**: Review and moderate reported content
- **Shadow Banning**: Soft-ban users for violations
- **Content Guidelines**: Enforce community guidelines

---

## Real-Time Features
Using WebSockets and Redis Pub/Sub:
- Live comment updates
- Real-time notifications
- Live reading progress sync
- Instant like updates
- Live follower counts

---

## Inter-Service Communication

The Engagement Service communicates with:
- **User Service**: Fetch user profiles and preferences
- **Content Service**: Validate novels and chapters exist
- **Gamification Service**: Trigger points for engagement activities
- **Analytics Service**: Send engagement metrics

### FeignAuthConfig

The service uses `FeignAuthConfig` to forward authentication headers for inter-service calls:

**Priority**:
1. **Gateway-Validated Requests** (Preferred): If incoming request has `X-Gateway-Validated: true`, forward all gateway headers:
   - `X-Gateway-Validated: true`
   - `X-User-Id`, `X-User-Email`, `X-User-Username`, `X-User-Role`, `X-User-Status`
   - `X-Gateway-Timestamp`, `X-Gateway-Signature` (HMAC signature)
2. **Backward Compatibility**: If no gateway headers, forward `Authorization` header (JWT token)

This ensures that:
- Gateway-validated requests maintain their authentication context across services
- HMAC signatures are preserved for security verification
- User status is forwarded to prevent disabled users from accessing resources
- Direct service calls (bypassing Gateway) still work with JWT tokens

---

## 🔐 Authentication & Security

### Authentication Architecture

**JWT Validation is Centralized at API Gateway Level**

- **Primary Flow**: All requests must go through API Gateway which validates JWT tokens
- **Gateway-Validated Requests**: Service trusts requests with `X-Gateway-Validated: true` header
- **HMAC Signature Verification**: Service verifies HMAC-SHA256 signatures to prevent header forgery attacks
- **Backward Compatibility**: Service can still validate JWT tokens directly for inter-service calls

**Filter Chain**:
1. `GatewayAuthenticationFilter` - Processes gateway-validated requests with HMAC signature verification (preferred)
2. `JwtAuthenticationFilter` - Validates JWT tokens (backward compatibility)

**Security Features**:
- **HMAC Signature**: Gateway signs requests with HMAC-SHA256 using shared secret (`GATEWAY_HMAC_SECRET`)
- **Timestamp Validation**: Prevents replay attacks (5-minute tolerance)
- **Constant-Time Comparison**: Prevents timing attacks during signature verification
- **User Status Check**: `GatewayAuthenticationFilter` checks `X-User-Status` header to ensure user is active (`isEnabled()`)
- **Disabled User Rejection**: Disabled/suspended users are rejected with **403 Forbidden** response

### HMAC Configuration

Configure the shared secret for HMAC signature verification in `application.yml`:

```yaml
gateway:
  hmac:
    secret: ${GATEWAY_HMAC_SECRET:yushan-gateway-hmac-secret-key-for-request-signature-2024}
```

**Important**: The same secret must be configured in API Gateway and all microservices.

**Environment Variable**:
- `GATEWAY_HMAC_SECRET`: Shared secret for HMAC signature verification (must match Gateway)

### Security Considerations
- Validate content ownership before updates/deletes
- Implement rate limiting on write operations
- Sanitize user input to prevent XSS
- Use prepared statements to prevent SQL injection
- Implement CSRF protection
- Validate user permissions for private content
- Monitor for spam and abuse patterns

---

## Event Publishing
The Engagement Service publishes events for:
- Comment created/deleted
- Review posted
- Rating given
- Novel followed
- Reading progress updated

These events are consumed by Analytics and Gamification services.

---

## Monitoring
The Engagement Service exposes metrics through:
- Spring Boot Actuator endpoints (`/actuator/metrics`)
- Custom engagement metrics (comments/reviews per hour)
- Notification delivery success rate
- Cache hit rates
- WebSocket connection count

---

## 📄 License

This project is part of the Yushan Platform ecosystem.

## 🔗 Links

- **API Gateway**: [yushan-microservices-api-gateway](https://github.com/phutruonnttn/yushan-microservices-api-gateway)
- **Service Registry**: [yushan-microservices-service-registry](https://github.com/phutruonnttn/yushan-microservices-service-registry)
- **Config Server**: [yushan-microservices-config-server](https://github.com/phutruonnttn/yushan-microservices-config-server)
- **Platform Documentation**: [yushan-platform-docs](https://github.com/phutruonnttn/yushan-platform-docs) - Complete documentation for all phases
- **Phase 2 Architecture**: See [Phase 2 Microservices Architecture](https://github.com/phutruonnttn/yushan-platform-docs/blob/main/docs/phase2-microservices/PHASE2_MICROSERVICES_ARCHITECTURE.md)
