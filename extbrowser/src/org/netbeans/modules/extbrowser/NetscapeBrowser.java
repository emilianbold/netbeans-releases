/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.extbrowser;

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
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
                ExtWebBrowser.getEM().log(Level.INFO, "Cannot detect Netscape 7 : " + e);   // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }

            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("NETSCP6");      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.INFO, "Cannot detect Netscape 6 : " + e);   // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("NETSCAPE");     // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.INFO, "Cannot detect Netscape 4 : " + e);   // NOI18N
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
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
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
                    if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                        ExtWebBrowser.getEM().log(Level.FINE, "Cannot get Path for Netscape 7: " + e);   // NOI18N
                    }
                }

                try {
                    b = NbDdeBrowserImpl.getBrowserPath("Netscp6");  // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.NETSCAPE6);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                        ExtWebBrowser.getEM().log(Level.FINE, "Cannot get Path for Netscape 6: " + e);   // NOI18N
                    }
                }

                try {
                    b = NbDdeBrowserImpl.getBrowserPath("Netscape");  // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(ExtWebBrowser.NETSCAPE);
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e) {
                    if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                        ExtWebBrowser.getEM().log(Level.FINE, "Cannot get Path for Netscape 4: " + e);   // NOI18N
                    }
                }
                
            } catch (UnsatisfiedLinkError e) {
                if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
                    ExtWebBrowser.getEM().log(Level.FINE, "Some problem here:" + e);   // NOI18N
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
