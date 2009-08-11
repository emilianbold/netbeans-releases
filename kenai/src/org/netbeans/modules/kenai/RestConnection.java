/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.kenai.api.Kenai;

/**
 * RestConnection
 *
 * @author Maros Sandor
 */
public class RestConnection {

    static {
        //set the identification of the client
        System.setProperty("http.agent", System.getProperty("user.name") + " (from NetBeans IDE)");
    }
    public static final int TIMEOUT = 30 * 1000;
    private HttpsURLConnection conn;
    private String date;

    /** Creates a new instance of RestConnection */
    public RestConnection(String baseUrl) {
        this(baseUrl, null, null);
    }

    /** Creates a new instance of RestConnection */
    public RestConnection(String baseUrl, String[][] params) {
        this(baseUrl, null, params);
    }

    /** Creates a new instance of RestConnection */
    public RestConnection(String baseUrl, String[][] pathParams, String[][] params) {
        //T9Y
        String testUrl = System.getProperty("netbeans.t9y.kenai.testUrl");
        if (testUrl != null && testUrl.length() > 0) {
        } else {
            try {
                String urlStr = baseUrl;
                if (pathParams != null && pathParams.length > 0) {
                    urlStr = replaceTemplateParameters(baseUrl, pathParams);
                }
                URL url = new URL(encodeUrl(urlStr, params));
                conn = (HttpsURLConnection) url.openConnection();

                TrustManager[] tm = new TrustManager[]{
                    new X509TrustManager() {

                        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            return;
                        }

                        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            return;
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
                };

                SSLContext context = null;
                context = SSLContext.getInstance("SSL");
                context.init(null, tm, null);
                conn.setSSLSocketFactory(context.getSocketFactory());

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                conn.setAllowUserInteraction(true);
                conn.setConnectTimeout(TIMEOUT);
                //TODO: KenaiAuthenticator not working. Why?
                //this is just workaround this should be implemented properly
                PasswordAuthentication a = Kenai.getDefault().getPasswordAuthentication();
                if (a!= null && a.getUserName()!=null && (params==null || !params[0][0].equals("username"))) {
                    assert a.getPassword()!=null;
                    String userPassword = a.getUserName() + ":" + String.valueOf(a.getPassword());
                    String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                    conn.setRequestProperty("Authorization", "Basic " + encoding);
                }

                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                date = format.format(new Date());
                conn.setRequestProperty("Date", date);
            } catch (Exception ex) {
                Logger.getLogger(RestConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getDate() {
        return date;
    }

    public RestResponse get() throws IOException {
        return get(null);
    }

    public RestResponse get(String[][] headers) throws IOException {
        String testUrl = System.getProperty("netbeans.t9y.kenai.testUrl");
        if (testUrl != null && testUrl.length() > 0) {
            return new RestResponse();
        } else {
            conn.setRequestMethod("GET");
            return connect(headers, null);
        }
    }

    public RestResponse head() throws IOException {
        return get(null);
    }

    public RestResponse head(String[][] headers) throws IOException {
        conn.setRequestMethod("HEAD");
        return connect(headers, null);
    }

    public RestResponse put(String[][] headers) throws IOException {
        return put(headers, (InputStream) null);
    }

    public RestResponse put(String[][] headers, String data) throws IOException {
        InputStream is = null;
        if (data != null) {
            is = new ByteArrayInputStream(data.getBytes("UTF-8"));
        }
        return put(headers, is);
    }

    public RestResponse put(String[][] headers, InputStream is) throws IOException {
        conn.setRequestMethod("PUT");
        return connect(headers, is);
    }

    public RestResponse post(String[][] headers) throws IOException {
        return post(headers, (InputStream) null);
    }

    public RestResponse post(String[][] headers, String data) throws IOException {
        InputStream is = null;
        if (data != null) {
            is = new ByteArrayInputStream(data.getBytes("UTF-8"));
        }
        return post(headers, is);
    }

    public RestResponse post(String[][] headers, InputStream is) throws IOException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        return connect(headers, is);
    }

    /**
     * Used by post method whose contents are like form input
     */
    public RestResponse post(String[][] headers, String[][] params) throws IOException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        String data = encodeParams(params);
        return connect(headers, new ByteArrayInputStream(data.getBytes("UTF-8")));
    }

    public RestResponse delete(String[][] headers) throws IOException {
        conn.setRequestMethod("DELETE");
        return connect(headers, null);
    }

    /**
     * @param baseUrl
     * @param params
     * @return response
     */
    private RestResponse connect(String[][] headers, InputStream data) throws IOException {
        RestResponse response = new RestResponse();
        try {
            // Send data
            setHeaders(headers);

            String method = conn.getRequestMethod();

            byte[] buffer = new byte[1024];
            int count = 0;

            if (method.equals("PUT") || method.equals("POST")) {
                if (data != null) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();

                    while ((count = data.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    os.flush();
                }
            }

            response.setResponseCode(conn.getResponseCode());
            response.setResponseMessage(conn.getResponseMessage());
            response.setContentType(conn.getContentType());
            response.setContentEncoding(conn.getContentEncoding());
            response.setLastModified(conn.getLastModified());

            try {
            InputStream is = conn.getInputStream();
                while ((count = is.read(buffer)) != -1) {
                    response.write(buffer, 0, count);
                }
            } catch (IOException e) {
                while ((count = conn.getErrorStream().read(buffer)) != -1) {
                    response.write(buffer, 0, count);
                }
            }
            return response;
        } catch (Exception e) {
            String errMsg = "Cannot connect to : " + conn.getURL().getHost();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                StringBuffer buf = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    buf.append(line);
                    buf.append('\n');
                }
                errMsg = buf.toString();
            } finally {
                throw new IOException(errMsg, e);
            }
        }
    }

    private String replaceTemplateParameters(String baseUrl, String[][] pathParams) {
        String url = baseUrl;
        if (pathParams != null) {
            for (int i = 0; i < pathParams.length; i++) {
                String key = pathParams[i][0];
                String value = pathParams[i][1];
                if (value == null) {
                    value = "";
                }
                url = url.replace(key, value);
            }
        }
        return url;
    }

    private String encodeUrl(String baseUrl, String[][] params) {
        String encodedParams = encodeParams(params);
        if (encodedParams.length() > 0) {
            encodedParams = "?" + encodedParams;
        }
        return baseUrl + encodedParams;
    }

    private String encodeParams(String[][] params) {
        String p = "";

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String key = params[i][0];
                String value = params[i][1];

                if (value != null) {
                    try {
                        p += key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(RestConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (p.length() > 0) {
                p = p.substring(0, p.length() - 1);
            }
        }

        return p;
    }

    private void setHeaders(String[][] headers) {
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                conn.setRequestProperty(headers[i][0], headers[i][1]);
            }
        }
    }
}
