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

package org.netbeans.modules.web.freeform;

import org.netbeans.modules.web.api.webmodule.*;
import org.openide.filesystems.FileObject;

/**
 * Check that the web module is found for various files and has correct properties.
 * @author Pavel Buzek
 */
public class ModulesWebTest extends TestBaseWeb {

    public ModulesWebTest (String name) {
        super(name);
    }

    public void testGetWebModule() throws Exception {
        WebModule forJsp = WebModule.getWebModule (helloWorldJsp);
        assertNotNull ("find web module for:" + helloWorldJsp.getPath (), forJsp);
        FileObject dd = jakarta.getProjectDirectory ().getFileObject ("web/WEB-INF/web.xml");
        WebModule forDD = WebModule.getWebModule (dd);
        assertNotNull ("find web module for:" + dd.getPath (), forDD);
        WebModule forServlet =  WebModule.getWebModule (helloWorldServlet);
        assertNotNull ("find web module for:" + helloWorldServlet.getPath (), forServlet);
        
        assertEquals ("same web modules for servlet ("+ forServlet.getDocumentBase ()+") and jsp ("+forJsp.getDocumentBase ()+")", forServlet.getDocumentBase (), forJsp.getDocumentBase ());
        assertEquals ("same web modules for servlet ("+ forServlet.getDocumentBase ()+") and we.xml ("+forDD.getDocumentBase ()+")", forServlet.getDocumentBase (), forDD.getDocumentBase ());
        assertEquals ("same web modules for jsp ("+ forJsp.getDocumentBase ()+") and web.xml ("+forDD.getDocumentBase ()+")", forJsp.getDocumentBase (), forDD.getDocumentBase ());
        WebModule forBuildXml = WebModule.getWebModule (jakarta.getProjectDirectory ().getFileObject ("build.xml"));
        assertNull ("WebModule found for build.xml which does not belong to web module", forBuildXml);
    }
    
    public void testWebModuleProperties () throws Exception {
        WebModule wm = WebModule.getWebModule (jakarta.getProjectDirectory ().getFileObject ("web"));
        assertNotNull ("find web module for doc root", wm);
	assertEquals ("correct j2ee version", WebModule.J2EE_14_LEVEL, wm.getJ2eePlatformVersion ());
        assertEquals ("correct context path", "/myapp", wm.getContextPath ());
        assertEquals ("correct context path", jakarta.getProjectDirectory ().getFileObject ("web"), wm.getDocumentBase ());
    }
    
}
