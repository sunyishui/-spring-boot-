package com.survey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.dto.LoginRequest;
import com.survey.dto.RegisterRequest;
import com.survey.dto.UserUpdateDTO;
import com.survey.entity.User;
import com.survey.util.Result;

import java.util.Map;

public interface UserService extends IService<User> {
    Result<Map<String, Object>> login(LoginRequest request);
    Result<?> register(RegisterRequest request);
    IPage<User> listUsers(int page, int size, String keyword);
    Result<?> updateUser(Long id, UserUpdateDTO dto);
    Result<?> deleteUser(Long id);
    Result<?> toggleStatus(Long id);
}
