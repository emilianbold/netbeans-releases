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
