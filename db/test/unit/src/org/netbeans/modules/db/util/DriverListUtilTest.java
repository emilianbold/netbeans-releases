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

package org.netbeans.modules.db.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class DriverListUtilTest extends TestCase {
    private static final String HOST = "myhost";
    private static final String PORT = "8888";
    private static final String DB = "mydb";
    private static final String SERVERNAME = "servername";
    private static final String ADDITIONAL = "foo;bar;baz";
    
    private static final HashMap<String, String> ALLPROPS = 
            new HashMap<String, String>();
    
    private static final ArrayList<String> STD_SUPPORTED_TOKENS =
            new ArrayList<String>();
    
    static {
        ALLPROPS.put(JdbcUrl.TOKEN_HOST, HOST);
        ALLPROPS.put(JdbcUrl.TOKEN_DB, DB);
        ALLPROPS.put(JdbcUrl.TOKEN_PORT, PORT);
        ALLPROPS.put(JdbcUrl.TOKEN_SERVERNAME, SERVERNAME);
        ALLPROPS.put(JdbcUrl.TOKEN_ADDITIONAL, ADDITIONAL);  
        
        STD_SUPPORTED_TOKENS.add(JdbcUrl.TOKEN_HOST);
        STD_SUPPORTED_TOKENS.add(JdbcUrl.TOKEN_PORT);
        STD_SUPPORTED_TOKENS.add(JdbcUrl.TOKEN_DB);
        STD_SUPPORTED_TOKENS.add(JdbcUrl.TOKEN_ADDITIONAL);
    }
    
    public DriverListUtilTest(String testName) {
        super(testName);
    }
    
    public void testJdbcUrls() throws Exception {
        List<JdbcUrl> urls = DriverListUtil.getJdbcUrls();
        for ( JdbcUrl url : urls ) {
            if (! url.urlIsParsed()) {
                testNonParsedUrl(url);
            } else if (url.getName().equals(getDriverName("DRIVERNAME_MySQL"))) {
                testMySQL(url);
            } else if (url.getName().equals(getDriverName("DRIVERNAME_JavaDbEmbedded"))) {
                testJavaDbEmbedded(url);
            } else if (url.getName().equals(getDriverName("DRIVERNAME_JavaDbNetwork"))) {
                testJavaDbNetwork(url);
            } else if (url.getName().equals(getDriverName("DRIVERNAME_PostgreSQL"))) {
                testPostgreSQL(url);
            }
        }
    }

    private void testJavaDbEmbedded(JdbcUrl url) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        ArrayList<String> supportedProps = new ArrayList<String>();
        supportedProps.add(JdbcUrl.TOKEN_DB);
        supportedProps.add(JdbcUrl.TOKEN_ADDITIONAL);
        
        checkUrl(url, getDriverName("DRIVERNAME_JavaDbEmbedded"), null, "org.apache.derby.jdbc.EmbeddedDriver", 
                "jdbc:derby:<DB>[;<ADDITIONAL>]", supportedProps, requiredProps);
        
        HashMap<String, String> props = new HashMap<String, String>();
        props.putAll(ALLPROPS);
        props.remove(JdbcUrl.TOKEN_SERVERNAME);
        props.remove(JdbcUrl.TOKEN_HOST);
        props.remove(JdbcUrl.TOKEN_PORT);
        
        testUrlString(url, props, "jdbc:derby:" + DB + ";" + ADDITIONAL);

        props.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, props, "jdbc:derby:" + DB);
        
        props.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, props);
        
        testBadUrlString(url, "jdbc:derby:");
        testBadUrlString(url, "jdbc:daryb://db");
        testBadUrlString(url, "jdbc:derby/:db;create=true");
    }

    private void testJavaDbNetwork(JdbcUrl url) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_HOST);
        requiredProps.add(JdbcUrl.TOKEN_DB);
        checkUrl(url, getDriverName("DRIVERNAME_JavaDbNetwork"), null, "org.apache.derby.jdbc.ClientDriver", 
                "jdbc:derby://<HOST>[:<PORT>]/<DB>[;<ADDITIONAL>]", STD_SUPPORTED_TOKENS, requiredProps);
        
        HashMap<String, String> props = new HashMap<String, String>();
        props.putAll(ALLPROPS);
        props.remove(JdbcUrl.TOKEN_SERVERNAME);
        
        testUrlString(url, props, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB + ";" + ADDITIONAL);

        props.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, props, "jdbc:derby://" + HOST + ":" + PORT + "/" + DB);
        
        props.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, props, "jdbc:derby://" + HOST + "/" + DB);  
        
        props.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, props);
        
        props.remove(JdbcUrl.TOKEN_HOST);
        props.put(JdbcUrl.TOKEN_DB, DB);
        testMissingParameter(url, props);
        
        testBadUrlString(url, "jdbc:derby:///db");
        testBadUrlString(url, "jdbc:derby://localhost");
        testBadUrlString(url, "jdbc:derby://localhost/;create=true");
        testBadUrlString(url, "jdbc:derby:/localhost:8889/db;create=true");
    }

    private void testMySQL(JdbcUrl url) throws Exception {
        checkUrl(url, getDriverName("DRIVERNAME_MySQL"), null, "com.mysql.jdbc.Driver", 
                "jdbc:mysql://[<HOST>[:<PORT>]]/[<DB>][?<ADDITIONAL>]",
                STD_SUPPORTED_TOKENS, new ArrayList<String>());
        
        HashMap<String, String> props = new HashMap<String, String>();
        props.putAll(ALLPROPS);
        props.remove(JdbcUrl.TOKEN_SERVERNAME);
        
        testUrlString(url, props, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        props.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, props, "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB);
        
        props.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, props, "jdbc:mysql://" + HOST + "/" + DB);
        
        props.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, props, "jdbc:mysql:///" + DB); 
        
        props.remove(JdbcUrl.TOKEN_DB);
        testUrlString(url, props, "jdbc:mysql:///");
        
        props.put(JdbcUrl.TOKEN_HOST, HOST);
        testUrlString(url, props, "jdbc:mysql://" + HOST + "/");
        
        props.put(JdbcUrl.TOKEN_PORT, PORT);
        testUrlString(url, props, "jdbc:mysql://" + HOST + ":" + PORT + "/");
        
        props.put(JdbcUrl.TOKEN_ADDITIONAL, ADDITIONAL);
        testUrlString(url, props, "jdbc:mysql://" + HOST + ":" + PORT + "/?" + ADDITIONAL);
    }
    
    private static String getDriverName(String key) {
        return NbBundle.getMessage(DriverListUtil.class, key);
    }
        
    private void testNonParsedUrl(JdbcUrl url) throws Exception {
        String urlString = "foo:bar:my.url";
        url.setUrl(urlString);
        assertEquals(url.getUrl(), urlString);
    }
    
    private void checkUrl(JdbcUrl url, String name, String type, String className,
            String template, List<String> supportedTokens, List<String> requiredTokens) {
        assertEquals(name, url.getName());
        assertEquals(type, url.getType());
        
        if (type == null) {
            assertEquals(name, url.getDisplayName());
        } else {
            assertEquals(name + "(" + type + ")", url.getDisplayName());
        }
        
        assertEquals(className, url.getClassName());
        assertEquals(template, url.getUrlTemplate());
        
        JdbcUrl other = new JdbcUrl(url.getName(), url.getClassName(),
                url.getType(), url.getUrlTemplate(), url.urlIsParsed());
        
        assertEquals(url, other);

        checkSupportedTokens(url, supportedTokens);
        checkRequiredTokens(url, requiredTokens);
    }

    private void testPostgreSQL(JdbcUrl url) throws Exception {
        ArrayList<String> requiredProps = new ArrayList<String>();
        requiredProps.add(JdbcUrl.TOKEN_DB);
        
        checkUrl(url, getDriverName("DRIVERNAME_PostgreSQL"), null, "org.postgresql.Driver", 
                "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]",
                STD_SUPPORTED_TOKENS, requiredProps);
        
        HashMap<String, String> props = new HashMap<String, String>();
        props.putAll(ALLPROPS);
        props.remove(JdbcUrl.TOKEN_SERVERNAME);
        
        testUrlString(url, props, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB + "?" + ADDITIONAL);

        props.remove(JdbcUrl.TOKEN_ADDITIONAL);
        testUrlString(url, props, "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB);
        
        props.remove(JdbcUrl.TOKEN_PORT);
        testUrlString(url, props, "jdbc:postgresql://" + HOST + "/" + DB);
        
        props.remove(JdbcUrl.TOKEN_HOST);
        testUrlString(url, props, "jdbc:postgresql:" + DB);
        
        props.remove(JdbcUrl.TOKEN_DB);
        testMissingParameter(url, props);
        
        testBadUrlString(url, "jdbc:postgresql:");
        testBadUrlString(url, "jdbc:postgresql:///" + DB);
    }
    
    private void testUrlString(JdbcUrl url, Map<String, String> props, String urlString) throws Exception {
        url.clear();
        url.putAll(props);
        assertEquals(urlString, url.getUrl());
        
        url.clear();
        
        url.setUrl(urlString);
        for (String prop : props.keySet()) {
            assertEquals(props.get(prop), url.get(prop));
        }
    }
    
    private void testMissingParameter(JdbcUrl url, HashMap<String, String> props) {
        url.clear();
        url.putAll(props);        
        
        assertEquals("", url.getUrl());
    }

    private void testBadUrlString(JdbcUrl url, String urlString) {
        boolean shouldHaveFailed = false;
        try {
          url.setUrl(urlString);
          shouldHaveFailed = true;
        } catch (Throwable t) {
            if (! (t instanceof MalformedURLException)) {
                fail("Should have thrown a MalformedURLException");
            }
        }
        
        if (shouldHaveFailed) {
            fail("Should have thrown an exception");
        }
    }


    private void checkSupportedTokens(JdbcUrl url, List<String> expected) {       
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.supportsToken(token));
                assertFalse(url.requiresToken(token));
            }
        }
    }

    private void checkRequiredTokens(JdbcUrl url, List<String> expected) { 
        for (String token : ALLPROPS.keySet()) {
            if (expected.contains(token)) {
                assertTrue(url.requiresToken(token));
                assertTrue(url.supportsToken(token));
            } else {
                assertFalse(url.requiresToken(token));
            }
        }
    }
    
}
