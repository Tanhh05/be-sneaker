package com.sneakerstore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    /**
     * ID Token JWT do Google Identity Services trả về từ phía Client.
     * Backend sẽ gửi token này tới https://oauth2.googleapis.com/tokeninfo để xác thực thật.
     */
    private String idToken;

    /**
     * OAuth2 Access Token do Google OAuth Client trả về.
     * Backend sẽ gọi https://www.googleapis.com/oauth2/v3/userinfo để lấy thông tin.
     */
    private String accessToken;

    /**
     * Mã ủy quyền Google Authorization Code (được đổi sang Token bằng Client Secret).
     */
    private String authCode;

    /**
     * Redirect URI tương ứng nếu dùng Authorization Code flow.
     */
    private String redirectUri;

    /**
     * Email người dùng Google (tùy chọn nếu đã có idToken / accessToken / authCode).
     */
    private String email;

    /**
     * Họ tên người dùng Google.
     */
    private String fullName;

    /**
     * Google Subject ID (sub).
     */
    private String googleId;

    /**
     * Ảnh đại diện Google (picture).
     */
    private String avatarUrl;
}
