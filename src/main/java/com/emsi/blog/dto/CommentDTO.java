package com.emsi.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentDTO {
    // private Long id;
    private String content;
    private String firstName;
    private String lastName;
}