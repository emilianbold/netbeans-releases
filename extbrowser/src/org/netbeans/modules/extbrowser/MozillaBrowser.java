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

package org.netbeans.modules.extbrowser;

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Martin Grebac
 */
public class MozillaBrowser extends ExtWebBrowser {

//    /** storage for starting browser timeout property */
//    protected int browserStartTimeout = 6000;

    private static final long serialVersionUID = -3982770681461437966L;

    /** Creates new ExtWebBrowser */
    public MozillaBrowser() {
        ddeServer = ExtWebBrowser.MOZILLA;
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
                detectedPath = NbDdeBrowserImpl.getBrowserPath("MOZILLA");      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log("Cannot detect Mozilla : " + e);      // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return (Utilities.isUnix() && !Utilities.isMac()) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(MozillaBrowser.class, "CTL_MozillaBrowserName");
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
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (MozillaBrowser.class, "MSG_CannotUseBrowser"));
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
                    prg = "C:\\Program Files\\Mozilla.org\\Mozilla\\mozilla.exe";     // NOI18N
            } catch (UnsatisfiedLinkError e) {
                prg = "iexplore";                                     // NOI18N
            }
        
            retValue = new NbProcessDescriptor (prg, params);
            return retValue;            
        
        //Unix
        } else { 
            
            prg = "mozilla";                                                      // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                java.io.File f = new java.io.File ("/usr/bin/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
                f = new java.io.File ("/usr/local/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                java.io.File f = new java.io.File ("/usr/sfw/lib/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                } else {
                    f = new java.io.File ("/opt/csw/bin/mozilla"); // NOI18N
                    if (f.exists()) {
                        prg = f.getAbsolutePath();
                    }
                }
            }
            retValue = new NbProcessDescriptor(
                prg,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage(MozillaBrowser.class, "MSG_BrowserExecutorHint")
            );
        }
        return retValue;    
    }
    
}
