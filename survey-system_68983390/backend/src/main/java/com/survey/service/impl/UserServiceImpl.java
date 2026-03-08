package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.dto.LoginRequest;
import com.survey.dto.RegisterRequest;
import com.survey.dto.UserUpdateDTO;
import com.survey.entity.User;
import com.survey.mapper.UserMapper;
import com.survey.security.JwtUtil;
import com.survey.service.UserService;
import com.survey.util.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Result<Map<String, Object>> login(LoginRequest request) {
        User user = this.lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return Result.success("登录成功", data);
    }

    @Override
    public Result<?> register(RegisterRequest request) {
        long count = this.lambdaQuery().eq(User::getUsername, request.getUsername()).count();
        if (count > 0) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        this.save(user);
        return Result.success("注册成功", null);
    }

    @Override
    public IPage<User> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Result<?> updateUser(Long id, UserUpdateDTO dto) {
        User user = this.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (StringUtils.hasText(dto.getRole())) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        this.updateById(user);
        return Result.success();
    }

    @Override
    public Result<?> deleteUser(Long id) {
        this.removeById(id);
        return Result.success();
    }

    @Override
    public Result<?> toggleStatus(Long id) {
        User user = this.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        this.updateById(user);
        return Result.success();
    }
}
