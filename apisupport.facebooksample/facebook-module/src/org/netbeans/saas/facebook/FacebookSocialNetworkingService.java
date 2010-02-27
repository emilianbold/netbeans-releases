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

import facebook.socialnetworkingservice.facebookresponse.AuthExpireSessionResponse;
import facebook.socialnetworkingservice.facebookresponse.ErrorResponse;
import facebook.socialnetworkingservice.facebookresponse.User;
import facebook.socialnetworkingservice.facebookresponse.UsersGetLoggedInUserResponse;
import facebook.socialnetworkingservice.facebookresponse.UsersGetinfoResponse;
import facebook.socialnetworkingservice.facebookresponse.UsersSetStatusResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.saas.RestConnection;
import org.netbeans.saas.RestResponse;

/**
 * FacebookSocialNetworkingService Service
 *
 * @author lukas
 */
public class FacebookSocialNetworkingService {

    private static final Logger L = Logger.getLogger(FacebookSocialNetworkingService.class.getName());

    /** Creates a new instance of FacebookSocialNetworkingService */
    public FacebookSocialNetworkingService() {
    }

    public static User getUserInfo() {
        try {
            String format = null;

            RestResponse result = usersGetLoggedInUser(format);
            L.log(Level.FINE, "users.GetLoggedInUser: {0}", result.getDataAsString());
            if (result.getDataAsObject(UsersGetLoggedInUserResponse.class) instanceof UsersGetLoggedInUserResponse) {
                UsersGetLoggedInUserResponse rsp = result.getDataAsObject(UsersGetLoggedInUserResponse.class);

                try {
                    String uids = String.valueOf(rsp.getValue());
                    String fields = "name,pic,status";
                    String format1 = null;

                    RestResponse result1 = usersGetinfo(uids, fields, format1);
                    L.log(Level.FINE, "users.getInfo: {0}", result1.getDataAsString());
                    if (result1.getDataAsObject(UsersGetinfoResponse.class) instanceof UsersGetinfoResponse) {
                        UsersGetinfoResponse result1Obj = result1.getDataAsObject(UsersGetinfoResponse.class);
                        return result1Obj.getUser().get(0);
                    } else if (result1.getDataAsObject(ErrorResponse.class) instanceof ErrorResponse) {
                        ErrorResponse result1Obj = result1.getDataAsObject(ErrorResponse.class);
                    }
                } catch (Exception ex) {
                    L.log(Level.SEVERE, ex.getMessage(), ex);
                }
            } else if (result.getDataAsObject(ErrorResponse.class) instanceof ErrorResponse) {
                ErrorResponse resultObj = result.getDataAsObject(ErrorResponse.class);
            }
        } catch (Exception ex) {
            L.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    public static void updateStatus(String text) {
        try {
            String format = null;
            String status = text;
            String clear = String.valueOf(text == null);
            String statusIncludesVerb = "false";

            RestResponse result = usersSetStatus(format, status, clear, statusIncludesVerb);
            L.log(Level.FINE, "users.setStatus: {0}", result.getDataAsString());
            if (result.getDataAsObject(UsersSetStatusResponse.class) instanceof UsersSetStatusResponse) {
                UsersSetStatusResponse resultObj = result.getDataAsObject(UsersSetStatusResponse.class);
            } else if (result.getDataAsObject(ErrorResponse.class) instanceof ErrorResponse) {
                ErrorResponse resultObj = result.getDataAsObject(ErrorResponse.class);
            }
        } catch (Exception ex) {
            L.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public static void logout() {
        try {
            String format = null;

            RestResponse result = authExpireSession(format);
            L.log(Level.SEVERE, "auth.expireSession: {0}", result.getDataAsString());

            if (result.getDataAsObject(AuthExpireSessionResponse.class) instanceof AuthExpireSessionResponse) {
                AuthExpireSessionResponse resultObj = result.getDataAsObject(AuthExpireSessionResponse.class);
                if (1 != resultObj.getValue()) {
                    throw new RuntimeException("Cannot sign out");
                }
            } else if (result.getDataAsObject(ErrorResponse.class) instanceof ErrorResponse) {
                ErrorResponse resultObj = result.getDataAsObject(ErrorResponse.class);
            }
        } catch (Exception ex) {
            L.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param format
     * @return an instance of RestResponse
     */
    public static RestResponse authExpireSession(String format) throws IOException {
        String v = "1.0";
        String method = "facebook.auth.expireSession";
        FacebookSocialNetworkingServiceAuthenticator.login();
        String callId = String.valueOf(System.currentTimeMillis());
        String apiKey = FacebookSocialNetworkingServiceAuthenticator.getApiKey();
        String sessionKey = FacebookSocialNetworkingServiceAuthenticator.getSessionKey();
        String sig = FacebookSocialNetworkingServiceAuthenticator.sign(new String[][]{{"api_key", apiKey}, {"session_key", sessionKey}, {"call_id", callId}, {"v", v}, {"format", format}, {"method", method}});
        String[][] pathParams = new String[][]{};
        String[][] queryParams = new String[][]{{"api_key", "" + apiKey + ""}, {"session_key", sessionKey}, {"sig", sig}, {"call_id", callId}, {"v", v}, {"format", format}, {"method", method}};
        RestConnection conn = new RestConnection("http://api.facebook.com/restserver.php", pathParams, queryParams);
        sleep(1000);
        return conn.get(null);
    }

    //-------------------------------------------------------- generated methods
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable th) {
        }
    }

    /**
     *
     * @param format
     * @return an instance of RestResponse
     */
    public static RestResponse usersGetLoggedInUser(String format) throws IOException {
        String v = "1.0";
        String method = "facebook.users.getLoggedInUser";
        FacebookSocialNetworkingServiceAuthenticator.login();
        String callId = String.valueOf(System.currentTimeMillis());
        String apiKey = FacebookSocialNetworkingServiceAuthenticator.getApiKey();
        String sessionKey = FacebookSocialNetworkingServiceAuthenticator.getSessionKey();
        String sig = FacebookSocialNetworkingServiceAuthenticator.sign(new String[][]{{"api_key", apiKey}, {"session_key", sessionKey}, {"call_id", callId}, {"v", v}, {"format", format}, {"method", method}});
        String[][] pathParams = new String[][]{};
        String[][] queryParams = new String[][]{{"api_key", "" + apiKey + ""}, {"session_key", sessionKey}, {"call_id", callId}, {"sig", sig}, {"v", v}, {"format", format}, {"method", method}};
        RestConnection conn = new RestConnection("http://api.facebook.com/restserver.php", pathParams, queryParams);
        sleep(1000);
        return conn.get(null);
    }

    /**
     *
     * @param uids
     * @param fields
     * @param format
     * @return an instance of RestResponse
     */
    public static RestResponse usersGetinfo(String uids, String fields, String format) throws IOException {
        String v = "1.0";
        String method = "facebook.users.getinfo";
        FacebookSocialNetworkingServiceAuthenticator.login();
        String callId = String.valueOf(System.currentTimeMillis());
        String apiKey = FacebookSocialNetworkingServiceAuthenticator.getApiKey();
        String sessionKey = FacebookSocialNetworkingServiceAuthenticator.getSessionKey();
        String sig = FacebookSocialNetworkingServiceAuthenticator.sign(new String[][]{{"api_key", apiKey}, {"session_key", sessionKey}, {"call_id", callId}, {"v", v}, {"uids", uids}, {"fields", fields}, {"format", format}, {"method", method}});
        String[][] pathParams = new String[][]{};
        String[][] queryParams = new String[][]{{"api_key", "" + apiKey + ""}, {"session_key", sessionKey}, {"call_id", callId}, {"sig", sig}, {"v", v}, {"uids", uids}, {"fields", fields}, {"format", format}, {"method", method}};
        RestConnection conn = new RestConnection("http://api.facebook.com/restserver.php", pathParams, queryParams);
        sleep(1000);
        return conn.get(null);
    }

    /**
     *
     * @param format
     * @param status
     * @param clear
     * @param statusIncludesVerb
     * @return an instance of RestResponse
     */
    public static RestResponse usersSetStatus(String format, String status, String clear, String statusIncludesVerb) throws IOException {
        String v = "1.0";
        String method = "facebook.users.setStatus";
        FacebookSocialNetworkingServiceAuthenticator.login();
        String callId = String.valueOf(System.currentTimeMillis());
        String apiKey = FacebookSocialNetworkingServiceAuthenticator.getApiKey();
        String sessionKey = FacebookSocialNetworkingServiceAuthenticator.getSessionKey();
        String sig = FacebookSocialNetworkingServiceAuthenticator.sign(new String[][]{{"api_key", apiKey}, {"session_key", sessionKey}, {"call_id", callId}, {"v", v}, {"format", format}, {"status", status}, {"clear", clear}, {"status_includes_verb", statusIncludesVerb}, {"method", method}});
        String[][] pathParams = new String[][]{};
        String[][] queryParams = new String[][]{{"api_key", "" + apiKey + ""}, {"session_key", sessionKey}, {"call_id", callId}, {"sig", sig}, {"v", v}, {"format", format}, {"status", status}, {"clear", clear}, {"status_includes_verb", statusIncludesVerb}, {"method", method}};
        RestConnection conn = new RestConnection("http://api.facebook.com/restserver.php", pathParams, queryParams);
        sleep(1000);
        return conn.get(null);
    }
}
