package com.jombay.learnr.pojos;

/**
 * Created by Rishab on 25-09-2017.
 * Represents access token which
 * is expired after certain time
 * interval and can be refreshed
 * using its refresh token
 */

public class AccessToken
{
    private String access_token, refresh_token;
    private long expires_in, created_at;

    public String getAccess_token()
    {
        return access_token;
    }

    public String getRefresh_token()
    {
        return refresh_token;
    }

    public long getExpires_in()
    {
        return expires_in;
    }

    public long getCreated_at()
    {
        return created_at;
    }
}
