/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.tasks.nb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import junit.framework.Assert;
import org.openide.util.URLStreamHandlerRegistration;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@URLStreamHandlerRegistration(protocol="test")
public final class TSHandler extends URLStreamHandler {
    private static String assertURL;
    private static Appendable assertRequest;
    private static String assertResponse;
    
    static synchronized void expectQuery(
        String url, Appendable request, String response
    ) {
        Assert.assertNotNull("Expected url can't be null", url);
        Assert.assertNull("No URL specified yet: " + assertURL, assertURL);
        assertURL = url;
        assertRequest = request;
        assertResponse = response;
    }
    
    private static synchronized Conn newConnection(URL u) throws IOException {
        Assert.assertNotNull("Expected URL was specified", assertURL);
        Assert.assertEquals("The URL is the same", assertURL, u.toExternalForm());
        Conn c = new Conn(u, assertRequest, assertResponse);
        assertURL = null;
        assertResponse = null;
        assertRequest = null;
        return c;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return newConnection(u);
    }
    
    
    private static final class Conn extends HttpURLConnection {
        private final ByteArrayInputStream is;
        private final OutputStream os;
        private final Map<String, List<String>> headers;
        
        private Conn(URL u, Appendable assertRequest, String assertResponse) 
        throws IOException {
            super(u);
            is = new ByteArrayInputStream(assertResponse.getBytes("UTF-8"));
            os = new AppOS(assertRequest);
            HashMap<String, List<String>> h = new HashMap<String, List<String>>();
            h.put("Content-Type", Collections.singletonList(MediaType.APPLICATION_JSON));
            headers = Collections.unmodifiableMap(h);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return os;
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return headers;
        }
        
        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
        }
    } // end of Conn

    private static class AppOS extends OutputStream {
        private final Appendable a;

        public AppOS(Appendable a) {
            this.a = a;
        }

        @Override
        public void write(int b) throws IOException {
            a.append((char)b);
        }
    } // end of AppOS
}

