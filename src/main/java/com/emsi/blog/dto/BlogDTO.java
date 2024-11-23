package com.emsi.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Getter
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