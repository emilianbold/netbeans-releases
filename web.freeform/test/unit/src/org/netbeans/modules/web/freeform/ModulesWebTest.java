/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
