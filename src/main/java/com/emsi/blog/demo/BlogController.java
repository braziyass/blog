package com.emsi.blog.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.emsi.blog.dto.BlogDTO;
import com.emsi.blog.dto.CommentDTO;
import com.emsi.blog.dto.LikeDTO;
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

    @PutMapping("/{blogId}")
    public ResponseEntity<BlogDTO> updateBlog(@PathVariable Long blogId, @RequestBody String content, HttpServletRequest request) {
        String token = extractToken(request);
        BlogDTO blogDTO = blogService.updateBlog(blogId, content, token);
        return ResponseEntity.ok(blogDTO);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long blogId, HttpServletRequest request) {
        String token = extractToken(request);
        blogService.deleteBlog(blogId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blogId}/like")
    public ResponseEntity<LikeDTO> likeBlog(@PathVariable Long blogId, HttpServletRequest request) {
        String token = extractToken(request);
        Like like = blogService.likeBlog(blogId, token);
        LikeDTO likeDTO = new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName());
        return ResponseEntity.ok(likeDTO);
    }

    @DeleteMapping("/{blogId}/like")
    public ResponseEntity<Void> unlikeBlog(@PathVariable Long blogId, HttpServletRequest request) {
        String token = extractToken(request);
        blogService.unlikeBlog(blogId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blogId}/comment")
    public ResponseEntity<CommentDTO> commentOnBlog(@PathVariable Long blogId, @RequestBody String content, HttpServletRequest request) {
        String token = extractToken(request);
        Comment comment = blogService.commentOnBlog(blogId, content, token);
        CommentDTO commentDTO = new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName());
        return ResponseEntity.ok(commentDTO);
    }

    @GetMapping
    public ResponseEntity<List<BlogDTO>> getAllBlogs() {
        List<BlogDTO> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<BlogDTO> getBlogById(@PathVariable Long blogId) {
        BlogDTO blog = blogService.getBlogById(blogId);
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

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("JWT Token is missing");
    }
}