package com.emsi.blog.demo;

import lombok.RequiredArgsConstructor;

// import org.hibernate.sql.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// import com.emsi.blog.auth.AuthenticationResponse;
import com.emsi.blog.dto.BlogDTO;
import com.emsi.blog.dto.CommentDTO;
import com.emsi.blog.dto.LikeDTO;
import com.emsi.blog.dto.UpdatedUserDTO;
import com.emsi.blog.dto.UserDTO;
import com.emsi.blog.user.Comment;
import com.emsi.blog.user.Like;
// import com.emsi.blog.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    // private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BlogDTO> createBlog(@RequestBody String content, HttpServletRequest request) {
        String token = extractToken(request);
        BlogDTO blogDTO = blogService.createBlog(content, token);
        return ResponseEntity.ok(blogDTO);
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<BlogDTO> updateBlog(@PathVariable("publicId") String publicId, @RequestBody String content, HttpServletRequest request) {
        String token = extractToken(request);
        BlogDTO blogDTO = blogService.updateBlogByPublicId(publicId, content, token);
        return ResponseEntity.ok(blogDTO);
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteBlog(@PathVariable("publicId") String publicId, HttpServletRequest request) {
        String token = extractToken(request);
        blogService.deleteBlogByPublicId(publicId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{publicId}/like")
    public ResponseEntity<LikeDTO> likeBlog(@PathVariable("publicId") String publicId, HttpServletRequest request) {
        String token = extractToken(request);
        Like like = blogService.likeBlogByPublicId(publicId, token);
        LikeDTO likeDTO = new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName());
        return ResponseEntity.ok(likeDTO);
    }

    @DeleteMapping("/{publicId}/like")
    public ResponseEntity<Void> unlikeBlog(@PathVariable("publicId") String publicId, HttpServletRequest request) {
        String token = extractToken(request);
        blogService.unlikeBlogByPublicId(publicId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{publicId}/comment")
    public ResponseEntity<CommentDTO> commentOnBlog(@PathVariable("publicId") String publicId, @RequestBody String content, HttpServletRequest request) {
        String token = extractToken(request);
        Comment comment = blogService.commentOnBlogByPublicId(publicId, content, token);
        CommentDTO commentDTO = new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName());
        return ResponseEntity.ok(commentDTO);
    }

    @GetMapping
    public ResponseEntity<List<BlogDTO>> getAllBlogs() {
        List<BlogDTO> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<BlogDTO> getBlogByPublicId(@PathVariable("publicId") String publicId) {
        BlogDTO blog = blogService.getBlogByPublicId(publicId);
        return ResponseEntity.ok(blog);
    }

    @GetMapping("/{blogId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long blogId) {
        List<CommentDTO> comments = blogService.getComments(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{blogId}/likes")
    public ResponseEntity<List<LikeDTO>> getLikes(@PathVariable Long blogId) {
        List<LikeDTO> likes = blogService.getLikes(blogId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(HttpServletRequest request) {
        String token = extractToken(request);
        UserDTO userDTO = blogService.getUserProfile(token);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdatedUserDTO> updateProfile(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String token = extractToken(request);
        UpdatedUserDTO updatedUser = blogService.updateUserProfile(userDTO, token);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{userId}/blog")
    public ResponseEntity<List<BlogDTO>> getBlogsByUserId(@PathVariable Integer userId) {
        List<BlogDTO> blogs = blogService.getBlogsByUserId(userId);
        return ResponseEntity.ok(blogs);
        
    }
    

    @GetMapping("/my-blogs")
    public ResponseEntity<List<BlogDTO>> getMyBlogs(HttpServletRequest request) {
        String token = extractToken(request);
        List<BlogDTO> myBlogs = blogService.getMyBlogs(token);
        return ResponseEntity.ok(myBlogs);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new UnauthenticatedException("JWT Token is missing");
    }
}

// package-private exception used to signal missing authentication
class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String message) {
        super(message);
    }
}

// Global handler that redirects unauthenticated requests to the authenticate endpoint
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Void> handleUnauthenticated(UnauthenticatedException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "/api/auth/authenticate");
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect
    }
}