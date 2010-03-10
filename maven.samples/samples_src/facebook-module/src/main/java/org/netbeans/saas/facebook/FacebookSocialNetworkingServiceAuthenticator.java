/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.saas.facebook;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.saas.RestConnection;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author lukas
 */
public class FacebookSocialNetworkingServiceAuthenticator {

    private static String _apiKey;
    private static String _secret;
    private static String sessionKey;
    private static String sessionSecret;
    private static final String PROP_FILE = FacebookSocialNetworkingServiceAuthenticator.class.getSimpleName().toLowerCase() + ".properties"; //NOI18N

    static {
        try {
            Properties props = new Properties();
            props.load(FacebookSocialNetworkingServiceAuthenticator.class.getResourceAsStream(PROP_FILE));
            _apiKey = props.getProperty("api_key"); //NOI18N
            _secret = props.getProperty("secret"); //NOI18N
        } catch (IOException ex) {
            Logger.getLogger(FacebookSocialNetworkingServiceAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Preferences pref = NbPreferences.forModule(FacebookSocialNetworkingServiceAuthenticator.class);
        sessionKey = pref.get("sessionKey", null); //NOI18N
        sessionSecret = pref.get("sessionSecret", null); //NOI18N
    }

    public static String getApiKey() throws IOException {
        if (_apiKey == null || _apiKey.length() == 0) {
            throw new IOException("Please specify your api key and secret in the " + PROP_FILE + " file."); //NOI18N
        }
        return _apiKey;
    }

    public static String getSessionKey() throws IOException {
        if (sessionKey == null || sessionKey.length() == 0) {
            throw new IOException("Failed to get a valid session key."); //NOI18N
        }
        return sessionKey;
    }

    private static String getSecret() throws IOException {
        if (_secret == null || _secret.length() == 0) {
            throw new IOException("Please specify your secret in the " + PROP_FILE + " file."); //NOI18N
        }
        return _secret;
    }

    private static String getSessionSecret() throws IOException {
        if (sessionSecret == null || sessionSecret.length() == 0) {
            throw new IOException("Failed to get a valid session secret."); //NOI18N
        }
        return sessionSecret;
    }

    public static void login() throws IOException {
        if (sessionKey == null) {
            String token = getToken();

            String method = "facebook.auth.getSession"; //NOI18N
            String v = "1.0"; //NOI18N
            String apiKey = getApiKey();
            String secret = getSecret();

            String sig = sign(secret,
                    new String[][]{
                        {"method", method}, //NOI18N
                        {"v", v}, //NOI18N
                        {"api_key", apiKey}, //NOI18N
                        {"auth_token", token} //NOI18N
                    });

            RestConnection conn = new RestConnection(
                    "http://api.facebook.com/restserver.php", //NOI18N
                    new String[][]{
                        {"method", method}, //NOI18N
                        {"api_key", apiKey}, //NOI18N
                        {"sig", sig}, //NOI18N
                        {"v", v}, //NOI18N
                        {"auth_token", token} //NOI18N
                    });

            String result = conn.get().getDataAsString();

            try {
                sessionKey = result.substring(result.indexOf("<session_key>") + 13, //NOI18N
                        result.indexOf("</session_key>")); //NOI18N

                sessionSecret = result.substring(result.indexOf("<secret>") + 8, //NOI18N
                        result.indexOf("</secret>")); //NOI18N
            } catch (Exception ex) {
                throw new IOException("Failed to get session key and secret: " + result); //NOI18N
            }
            Preferences pref = NbPreferences.forModule(FacebookSocialNetworkingServiceAuthenticator.class);
            pref.put("sessionKey", sessionKey); //NOI18N
            pref.put("sessionSecret", sessionSecret); //NOI18N
        }
    }

    public static void logout() {
        FacebookSocialNetworkingService.logout();
        Preferences pref = NbPreferences.forModule(FacebookSocialNetworkingServiceAuthenticator.class);
        pref.remove("sessionKey"); //NOI18N
        pref.remove("sessionSecret"); //NOI18N
        sessionKey = null;
        sessionSecret = null;
    }

    private static String getToken() throws IOException {
        String token = null;
        String method = "facebook.auth.createToken"; //NOI18N
        String v = "1.0"; //NOI18N
        String apiKey = getApiKey();
        String secret = getSecret();

        String sig = sign(secret,
                new String[][]{
                    {"method", method}, //NOI18N
                    {"api_key", apiKey}, //NOI18N
                    {"v", v} //NOI18N
                });

        RestConnection conn = new RestConnection(
                "http://api.facebook.com/restserver.php", //NOI18N
                new String[][]{
                    {"method", method}, //NOI18N
                    {"api_key", apiKey}, //NOI18N
                    {"sig", sig}, //NOI18N
                    {"v", v} //NOI18N
                });
        String result = conn.get().getDataAsString();

        try {
            token = result.substring(result.indexOf("<auth_createToken_response"), //NOI18N
                    result.indexOf("</auth_createToken_response>")); //NOI18N
            token = token.substring(token.indexOf(">") + 1); //NOI18N
        } catch (Exception ex) {
            throw new IOException("Failed to get session token: " + result); //NOI18N
        }

        String loginUrl = "http://www.facebook.com/login.php?api_key=" //NOI18N
                + apiKey + "&v=" + v + "&auth_token=" + token + "&req_perms=publish_stream"; //NOI18N

        URLDisplayer.getDefault().showURL(new URL(loginUrl));

        final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(FacebookSocialNetworkingServiceAuthenticator.class, "MSG_allow"),
                NbBundle.getMessage(FacebookSocialNetworkingServiceAuthenticator.class, "TTL_allow"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);

        return token;
    }

    public static String sign(String[][] params) throws IOException {
        return sign(getSessionSecret(), params);
    }

    private static String sign(String secret, String[][] params) throws IOException {
        try {
            TreeMap<String, String> map = new TreeMap<String, String>();

            for (int i = 0; i
                    < params.length; i++) {
                String key = params[i][0];
                String value = params[i][1];

                if (value != null) {
                    map.put(key, value);
                }
            }

            String signature = ""; //NOI18N
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                signature += entry.getKey() + "=" + entry.getValue(); //NOI18N
            }

            signature += secret;

            MessageDigest md = MessageDigest.getInstance("MD5"); //NOI18N
            byte[] sum = md.digest(signature.getBytes("UTF-8")); //NOI18N
            BigInteger bigInt = new BigInteger(1, sum);

            return bigInt.toString(16);
        } catch (Exception ex) {
            Logger.getLogger(FacebookSocialNetworkingServiceAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
