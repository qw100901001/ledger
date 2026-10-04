package org.soso.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需在2-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 2, max = 20, message = "密码长度需在2-20之间")
    private String password;
    
    @Size(min = 1, max = 20, message = "昵称长度需在 1-20 之间")
    private String nickname;
}
