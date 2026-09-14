package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.MenuAction;
import com.sdjzuxg.collegemanagesystem.mapper.MenuActionMapper;
import com.sdjzuxg.collegemanagesystem.service.MenuActionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class MenuActionServiceImpl implements MenuActionService {

    @Resource
    private MenuActionMapper menuActionMapper;

    @Override
    public List<MenuAction> selectAll() {
        return menuActionMapper.selectAll();
    }

    @Override
    public MenuAction selectById(Integer actionId) {
        return menuActionMapper.selectById(actionId);
    }

    @Override
    public List<MenuAction> selectByMenuId(Integer menuId) {
        return menuActionMapper.selectByMenuId(menuId);
    }

    @Override
    public MenuAction selectByActionCode(String actionCode) {
        return menuActionMapper.selectByActionCode(actionCode);
    }

    @Override
    public int insert(MenuAction menuAction) {
        return menuActionMapper.insert(menuAction);
    }

    @Override
    public int update(MenuAction menuAction) {
        return menuActionMapper.update(menuAction);
    }

    @Override
    public int deleteById(Integer actionId) {
        return menuActionMapper.deleteById(actionId);
    }
}
