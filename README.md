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
- **Profile Pictures**: Users can upload profile pictures to AWS S3.
- **Email verification**: New accounts receive a verification email (Mailtrap used for testing). Users must verify their account before obtaining a JWT.
- **Opaque public IDs for blogs**: All blog resource URLs use a non-sequential publicId (UUID) instead of database numeric IDs for security.
- **API documentation**: Swagger UI available at `/swagger-ui.html` and `/swagger-ui/index.html`.
- **Redis caching**: Blog data is cached using Redis for improved performance.

## Project Structure

The application follows a modular configuration approach:

- **SecurityConfig**: Complete security configuration including authentication beans (UserDetailsService, AuthenticationProvider, PasswordEncoder), HTTP security rules, JWT filter, and CORS settings
- **CustomAuthenticationEntryPoint**: Handles unauthenticated requests with smart detection for browser vs API clients
- **RedisConfig**: Redis connection and caching configuration with support for Redis URL parsing
- **JacksonConfig**: Shared ObjectMapper configuration for JSON serialization
- **S3Config**: AWS S3 client configuration for file storage
- **ApplicationConfig**: Application-wide configuration (currently empty, ready for future beans)

## Endpoints

### Authentication

- **Register**: `POST /api/auth/register`
- **Login**: `POST /api/auth/authenticate`
- **Verify**: `GET /api/auth/verify?token=<verification-token>`

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

### File Upload

- **Upload General File**: `POST /api/files/upload`
- **Upload Profile Picture**: `POST /api/files/profile-picture` (requires authentication)
- **Delete File**: `DELETE /api/files/delete?fileName={fileName}`

### Documentation

- **Swagger UI**: `GET /swagger-ui.html` or `/swagger-ui/index.html`
- **OpenAPI JSON**: `GET /v3/api-docs`

## API Usage Examples

### 1. Register a New User

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": null,
  "message": "Verification email sent. Please check your inbox."
}
```

### 2. Verify Account

```http
GET http://localhost:8080/api/auth/verify?token=your-verification-token-from-email
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Account verified. Use this token to authenticate requests."
}
```

### 3. Login

```http
POST http://localhost:8080/api/auth/authenticate
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Authenticated"
}
```

### 4. Upload Profile Picture

**Using Postman/Insomnia:**
```http
POST http://localhost:8080/api/files/profile-picture
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

file: [select an image file]
```

**Response:**
```json
{
  "url": "https://amzn-s3-bucket-blog.s3.eu-north-1.amazonaws.com/profile-pictures/1/uuid-filename.jpg",
  "message": "Profile picture updated successfully",
  "userId": 1
}
```

**Constraints:**
- Only image files allowed (image/jpeg, image/png, image/gif, etc.)
- Maximum file size: 5MB
- Requires JWT authentication
- Automatically deletes old profile picture when uploading a new one

### 5. Create a Blog Post

```http
POST http://localhost:8080/api/blogs
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "content": "This is my first blog post!"
}
```

### 6. Get All Blogs

```http
GET http://localhost:8080/api/blogs
```

### 7. Like a Blog Post

```http
POST http://localhost:8080/api/blogs/{publicId}/like
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 8. Comment on a Blog Post

```http
POST http://localhost:8080/api/blogs/{publicId}/comment
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "content": "Great post!"
}
```

### 9. Get User Profile

```http
GET http://localhost:8080/api/blogs/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

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

### AWS S3 Configuration

**Setup Instructions:**

1. **Create S3 Bucket** (if not exists):
   - Log into AWS Console: https://console.aws.amazon.com/s3
   - Create bucket: `amzn-s3-bucket-blog`
   - Region: `Europe (Stockholm) eu-north-1`

2. **Add IAM Permissions**:
   - Go to IAM → Users → blog-application
   - Add permissions → Attach policies
   - Use this custom policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::amzn-s3-bucket-blog",
                "arn:aws:s3:::amzn-s3-bucket-blog/*"
            ]
        }
    ]
}
```

3. **Configure Application**:

AWS credentials are in `application.properties`:
- Region: `eu-north-1`
- Bucket: `amzn-s3-bucket-blog`
- Access credentials stored directly (learning project only)

**⚠️ Security Note:** Never commit AWS credentials to Git in production.

**Profile Picture Storage:**
- Files are stored at: `s3://amzn-s3-bucket-blog/profile-pictures/{userId}/{uuid}-{filename}`
- Each user can have one profile picture (old ones are automatically deleted)
- Supported formats: JPEG, PNG, GIF, and other image formats
- Maximum file size: 5MB

**Testing with REST Client (VS Code Extension):**

Create a file `test-api.http`:

```http
### Register
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@example.com",
  "password": "password123"
}

### Verify (replace token with actual token from email)
GET http://localhost:8080/api/auth/verify?token=your-token-here

### Login
POST http://localhost:8080/api/auth/authenticate
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

### Upload Profile Picture (replace token)
POST http://localhost:8080/api/files/profile-picture
Authorization: Bearer your-jwt-token-here
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="profile.jpg"
Content-Type: image/jpeg

< ./path/to/your/image.jpg
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

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
- **AWS S3**: For file storage (profile pictures).
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