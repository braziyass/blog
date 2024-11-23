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

## Endpoints

### Authentication

- **Register**: `/auth/register`
- **Login**: `/auth/login`

### Blog Posts

- **Create Blog Post**: `POST /api/blogs`
- **Update Blog Post**: `PUT /api/blogs/{blogId}`
- **Delete Blog Post**: `DELETE /api/blogs/{blogId}`
- **Get All Blog Posts**: `GET /api/blogs`
- **Get Blog Post by ID**: `GET /api/blogs/{blogId}`

### Likes

- **Like Blog Post**: `POST /api/blogs/{blogId}/like`
- **Unlike Blog Post**: `DELETE /api/blogs/{blogId}/like`
- **Get Likes for Blog Post**: `GET /api/blogs/{blogId}/likes`

### Comments

- **Comment on Blog Post**: `POST /api/blogs/{blogId}/comment`
- **Get Comments for Blog Post**: `GET /api/blogs/{blogId}/comments`

### Profile

- **Get User Profile**: `GET /api/blogs/profile`

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

## How to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/blog.git
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

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.

---

Happy coding! 🚀