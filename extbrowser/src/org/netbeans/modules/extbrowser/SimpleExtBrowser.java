/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.beans.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/** Simple external browser that uses new process for each URL request.
 *  Typically it runs command like <CODE>netscape [url]</CODE>.
 *
 * @author  Radim Kubacki, Martin Grebac
 */
public class SimpleExtBrowser extends ExtWebBrowser {
        
    private static final long serialVersionUID = -8494345762328555637L;
    
    /** Determines whether the browser should be visible or not
     *  @return false when OS is Windows or Unix.
     *          true in all other cases.
     */
    public static Boolean isHidden () {
        return (Utilities.isWindows() || (Utilities.isUnix() && Utilities.getOperatingSystem() != Utilities.OS_MAC)) ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /** Creates new SimpleExtBrowser */
    public SimpleExtBrowser() {
        super();
        if (Utilities.getOperatingSystem () == Utilities.OS_OS2) {
            browserExecutable = new NbProcessDescriptor(
                "Netscape.exe", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        } else if (Utilities.getOperatingSystem () == Utilities.OS_MAC) {
            browserExecutable = new NbProcessDescriptor(
                "/usr/bin/open", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        } else {
            browserExecutable = new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + BrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(SimpleExtBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName() {
        if (name == null) {
            this.name = NbBundle.getMessage(SimpleExtBrowser.class, "CTL_SimpleExtBrowser");
        }
        return name;
    }
            
    /** Builds new implementation of specified browser.
     * If embeddable property is true, then it is expected
     * that returned implementation will implement org.openide.awt.HtmlBrowser.Impl
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new SimpleExtBrowserImpl();
    }
        
    /** Default format that can format tags related to execution. Currently this is only the URL.
     */
    public static class BrowserFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 5990981835151848381L;
        /** Tag replaced with the URL */
        public static final String TAG_URL = "URL";  // NOI18N
        
        
        /** @param info exec info about class to execute
         * @param classPath to substitute instead of CLASSPATH
         * @param bootClassPath boot class path
         * @param repository repository path
         * @param library library path
         */
        public BrowserFormat (String url) {
            super(new HashMap());
            java.util.Map map = getMap ();
            
            map.put (TAG_URL, url);
        }
        
    }
}
