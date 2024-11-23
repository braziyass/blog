package com.emsi.blog.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.emsi.blog.user.Blog;
import com.emsi.blog.user.Comment;
import com.emsi.blog.user.Like;
import com.emsi.blog.user.User;
import com.emsi.blog.user.UserRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Blog> createBlog(@RequestBody String content) {
        Blog blog = blogService.createBlog(content);
        return ResponseEntity.ok(blog);
    }

    @PostMapping("/{blogId}/like")
    public ResponseEntity<Like> likeBlog(@PathVariable Long blogId, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Like like = blogService.likeBlog(blogId, user);
        return ResponseEntity.ok(like);
    }

    @PostMapping("/{blogId}/comment")
    public ResponseEntity<Comment> commentOnBlog(@PathVariable Long blogId, @RequestBody String content, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Comment comment = blogService.commentOnBlog(blogId, user, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping
    public ResponseEntity<List<Blog>> getAllBlogs() {
        List<Blog> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blog);
    }

    @GetMapping("/{blogId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long blogId) {
        List<Comment> comments = blogService.getComments(blogId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{blogId}/likes")
    public ResponseEntity<List<Like>> getLikes(@PathVariable Long blogId) {
        List<Like> likes = blogService.getLikes(blogId);
        return ResponseEntity.ok(likes);
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}