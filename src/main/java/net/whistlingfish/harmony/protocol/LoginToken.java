package net.whistlingfish.harmony.protocol;

public class LoginToken {
    private final String accountId;
    private final String userAuthToken;

    public LoginToken(String username, String password) {
        this.accountId = username;
        this.userAuthToken = password;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUserAuthToken() {
        return userAuthToken;
    }
}
