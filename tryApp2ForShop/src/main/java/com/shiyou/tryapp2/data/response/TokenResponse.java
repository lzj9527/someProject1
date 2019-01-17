package com.shiyou.tryapp2.data.response;

import android.extend.data.BaseData;

public class TokenResponse extends BaseResponse {
    public TokenInfo tokenInfo;
    public static class TokenInfo extends BaseData
    {
        public String token;
    }
}
