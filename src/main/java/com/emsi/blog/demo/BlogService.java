package com.emsi.blog.demo;

import lombok.RequiredArgsConstructor;

import com.emsi.blog.config.JwtService;
import com.emsi.blog.dto.BlogDTO;
import com.emsi.blog.dto.CommentDTO;
import com.emsi.blog.dto.LikeDTO;
import com.emsi.blog.user.*;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public BlogDTO createBlog(String content, String token) {
        User user = getUserFromToken(token);
        Blog blog = Blog.builder()
                .content(content)
                .numberLikes(0)
                .numberComments(0)
                .user(user)
                .build();
        blogRepository.save(blog);
        return toBlogDTO(blog);
    }

    public BlogDTO updateBlog(Long blogId, String content, String token) {
        User user = getUserFromToken(token);
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().equals(user)) {
            throw new RuntimeException("You are not authorized to update this blog");
        }

        blog.setContent(content);
        blogRepository.save(blog);
        return toBlogDTO(blog);
    }

    public void deleteBlog(Long blogId, String token) {
        User user = getUserFromToken(token);
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().equals(user)) {
            throw new RuntimeException("You are not authorized to delete this blog");
        }

        blogRepository.delete(blog);
    }

    public Like likeBlog(Long blogId, String token) {
        User user = getUserFromToken(token);
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));

        if (likeRepository.existsByBlogAndUser(blog, user)) {
            throw new RuntimeException("User has already liked this post");
        }

        Like like = Like.builder()
                .blog(blog)
                .user(user)
                .build();
        blog.setNumberLikes(blog.getNumberLikes() + 1);
        blogRepository.save(blog);
        return likeRepository.save(like);
    }

    public void unlikeBlog(Long blogId, String token) {
        User user = getUserFromToken(token);
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));

        Like like = likeRepository.findByBlogAndUser(blog, user)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        likeRepository.delete(like);
        blog.setNumberLikes(blog.getNumberLikes() - 1);
        blogRepository.save(blog);
    }

    public Comment commentOnBlog(Long blogId, String content, String token) {
        User user = getUserFromToken(token);
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        Comment comment = Comment.builder()
                .blog(blog)
                .user(user)
                .content(content)
                .build();
        blog.setNumberComments(blog.getNumberComments() + 1);
        blogRepository.save(blog);
        return commentRepository.save(comment);
    }

    public List<CommentDTO> getComments(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        return blog.getComments().stream()
                .map(comment -> new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName()))
                .collect(Collectors.toList());
    }

    public List<LikeDTO> getLikes(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        return blog.getLikes().stream()
                .map(like -> new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName()))
                .collect(Collectors.toList());
    }

    public List<BlogDTO> getAllBlogs() {
        return blogRepository.findAll().stream()
                .map(blog -> new BlogDTO(
                        blog.getContent(),
                        blog.getUser().getFirstName(),
                        blog.getUser().getLastName(),
                        blog.getNumberLikes(),
                        blog.getNumberComments(),
                        blog.getComments().stream()
                                .map(comment -> new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName()))
                                .collect(Collectors.toList()),
                        blog.getLikes().stream()
                                .map(like -> new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public BlogDTO getBlogById(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        return new BlogDTO(
                blog.getContent(),
                blog.getUser().getFirstName(),
                blog.getUser().getLastName(),
                blog.getNumberLikes(),
                blog.getNumberComments(),
                blog.getComments().stream()
                        .map(comment -> new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName()))
                        .collect(Collectors.toList()),
                blog.getLikes().stream()
                        .map(like -> new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName()))
                        .collect(Collectors.toList())
        );
    }

    private BlogDTO toBlogDTO(Blog blog) {
        return new BlogDTO(
                blog.getContent(),
                blog.getUser().getFirstName(),
                blog.getUser().getLastName(),
                blog.getNumberLikes(),
                blog.getNumberComments(),
                blog.getComments().stream()
                        .map(comment -> new CommentDTO(comment.getContent(), comment.getUser().getFirstName(), comment.getUser().getLastName()))
                        .collect(Collectors.toList()),
                blog.getLikes().stream()
                        .map(like -> new LikeDTO(like.getUser().getFirstName(), like.getUser().getLastName()))
                        .collect(Collectors.toList())
        );
    }

    private User getUserFromToken(String token) {
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
}