package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Admin;
import com.sdjzuxg.collegemanagesystem.mapper.AdminMapper;
import com.sdjzuxg.collegemanagesystem.service.AdminService;
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    private AdminMapper adminMapper;

    @Override
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    @Override
    public Admin findById(Integer adminId) {
        return adminMapper.selectById(adminId);
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }

    @Override
    public boolean save(Admin admin) {
        // 新增管理员：密码为空则使用默认密码，统一 BCrypt 加密
        admin.setPassword(PasswordUtil.hash(admin.getPassword()));
        return adminMapper.insert(admin) > 0;
    }

    @Override
    public boolean update(Admin admin) {
        // 修改管理员：密码为空表示不改密码，查库回填原哈希，避免被清空
        if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
            Admin original = adminMapper.selectById(admin.getAdminId());
            if (original != null) {
                admin.setPassword(original.getPassword());
            }
        } else {
            // 传了新明文密码，BCrypt 加密
            admin.setPassword(PasswordUtil.hash(admin.getPassword()));
        }
        return adminMapper.update(admin) > 0;
    }

    @Override
    public boolean updatePassword(Integer adminId, String password) {
        // 改密码：BCrypt 加密后写入
        return adminMapper.updatePassword(adminId, PasswordUtil.hash(password)) > 0;
    }

    @Override
    public boolean deleteById(Integer adminId) {
        return adminMapper.deleteById(adminId) > 0;
    }
}
