/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import org.netbeans.junit.NbTestCase;

/** Test for ServletExecutionTest.
 * It tries to connect to servlet provided by additional module.
 *
 * @author Radim Kubacki
 */
public class ServletExecutionTest extends NbTestCase {

    public ServletExecutionTest(String testName) {
        super (testName);
    }

    public void testServletExecution() throws Exception {
        HttpServerSettings settings =
         (HttpServerSettings)HttpServerSettings.findObject (HttpServerSettings.class, true);
        log("Starting HTTP server");
        settings.setRunning(true);
        
        assertTrue("HTTP server has to run", settings.isRunning());
        URL url = new URL("http", "localhost", settings.getPort(), "/servlet/org.netbeans.modules.servlettest.ModuleServlet");
        log("Connecting to "+url.toExternalForm());
        InputStream is = url.openStream();
        log(new DataInputStream(is).readLine());
    }
    
}
