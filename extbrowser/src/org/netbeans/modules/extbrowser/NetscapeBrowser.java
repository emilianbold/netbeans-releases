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
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;

/*
 * @author Martin Grebac
 */
public class NetscapeBrowser extends ExtWebBrowser implements PropertyChangeListener {
    
    private static final long serialVersionUID = -2097024098026706995L;    

    /** storage for starting browser timeout property */
    //protected int browserStartTimeout = 6000;

    /** Creates new ExtWebBrowser */
    public NetscapeBrowser() {
        init();
    }
    
    public void init() {
        //Windows - we don't care about executable changes, because there's no need to setup ddeserver on non-windows platform
        if (Utilities.isWindows()) {
            ddeServer = ExtWebBrowser.NETSCAPE6;
            pcs.addPropertyChangeListener(this);
            //browserStartTimeout = 6000;
        }
    }

    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        String detectedPath = null;
        if (Utilities.isWindows()) {
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("NETSCP");       // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log("Cannot detect Netscape 7 : " + e);   // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }

            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("NETSCP6");      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log("Cannot detect Netscape 6 : " + e);   // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("NETSCAPE");     // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log("Cannot detect Netscape 4 : " + e);   // NOI18N
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
            this.name = NbBundle.getMessage(NetscapeBrowser.class, "CTL_NetscapeBrowserName");
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

        if (org.openide.util.Utilities.isWindows ()) {
            impl = new NbDdeBrowserImpl (this);
        } else if (Utilities.isUnix() && Utilities.getOperatingSystem() != Utilities.OS_MAC) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (NetscapeBrowser.class, "MSG_CannotUseBrowser"));
        }
        
        return impl;
    }

    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    protected NbProcessDescriptor defaultBrowserExecutable() {
        String b = "";
        String params = "";     // NOI18N
        NbProcessDescriptor retValue;
        
        //Windows
        if (Utilities.isWindows()) {
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            try {
                try {
                    b = NbDdeBrowserImpl.getBrowserPath("Netscp"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.NETSCAPE6);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                        ExtWebBrowser.getEM().log("Cannot get Path for Netscape 7: " + e);   // NOI18N
                    }
                }

                try {
                    b = NbDdeBrowserImpl.getBrowserPath("Netscp6");  // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.NETSCAPE6);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                        ExtWebBrowser.getEM().log("Cannot get Path for Netscape 6: " + e);   // NOI18N
                    }
                }

                try {
                    b = NbDdeBrowserImpl.getBrowserPath("Netscape");  // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.NETSCAPE);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                        ExtWebBrowser.getEM().log("Cannot get Path for Netscape 4: " + e);   // NOI18N
                    }
                }
                
            } catch (UnsatisfiedLinkError e) {
                if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                    ExtWebBrowser.getEM().log("Some problem here:" + e);   // NOI18N
                }
            }

            b = "C:\\PROGRA~1\\Netscape\\NETSCA~1\\netscp.exe";  // NOI18N
            setDDEServer(ExtWebBrowser.NETSCAPE6);
            
            retValue = new NbProcessDescriptor (b, params);
        
        //Unix
        } else {

            b = "netscape"; // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                java.io.File f = new java.io.File ("/usr/dt/bin/sun_netscape"); // NOI18N
                if (f.exists()) {
                    b = f.getAbsolutePath();
                }
            }
            retValue = new NbProcessDescriptor (
                b, "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage (NetscapeBrowser.class, "MSG_BrowserExecutorHint")
            );                
        }
        
        return retValue;        
    }

}
