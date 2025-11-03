package com.xianzhuan.service;

import com.xianzhuan.dto.UserLoginDTO;
import com.xianzhuan.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
