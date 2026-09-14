package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Admin;

import java.util.List;

public interface AdminService {
    List<Admin> findAll();
    Admin findById(Integer adminId);
    Admin findByUsername(String username);
    boolean save(Admin admin);
    boolean update(Admin admin);
    boolean updatePassword(Integer adminId, String password);
    boolean deleteById(Integer adminId);
}
