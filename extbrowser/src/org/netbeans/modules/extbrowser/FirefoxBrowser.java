/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Martin Grebac
 */
public class FirefoxBrowser extends ExtWebBrowser {
            
//    /** storage for starting browser timeout property */
//    protected int browserStartTimeout = 6000;

    private static final long serialVersionUID = -3982770681461437966L;
    
    /** Creates new ExtWebBrowser */
    public FirefoxBrowser() {
        ddeServer = ExtWebBrowser.FIREFOX;
        //browserStartTimeout = 6000;
    }

    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        String detectedPath = null;
        if (Utilities.isWindows()) {
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath(ExtWebBrowser.FIREFOX);      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log("Cannot detect Firefox : " + e);      // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return (Utilities.isUnix() && Utilities.getOperatingSystem() != Utilities.OS_MAC) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(FirefoxBrowser.class, "CTL_FirefoxBrowserName");
        }
        return name;
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (Utilities.isWindows()) {
            impl = new NbDdeBrowserImpl(this);
        } else if (Utilities.isUnix() && Utilities.getOperatingSystem() != Utilities.OS_MAC) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage(FirefoxBrowser.class, "MSG_CannotUseBrowser"));
        }
        
        return impl;
    }
    
    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {

        String prg;
        String params = "";                                                     // NOI18N
        NbProcessDescriptor retValue;
        
        //Windows
        if (Utilities.isWindows()) {
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            try {
                prg = NbDdeBrowserImpl.getBrowserPath(getDDEServer());
                return new NbProcessDescriptor (prg, params);
            } catch (NbBrowserException e) {
                    prg = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";     // NOI18N
            } catch (UnsatisfiedLinkError e) {
                prg = "firefox.exe";                                     // NOI18N
            }

            retValue = new NbProcessDescriptor (prg, params);
            return retValue;            
        
        //Unix
        } else { 
            
            prg = "firefox";                                                      // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                java.io.File f = new java.io.File ("/usr/bin/firefox"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
                f = new java.io.File ("/usr/local/firefox/firefox"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
            }
            retValue = new NbProcessDescriptor(
                prg,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage(FirefoxBrowser.class, "MSG_BrowserExecutorHint")
            );
        }
        return retValue;    
    }

}
