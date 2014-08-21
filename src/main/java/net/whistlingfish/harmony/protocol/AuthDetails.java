package net.whistlingfish.harmony.protocol;

public class AuthDetails {
    private final String email;
    private final String password;

    public AuthDetails(String username, String password) {
        this.email = username;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
