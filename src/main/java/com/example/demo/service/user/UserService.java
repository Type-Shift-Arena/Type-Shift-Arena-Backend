/*
 * @Author: hiddenSharp429 z404878860@163.com
 * @Date: 2024-10-27 15:39:23
 * @LastEditors: hiddenSharp429 z404878860@163.com
 * @LastEditTime: 2024-11-10 10:41:52
 */
package com.example.demo.service.user;

import com.example.demo.controller.user.UserController;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidPasswordException;
import com.example.demo.exception.PasswordMismatchException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.dto.UpdatePasswordDTO;
import com.example.demo.model.dto.UpdateUserDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.entity.PlayerProfile;
import com.example.demo.repository.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        User savedUser = userRepository.save(user);

        // 创建并初始化玩家资料
        PlayerProfile profile = new PlayerProfile();
        profile.setUser(savedUser);
        playerProfileRepository.save(profile);

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 更新用户基础信息（用户名、邮箱、头像url）
    public User updateUserById(Long id, UpdateUserDTO updateUserDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updateUserDTO.getUsername());
            user.setEmail(updateUserDTO.getEmail());
            user.setImgSrc(updateUserDTO.getImgSrc());
            return userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
    }

    // 更新用户密码
    public void updatePassword(Long id, UpdatePasswordDTO updatePasswordDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 验证旧密码
            if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
                throw new InvalidPasswordException("旧密码不正确");
            }
            // 检查新密码与确认密码是否一致
            if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getConfirmPassword())) {
                throw new PasswordMismatchException("两次新密码不一致");
            }
            // 更新用户密码
            user.setPassword(encodePassword(updatePasswordDTO.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}