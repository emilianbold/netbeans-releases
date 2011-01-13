/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author Jirka Rechtacek
 */
public class ProxyAutoConfig {
    private static ProxyAutoConfig INSTANCE = null;
    private static ProxyAutoConfig DUMMY = new ProxyAutoConfig(null, null);

    public static synchronized ProxyAutoConfig get() {
        if (INSTANCE == null) {
            String init = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            LOGGER.fine("Init ProxyAutoConfig for " + init);
            Invocable inv;
            try {
                INSTANCE = DUMMY;
                inv = getEngine(init);
                INSTANCE = new ProxyAutoConfig(init.substring(4).trim(), inv); // NOI18N
            } catch (IllegalArgumentException x) {
                LOGGER.log(Level.INFO, x.getLocalizedMessage(), x);
                INSTANCE = DUMMY;
            }
        }
        return INSTANCE;
    }

    private String pacURL;
    private static final Logger LOGGER = Logger.getLogger(ProxyAutoConfig.class.getName());
    private final Invocable inv;

    private ProxyAutoConfig(String pac, Invocable inv) {
        this.inv = inv;
        this.pacURL = pac;
    }
    
    private static Invocable getEngine(String init) {
        assert init != null && init.startsWith(ProxySettings.PAC) : "Init string starts with PAC, but " + init;
        String pacURL = init.substring(4).trim();
        InputStream pacIS = downloadPAC(pacURL);
        assert pacIS != null : "No InputStream for " + pacURL;
        if (pacIS == null) {
            throw new IllegalArgumentException("No InputStream for " + pacURL);
        }
        ScriptEngine eng;
        try {
            eng = evalPAC(pacIS);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.FINE, "While constructing ProxyAutoConfig thrown " + ex, ex);
            throw new IllegalArgumentException(ex);
        } catch (ScriptException ex) {
            LOGGER.log(Level.FINE, "While constructing ProxyAutoConfig thrown " + ex, ex);
            throw new IllegalArgumentException(ex);
        }
        assert eng != null : "JavaScript engine cannot be null";
        if (eng == null) {
            throw new IllegalArgumentException("JavaScript engine cannot be null");
        }
        return (Invocable) eng;
    }
    
    public List<Proxy> findProxyForURL(URI u) {
        if (inv == null) {
            System.out.println("DUMMY NO PROXY");
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        Object proxies = null;
        try {
            proxies = inv.invokeFunction("FindProxyForURL", u.toURL().toExternalForm(), u.getHost()); // NOI18N
        } catch (ScriptException ex) {
            LOGGER.log(Level.FINE, "While invoking FindProxyForURL(" + u + ", " + u.getHost() + " thrown " + ex, ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.FINE, "While invoking FindProxyForURL(" + u + ", " + u.getHost() + " thrown " + ex, ex);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
        List<Proxy> res = analyzeResult(u, proxies);
        LOGGER.fine("findProxyForURL(" + u + ") returns " + (res == null ? "null!" : Arrays.asList(res)));
        return res;
    }
    
    private static InputStream downloadPAC(String pacURL) {
        InputStream is = null;
        try {
            URL url = null;
            try {
                url = new URL(pacURL);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, "Malformed " + pacURL, ex);
            }
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
        return is;
    }

    private static ScriptEngine evalPAC(InputStream is) throws FileNotFoundException, ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        Reader pacReader = new InputStreamReader(is);
        Reader utilsReader = new FileReader("pac_utils.js");
        engine.eval(pacReader);
        engine.eval(utilsReader);
        return engine;
    }

    private List<Proxy> analyzeResult(URI uri, Object proxiesString) {
        if (proxiesString == null) {
            LOGGER.info("Null result for " + uri);
            return null;
        }
        Proxy.Type proxyType;
        String protocol = uri.getScheme();
        assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
        if (protocol == null) {
            return null;
        } else {
            if ("http".equals(protocol)) { // NOI18N
                proxyType = Proxy.Type.HTTP;
            } else  {
                proxyType = Proxy.Type.SOCKS;
            }
        }
        StringTokenizer st = new StringTokenizer(proxiesString.toString(), ";"); //NOI18N
        List<Proxy> proxies = new LinkedList<Proxy>();
        while (st.hasMoreTokens()) {
            String proxy = st.nextToken();
            if (ProxySettings.DIRECT.equals(proxy)) {
                proxies.add(Proxy.NO_PROXY);
            } else {
                String host = getHost(proxy);
                Integer port = getPort(proxy);
                if (host != null && port != null) {
                    proxies.add(new Proxy(proxyType, new InetSocketAddress(host, port)));
                }
            }
        }
        return proxies;
    }

    private static String getHost(String proxy) {
        int i = proxy.lastIndexOf(":"); // NOI18N
        if (i <= 0 || i >= proxy.length() - 1) {
            LOGGER.info("No port in " + proxy);
            return null;
        }

        String host = proxy.substring(0, i);

        return ProxySettings.normalizeProxyHost(host);
    }

    private static Integer getPort(String proxy) {
        int i = proxy.lastIndexOf(":"); // NOI18N
        if (i <= 0 || i >= proxy.length() - 1) {
            LOGGER.info("No port in " + proxy);
            return null;
        }

        String port = proxy.substring(i + 1);
        if (port.indexOf('/') >= 0) {
            port = port.substring(0, port.indexOf('/'));
        }

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }
    }
}
