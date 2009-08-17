/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.api;

import java.awt.EventQueue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Creates an HTTP connection to Hudson.
 * Handles redirects and authentication.
 */
public final class ConnectionBuilder {

    private static final Logger LOG = Logger.getLogger(ConnectionBuilder.class.getName());
    private static final RequestProcessor TIMER = new RequestProcessor(ConnectionBuilder.class.getName() + ".TIMER"); // NOI18N

    /**
     * Session cookies set by home.
     * {@link java.net.CookieManager} in JDK 6 would be a bit easier.
     */
    private static final Map<URL,String[]> COOKIES = new HashMap<URL,String[]>();

    private URL home;
    private URL url;
    private final Map<String,String> requestHeaders = new LinkedHashMap<String,String>();
    private byte[] postData;
    private int timeout;
    private boolean auth = true;

    /**
     * Prepare a connection.
     * You must also specify a location, and if possible an associated instance or job.
     */
    public ConnectionBuilder() {}

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(String url) throws MalformedURLException {
        return url(new URL(url));
    }

    /**
     * Specify the home URL.
     * Useful for login authentication.
     * @param url the base URL of the Hudson instance
     * @return this builder
     */
    public ConnectionBuilder homeURL(URL url) {
        this.home = url;
        return this;
    }

    /**
     * Specify the Hudson instance as per {@link #homeURL}.
     * @param instance a Hudson instance
     * @return this builder
     */
    public ConnectionBuilder instance(HudsonInstance instance) {
        try {
            this.home = new URL(instance.getUrl());
        } catch (MalformedURLException x) {
            LOG.warning(x.toString());
        }
        return this;
    }

    /**
     * Specify the job, and hence the Hudson instance as per {@link #homeURL}.
     * @param job an arbitrary job in an instance
     * @return this builder
     */
    public ConnectionBuilder job(HudsonJob job) {
        HudsonInstance instance = job.getInstance();
        if (instance != null) {
            instance(instance);
        }
        return this;
    }

    /**
     * Define an HTTP request header.
     * @param key header key
     * @param value header value
     * @return this builder
     */
    public ConnectionBuilder header(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    /**
     * Post data to the connection.
     * @param data bytes to post
     * @return this builder
     */
    public ConnectionBuilder postData(byte[] data) {
        postData = data;
        return this;
    }

    /**
     * Sets a timeout on the response.
     * If the connection has not opened within that time,
     * {@link InterruptedIOException} will be thrown from {@link #connection}.
     * @param milliseconds time to wait
     * @return this builder
     */
    public ConnectionBuilder timeout(int milliseconds) {
        timeout = milliseconds;
        return this;
    }

    /**
     * Configures whether to prompt for authentication.
     * @param true to prompt for authentication (the default), false to immediately report 403s as errors
     * @return this builder
     */
    public ConnectionBuilder authentication(boolean a) {
        auth = a;
        return this;
    }

    /**
     * Actually try to open the connection.
     * May need to retry to handle redirects and/or authentication.
     * @return an open and valid connection, ready for {@link URLConnection#getInputStream},
     *         {@link URLConnection#getHeaderField(String)}, etc.
     * @throws IOException for various reasons, including non-200 response code
     */
    public URLConnection connection() throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("You must call the url method!"); // NOI18N
        }
        if (url.getProtocol().matches("https?") && EventQueue.isDispatchThread()) {
            LOG.log(Level.FINER, "opening " + url, new IllegalStateException("Avoid connecting from EQ"));
            if (timeout == 0) {
                timeout = 3000;
            }
        }
        if (timeout == 0) {
            return doConnection();
        } else {
            final Thread curr = Thread.currentThread();
            RequestProcessor.Task task = TIMER.post(new Runnable() {
                public void run() {
                    curr.interrupt();
                }
            }, timeout);
            try {
                return doConnection();
            } finally {
                task.cancel();
            }
        }
    }

    private URLConnection doConnection() throws IOException {
        URLConnection conn = url.openConnection();
        RETRY: while (true) {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
            }
            if (conn instanceof HttpsURLConnection) {
                // #161324: permit self-signed SSL certificates.
                try {
                    SSLContext sc = SSLContext./* XXX JDK 6: getDefault() */getInstance("SSL"); // NOI18N
                    sc.init(null, new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                    }, new SecureRandom());
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                    ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                } catch (Exception x) {
                    LOG.log(Level.FINE, "could not disable SSL verification", x);
                }
            }
            URL curr = conn.getURL();
            LOG.log(Level.FINER, "Trying to open {0}", curr);
            if (home != null) {
                for (ConnectionAuthenticator authenticator : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                    authenticator.prepareRequest(conn, home);
                }
                if (COOKIES.containsKey(home)) {
                    for (String cookie : COOKIES.get(home)) {
                        String cookieBare = cookie.replaceFirst(";.*", ""); // NOI18N
                        LOG.log(Level.FINER, "Setting cookie {0} for {1}", new Object[] {cookieBare, conn.getURL()});
                        conn.setRequestProperty("Cookie", cookieBare); // NOI18N
                    }
                }
            }
            if (postData != null) {
                conn.setDoOutput(true);
            }
            for (Map.Entry<String,String> header : requestHeaders.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
            try {
                conn.connect();
            } catch (IOException x) {
                throw x;
            } catch (Exception x) {
                // JRE #6797318, etc.; various bugs in JRE networking code; see e.g. #163555
                throw (IOException) new IOException("Connecting to " + curr + ": " + x.toString()).initCause(x);
            }
            if (postData != null) {
                OutputStream os = conn.getOutputStream();
                try {
                    os.write(postData);
                } finally {
                    os.close();
                }
            }
            if (!(conn instanceof HttpURLConnection)) {
                break;
            }
            if (home != null) {
                List<String> cookies = conn.getHeaderFields().get("Set-Cookie"); // NOI18N
                if (cookies != null) {
                    LOG.log(Level.FINE, "Cookies set for domain {0}: {1}", new Object[] {home, cookies});
                    COOKIES.put(home, cookies.toArray(new String[cookies.size()]));
                }
            }
            int responseCode = ((HttpURLConnection) conn).getResponseCode();
            LOG.log(Level.FINER, "  => {0}", responseCode);
            switch (responseCode) {
            // Workaround for JDK bug #6810084; HttpURLConnection.setInstanceFollowRedirects does not work.
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                URL redirect = new URL(conn.getHeaderField("Location")); // NOI18N
                conn = redirect.openConnection();
                continue RETRY;
            case HttpURLConnection.HTTP_FORBIDDEN:
                if (auth && home != null) {
                    for (ConnectionAuthenticator authenticator : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                        URLConnection retry = authenticator.forbidden(conn, home);
                        if (retry != null) {
                            LOG.log(Level.FINER, "Retrying after auth from {0}", authenticator);
                            conn = retry;
                            continue RETRY;
                        }
                    }
                }
                IOException x = new IOException("403 on " + url); // NOI18N
                Exceptions.attachLocalizedMessage(x, NbBundle.getMessage(ConnectionBuilder.class, "ConnectionBuilder.log_in", url));
                throw x;
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new FileNotFoundException(curr.toString());
            case HttpURLConnection.HTTP_OK:
                break RETRY;
            default:
                // XXX are there other legitimate response codes?
                throw new IOException("Server rejected connection to " + curr + " with code " + responseCode); // NOI18N
            }
        }
        return conn;
    }

    /**
     * Like {@link #connection} but coerced to an HTTP connection.
     * @throws IOException for the usual reasons, or if a non-HTTP connection resulted
     */
    public HttpURLConnection httpConnection() throws IOException {
        URLConnection c = connection();
        if (c instanceof HttpURLConnection) {
            return (HttpURLConnection) c;
        } else {
            throw new IOException("Not an HTTP connection: " + c); // NOI18N
        }
    }

}
