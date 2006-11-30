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

package org.netbeans.upgrade.systemoptions;

import junit.framework.*;

/**
 * @author Radek Matous
 */
public class IDESettingsTest extends BasicTestForImport {
    public IDESettingsTest(String testName) {
        super(testName, "org-netbeans-core-IDESettings.settings");
    }
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "IgnoredFiles",
            "UIMode",
            "WWWBrowser",
            "confirmDelete",
            "homePage",
            "modulesSortMode",
            "proxyNonProxyHosts",
            "proxyType",
            "showFileExtensions",
            "showToolTipsInIDE",
            "useProxy",
            "proxyHttpHost",
            "proxyHttpPort"
        });
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/core");
    }
    
    public void testIgnoredFiles() throws Exception {
        //java.lang.String
        assertProperty("IgnoredFiles","^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store))$|^\\.[#_]|~$");
    }
    
    public void testUIMode() throws Exception {
        //java.lang.Integer
        assertProperty("UIMode","2");
    }
    
    public void testWWWBrowser() throws Exception {
        //java.lang.String
        assertProperty("WWWBrowser","SL[/Browsers/FirefoxBrowser");
    }
    
    public void testConfirmDelete() throws Exception {
        //java.lang.Boolean
        assertProperty("confirmDelete","true");
    }
    public void testHomePage() throws Exception {
        //java.lang.String
        assertProperty("homePage","http://www.netbeans.org/");
    }
    
    public void testModulesSortMode() throws Exception {
        //java.lang.Integer
        assertProperty("modulesSortMode","5");
    }
    
    public void testProxyType() throws Exception{
        //java.lang.Integer
        assertProperty("proxyType","1");
    }
    public void testShowFileExtensions() throws Exception{
        //java.lang.Boolean
        assertProperty("showFileExtensions","false");
    }
    
    public void testShowToolTipsInIDE() throws Exception{
        //java.lang.Boolean
        assertProperty("showToolTipsInIDE","false");
    }
    
    public void testUseProxy() throws Exception{
        //java.lang.Boolean
        assertProperty("useProxy","false");
    }
    
    public void testProxyHttpHost() throws Exception{
        //java.lang.String
        assertProperty("proxyHttpHost","");
    }
    
    public void testProxyHttpPort() throws Exception{
        //java.lang.String
        assertProperty("proxyHttpPort","");
    }

    public void testProxyNonProxyHosts() throws Exception{
        //java.lang.String
        assertProperty("proxyNonProxyHosts","localhost|127.0.0.1");
    }        
}
