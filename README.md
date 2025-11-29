# Blog Application with JWT Authentication

Welcome to the Blog Application! This project is built using Spring Boot and JWT for authentication. It allows users to create, update, delete, like, and comment on blog posts. Users can also view their profile information.

## Features

- **User Authentication**: Secure authentication using JWT.
- **Create Blog Posts**: Users can create new blog posts.
- **Update Blog Posts**: Users can update their own blog posts.
- **Delete Blog Posts**: Users can delete their own blog posts.
- **Like Blog Posts**: Users can like blog posts. Each user can only like a post once.
- **Unlike Blog Posts**: Users can remove their like from a blog post.
- **Comment on Blog Posts**: Users can comment on blog posts.
- **View Blog Posts**: Users can view all blog posts along with likes and comments.
- **View Profile**: Users can view their profile information.
- **Email verification**: New accounts receive a verification email (Mailtrap used for testing). Users must verify their account before obtaining a JWT.
- **Opaque public IDs for blogs**: All blog resource URLs use a non-sequential publicId (UUID) instead of database numeric IDs for security.
- **API documentation**: Swagger UI available at `/swagger-ui.html` and `/swagger-ui/index.html`.
- **Redis caching**: Blog data is cached using Redis for improved performance.

## Project Structure

The application follows a modular configuration approach:

- **AuthenticationConfig**: Authentication beans (UserDetailsService, AuthenticationProvider, AuthenticationManager, PasswordEncoder)
- **SecurityConfig**: HTTP security configuration, security filter chain, and CORS settings
- **CustomAuthenticationEntryPoint**: Handles unauthenticated requests with smart detection for browser vs API clients
- **JwtAuthenticationFilter**: JWT token validation filter that intercepts requests
- **RedisConfig**: Redis connection and caching configuration with support for Redis URL parsing
- **JacksonConfig**: Shared ObjectMapper configuration for JSON serialization
- **ApplicationConfig**: Application-wide configuration (currently empty, ready for future beans)

### Architecture Diagrams

Visual documentation of the application is available in PlantUML format in the `/docs` directory:

- **architecture-overview.puml**: Complete system architecture showing all layers and components
- **authentication-flow.puml**: Detailed authentication, registration, and verification flows
- **blog-operations-flow.puml**: Blog CRUD operations, likes, and comments with caching
- **security-flow.puml**: JWT validation and security filter chain
- **caching-strategy.puml**: Redis caching patterns and cache invalidation
- **database-schema.puml**: Entity-relationship diagram of the database

To view these diagrams:
1. Install a PlantUML viewer in your IDE (VS Code, IntelliJ, etc.)
2. Or use online tools like http://www.plantuml.com/plantuml/
3. Or generate PNG/SVG using PlantUML CLI

## Endpoints

### Authentication

- **Register**: `/api/auth/register`
- **Login**: `/api/auth/authenticate`
- **Verify**: `/api/auth/verify?token=<verification-token>`
- Note: Endpoints are implemented under `/api/auth` in the application.

### Blog Posts

- **Create Blog Post**: `POST /api/blogs`
- **Update Blog Post**: `PUT /api/blogs/{publicId}`
- **Delete Blog Post**: `DELETE /api/blogs/{publicId}`
- **Get All Blog Posts**: `GET /api/blogs`
- **Get Blog Post by Public ID**: `GET /api/blogs/{publicId}`
- **Get Blogs by User ID**: `GET /api/blogs/{userId}/blog`
- **Get My Blogs**: `GET /api/blogs/my-blogs` (requires Authorization header)

### Likes

- **Like Blog Post**: `POST /api/blogs/{publicId}/like`
- **Unlike Blog Post**: `DELETE /api/blogs/{publicId}/like`
- **Get Likes for Blog Post**: `GET /api/blogs/{publicId}/likes`

### Comments

- **Comment on Blog Post**: `POST /api/blogs/{publicId}/comment`
- **Get Comments for Blog Post**: `GET /api/blogs/{publicId}/comments`

### Profile

- **Get User Profile**: `GET /api/blogs/profile`
- **Update User Profile**: `PUT /api/blogs/profile`
  - Request body: UserDTO (firstName, lastName, email)
  - Response: UpdatedUserDTO (firstName, lastName, email, token)

### Documentation

- **Swagger UI**: `GET /swagger-ui.html` or `/swagger-ui/index.html`
- **OpenAPI JSON**: `GET /v3/api-docs`

## Data Transfer Objects (DTOs)

### BlogDTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogDTO {
    private String content;
    private String firstName;
    private String lastName;
    private int numberLikes;
    private int numberComments;
    private List<CommentDTO> comments;
    private List<LikeDTO> likes;
}
```

### CommentDTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private String content;
    private String firstName;
    private String lastName;
}
```

### LikeDTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {
    private String firstName;
    private String lastName;
}
```

### UserDTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
}
```

### UpdatedUserDTO

```java
@Data
@NorBuilder
@AllArgsConstructor
public class UpdatedUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String token; // JWT token returned after profile update
}
```

## How to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/braziyass/blog.git
   cd blog
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**:
   Open your browser and go to `http://localhost:8080`.

## Configuration

### Redis Configuration

Configure Redis connection in `application.yml` or via environment variables:

```yaml
spring:
  redis:
    url: redis://localhost:6379  # Full URL (optional, overrides host/port)
    host: localhost              # Redis host
    port: 6379                   # Redis port
    password: your-password      # Redis password (optional)
    username: default            # Redis username for ACL (optional)
    ssl: false                   # Enable SSL (true for rediss://)
    cache:
      ttl: 10                    # Cache TTL in minutes
```

The Redis configuration supports:
- Full Redis URLs (redis:// or rediss:// for SSL)
- Username/password authentication
- SSL/TLS connections
- Configurable cache TTL

### Mail / Verification settings
  - Configure Mailtrap SMTP in `src/main/resources/application.yml` or via environment variables:
    - spring.mail.username = `api` (Mailtrap live)
    - spring.mail.password = your Mailtrap API token (use an env var, e.g. `MAILTRAP_PASSWORD`)
    - spring.mail.from = `hello@demomailtrap.co` (use the sender Mailtrap recommends)
  - Example environment approach (recommended):
    export MAILTRAP_PASSWORD="your-token"
    and in application.yml: `spring.mail.password: ${MAILTRAP_PASSWORD}`
  - Behaviour:
    - POST /api/auth/register will create the user and attempt to send the verification email. If sending fails the API still returns a 201 with a message and the user record is stored (so you can resend later).
    - Use the verification link from Mailtrap inbox (or the token stored on the user record) to call GET /api/auth/verify?token=... — the endpoint verifies the account and returns a JWT you can use for protected endpoints.

## Technologies Used

- **Spring Boot**: For building the application.
- **Spring Security**: For authentication and authorization.
- **JWT**: For secure authentication tokens.
- **Lombok**: For reducing boilerplate code.
- **Hibernate**: For ORM (Object-Relational Mapping).
- **PostgreSQL**: As the database.
- **Redis**: For caching.
- **Lettuce**: Redis client for Spring Data Redis.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.

---

Happy coding! 🚀