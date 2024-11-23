package com.emsi.blog.demo;

import lombok.RequiredArgsConstructor;

import com.emsi.blog.user.Blog;
import com.emsi.blog.user.BlogRepository;
import com.emsi.blog.user.Comment;
import com.emsi.blog.user.LikeRepository;
import com.emsi.blog.user.User;
import com.emsi.blog.user.CommentRepository;
import com.emsi.blog.user.Like;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public Blog createBlog(String content) {
        Blog blog = Blog.builder()
                .content(content)
                .numberLikes(0)
                .numberComments(0)
                .build();
        return blogRepository.save(blog);
    }

    public Like likeBlog(Long blogId, User user) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        Like like = Like.builder()
                .blog(blog)
                .user(user)
                .build();
        blog.setNumberLikes(blog.getNumberLikes() + 1);
        blogRepository.save(blog);
        return likeRepository.save(like);
    }

    public Comment commentOnBlog(Long blogId, User user, String content) {
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

    public List<Comment> getComments(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        return blog.getComments();
    }

    public List<Like> getLikes(Long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new RuntimeException("Blog not found"));
        return blog.getLikes();
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    
}