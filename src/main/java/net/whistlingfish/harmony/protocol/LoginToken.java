package net.whistlingfish.harmony.protocol;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class LoginToken {
    private final String accountId;
    private final String userAuthToken;

    public LoginToken(String username, String password) {
        this.accountId = username;
        this.userAuthToken = password;
    }

    public LoginToken(JSONObject object) throws JSONException {
        this.accountId = object.getString("AccountId");
        this.userAuthToken = object.getString("UserAuthToken");
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUserAuthToken() {
        return userAuthToken;
    }
}
