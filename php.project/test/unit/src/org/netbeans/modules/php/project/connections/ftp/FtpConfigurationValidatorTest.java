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
package org.netbeans.modules.php.project.connections.ftp;

import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.util.NetworkSettings;
import org.openide.util.test.MockLookup;

public class FtpConfigurationValidatorTest extends NbTestCase {


    private static final String HOST = "localhost";
    private static final String PORT = "22";
    private static final String USER = "john";
    private static final String INITIAL_DIRECTORY = "/pub";
    private static final String TIMEOUT = "30";
    private static final String KEEP_ALIVE_INTERVAL = "10";


    public FtpConfigurationValidatorTest(String name) {
        super(name);
    }

    public void testValidate() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidHost() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(null, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("host", result.getErrors().get(0).getSource());
    }

    public void testInvalidPort() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, null, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port", result.getErrors().get(0).getSource());
    }

    public void testInvalidUser() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, false, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("user", result.getErrors().get(0).getSource());
    }

    public void testAnonymousLogin() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidInitialDirectory() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, null, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("initialDirectory", result.getErrors().get(0).getSource());
    }

    public void testInvalidTimeout() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, null, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("timeout", result.getErrors().get(0).getSource());
    }

    public void testInvalidKeepAliveInterval() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, null, true)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("keepAliveInterval", result.getErrors().get(0).getSource());
    }

    public void testNoProxyPassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoProxyActiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyPassiveMode() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    public void testProxyActiveMode() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(HOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getErrors().get(0).getSource());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    private void setupProxy() {
        MockLookup.init();
        MockLookup.setInstances(new ProxyCredentialsProvider());
    }

    //~ Inner classes

    private static final class ProxyCredentialsProvider extends NetworkSettings.ProxyCredentialsProvider {

        @Override
        protected String getProxyUserName(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected char[] getProxyPassword(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean isProxyAuthentication(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String getProxyHost(URI u) {
            return "http://some.proxy.com";
        }

        @Override
        protected String getProxyPort(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
