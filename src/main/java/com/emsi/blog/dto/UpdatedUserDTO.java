package com.emsi.blog.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String token;

}
