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
package org.netbeans.modules.ods.tasks.tasktop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import junit.framework.Assert;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class TSProtocol extends Protocol {
    private static String assertURL;
    private static Appendable assertRequest;
    private static String assertResponse;

    TSProtocol(ProtocolSocketFactory factory) {
        super("test", factory, 80);
    }
    
    static synchronized void expectQuery(
        String url, Appendable request, String response
    ) {
        Assert.assertNotNull("Expected url can't be null", url);
        Assert.assertNull("No URL specified yet: " + assertURL, assertURL);
        assertURL = url;
        assertRequest = request;
        assertResponse = response;
    }
    
    static synchronized Socket createSocket(
        String host, int port, InetAddress localAddress, 
        int localPort, HttpConnectionParams params
    ) throws IOException {
        Assert.assertNotNull("Expected URL was specified", assertURL);
        Conn c = new Conn(assertURL, assertRequest, assertResponse);
        assertURL = null;
        assertResponse = null;
        assertRequest = null;
        return c;
    }

    private static final class Conn extends Socket {
        private static final String HTTP_HEADER =
              "HTTP/1.1 200 OK\r\n"
            + "Server: Jarda's server/1.1\r\n"
            + "Pragma: no-cache\r\n"
            + "Cache-Control: no-cache, no-store, max-age=0\r\n"
            + "Content-Type: application/json;charset=UTF-8\r\n";

        private final ByteArrayInputStream is;
        private final OutputStream os;
        
        private Conn(String expectedURL, Appendable assertRequest, String assertResponse) 
        throws IOException {
            String reply = HTTP_HEADER + 
                "Content-Length: " + assertResponse.length() + "\r\n\r\n"
                + assertResponse;
            final byte[] bytes = reply.getBytes("UTF-8");
            Assert.assertEquals("Right now we support just ascii chars", bytes.length, reply.length());
            is = new ByteArrayInputStream(bytes);
            os = new AppOS(expectedURL, assertRequest);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return os;
        }
    } // end of Conn

    private static class AppOS extends OutputStream {
        private final String expectedURL;
        private final StringBuilder header;
        private final Appendable a;

        public AppOS(String expectedURL, Appendable a) {
            header = new StringBuilder();
            this.a = a;
            this.expectedURL = expectedURL;
        }

        @Override
        public void write(int b) throws IOException {
            if (isHeaderEnd(header)) {
                a.append((char)b);
            } else {
                header.append((char)b);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            
            int endOfFirstLine = header.indexOf("\n");
            if (endOfFirstLine == -1) {
                Assert.fail("No first line in " + header);
            }
            String[] three = header.substring(0, endOfFirstLine).split(" ");
            Assert.assertEquals("Three segments in " + header, 3, three.length);
            
            if (!expectedURL.endsWith(three[1])) {
                Assert.fail("URL suffix is not OK expected: " + expectedURL + " was: " + three[1]);
            }
        }
        
        private static boolean isHeaderEnd(CharSequence txt) {
            final int len = txt.length();
            int r = 0;
            int n = 0;
            for (int i = len - 4; i < len; i++) {
                if (i < 0) {
                    return false;
                }
                switch (txt.charAt(i)) {
                    case '\r': r++; break;
                    case '\n': n++; break;
                    default: return false;
                }
            }
            return r == 2 && n == 2;
        }
        
        
    } // end of AppOS
}

