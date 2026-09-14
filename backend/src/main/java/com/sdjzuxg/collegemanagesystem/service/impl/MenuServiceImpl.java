package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Menu;
import com.sdjzuxg.collegemanagesystem.mapper.MenuMapper;
import com.sdjzuxg.collegemanagesystem.service.MenuService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {
    @Resource
    private MenuMapper menuMapper;

    @Override
    public List<Menu> findAll() {
        return menuMapper.selectAll();
    }

    @Override
    public Menu findById(Integer menuId) {
        return menuMapper.selectById(menuId);
    }

    @Override
    public boolean save(Menu menu) {
        return menuMapper.insert(menu) > 0;
    }

    @Override
    public boolean update(Menu menu) {
        return menuMapper.update(menu) > 0;
    }

    @Override
    public boolean deleteById(Integer menuId) {
        return menuMapper.deleteById(menuId) > 0;
    }

    @Override
    public List<Menu> findByRoleId(Integer roleId) {
        return menuMapper.selectByRoleId(roleId);
    }
}
