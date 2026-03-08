package com.survey.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String password;
    private String role;
    private Integer status;
}
