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

import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Martin Grebac
 */
public class IExplorerBrowser extends ExtWebBrowser {

//    /** storage for starting browser timeout property */
//    protected int browserStartTimeout = 5000;

    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        return (Utilities.isWindows()) ? Boolean.FALSE : Boolean.TRUE;
    }
            
    private static final long serialVersionUID = 6433332055280422486L;
    
    /** Creates new ExtWebBrowser */
    public IExplorerBrowser() {
        ddeServer = ExtWebBrowser.IEXPLORE;
    }

    /** Getter for browser name
     *  @return name of browser
     */
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(IExplorerBrowser.class, "CTL_IExplorerBrowserName");
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
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (IExplorerBrowser.class, "MSG_CannotUseBrowser"));
        }
        
        return impl;
    }
        
    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {
        String b;
        String params = "-nohome ";    // NOI18N

        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
        try {
            b = NbDdeBrowserImpl.getBrowserPath(getDDEServer ());
        } catch (NbBrowserException e) {
            b = "C:\\Program Files\\Internet Explorer\\iexplore.exe";     // NOI18N
        } catch (UnsatisfiedLinkError e) {
            // someone is customizing this on non-Win platform
            b = "iexplore";     // NOI18N
        }
        if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
            ExtWebBrowser.getEM().log(ErrorManager.INFORMATIONAL, "" + System.currentTimeMillis() + " IE: defaultBrowserExecutable: " + params + ", " + b);
        }
        return new NbProcessDescriptor (b, params);
    }
    
    private void readObject (java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }
    
    
}
