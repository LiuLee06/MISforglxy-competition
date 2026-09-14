package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class Admin {
    private Integer adminId;
    private String username;
    private String password;
    private String phone;
    private String email;
    private Role role;
}
