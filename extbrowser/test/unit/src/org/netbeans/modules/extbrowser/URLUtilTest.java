/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * ExtWebBrowserTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */                

package org.netbeans.modules.extbrowser;
 
import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;
import java.beans.*;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;
         
/**
 *
 * @author rk109395
 */
public class URLUtilTest extends NbTestCase {

    public URLUtilTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testCreateExternalURL() throws Exception {
        // find fileobject for
        // jar:file:/${NB}/ide5/modules/docs/org-netbeans-modules-usersguide.jar!/org/netbeans/modules/usersguide/pending.html
        File f = InstalledFileLocator.getDefault().locate("modules/docs/org-netbeans-modules-usersguide.jar", null, false);
        assertNotNull("Usersguide module not found", f);
        FileObject fo = FileUtil.toFileObject(f);
        log("jar fileobject is  " + fo);
        assertNotNull("FileObject corresponding to usersguide module not found", fo);
        FileObject jar = FileUtil.getArchiveRoot(fo);
        assertNotNull("FileObject corresponding to usersguide as jar not found", jar);
        FileObject pendingPage = jar.getFileObject("org/netbeans/modules/usersguide/pending.html");
        URL pendingURL = pendingPage.getURL();
        log("original url is " + pendingURL);
        URL newURL1 = URLUtil.createExternalURL(pendingURL, true);
        log("jar url " + newURL1);
        URL newURL2 = URLUtil.createExternalURL(pendingURL, false);
        log("http url " + newURL2);
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (URLUtilTest.class);
        return suite;
    }
    
    
}
