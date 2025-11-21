package com.xianzhuan.controller.admin;

import com.xianzhuan.result.Result;
import com.xianzhuan.service.TencentService;
import com.xianzhuan.utils.LocalFileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommonController 单元测试")
class CommonControllerTest {

    @Mock
    private TencentService tencent;

    @Mock
    private LocalFileUtil localFileUtil;

    @InjectMocks
    private CommonController commonController;

    @Test
    @DisplayName("文件上传 - 成功")
    void upload_success() throws Exception {
        // 1. 准备数据
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        when(tencent.uploadImage(any(), anyString())).thenReturn("https://cos.example.com/test.jpg");

        // 2. 调用方法
        Result<String> result = commonController.upload(file);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(tencent).uploadImage(any(), anyString());
    }
}