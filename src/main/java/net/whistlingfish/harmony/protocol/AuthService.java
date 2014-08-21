package net.whistlingfish.harmony.protocol;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.content;

/*-
 * FIXME: Get rid of Resty. And here's why:
 *
 *    public boolean status(int responseCode) {
 *        . . .
 *            try {
 *                return http.getResponseCode() == responseCode;
 *            } catch (IOException e) {
 *                e.printStackTrace();    <--- gah
 *                return false;
 *            }
 *        . . .
 *    }
 */
public class AuthService {
    private static Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String ENDPOINT = "https://svcs.myharmony.com/CompositeSecurityServices/Security.svc/json/GetUserAuthToken";

    public LoginToken getLoginToken(String username, String password) {
        try {
            JSONResource response = new Resty().json(ENDPOINT, content(new JSONObject(new AuthDetails(username,
                    password))));
            if (!response.status(200)) {
                throw new AuthFailedException(response.object().toString());
            }
            logger.debug("auth token request returned: {}", response.object());

            return new LoginToken(response.object().getJSONObject("GetUserAuthTokenResult"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Logitech auth service", e);
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse response from Logitech auth service", e);
        }
    }
}
