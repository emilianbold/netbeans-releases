/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
