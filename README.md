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

## Technologies Used

- **Spring Boot**: For building the application.
- **Spring Security**: For authentication and authorization.
- **JWT**: For secure authentication tokens.
- **Lombok**: For reducing boilerplate code.
- **Hibernate**: For ORM (Object-Relational Mapping).
- **PostgreSQL**: As the database.

## Configuration

- Mail / Verification settings
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

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.

---

Happy coding! 🚀