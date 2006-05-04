/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Browser factory for System default Win browser.
 *
 * @author Martin Grebac
 */
public class SystemDefaultBrowser extends ExtWebBrowser {

    private static final long serialVersionUID = -7317179197254112564L;
    
    private interface BrowseInvoker {
        void browse(URI uri) throws IOException;
    }
    private static BrowseInvoker MUSTANG_DESKTOP_BROWSE;
    static {
        try {
            Class desktop = Class.forName("java.awt.Desktop"); // NOI18N
            Method isDesktopSupported = desktop.getMethod("isDesktopSupported", null); // NOI18N
            Boolean b = (Boolean) isDesktopSupported.invoke(null, null);
            if (b.booleanValue()) {
                final Object desktopInstance = desktop.getMethod("getDesktop", null).invoke(null, null); // NOI18N
                Class desktopAction = Class.forName("java.awt.Desktop$Action"); // NOI18N
                Method isSupported = desktop.getMethod("isSupported", new Class[] {desktopAction}); // NOI18N
                Object browseConst = desktopAction.getField("BROWSE").get(null); // NOI18N
                b = (Boolean) isSupported.invoke(desktopInstance, new Object[] {browseConst});
                if (b.booleanValue()) {
                    final Method browse = desktop.getMethod("browse", new Class[] {URI.class}); // NOI18N
                    MUSTANG_DESKTOP_BROWSE = new BrowseInvoker() {
                        public void browse(URI uri) throws IOException {
                            try {
                                browse.invoke(desktopInstance, new Object[] {uri});
                            } catch (InvocationTargetException e) {
                                throw (IOException) e.getTargetException();
                            } catch (Exception e) {
                                Logger.getLogger(SystemDefaultBrowser.class.getName()).log(Level.WARNING, null, e);
                            }
                        }
                    };
                }
            }
        } catch (ClassNotFoundException e) {
            // JDK 5, ignore
        } catch (Exception e) {
            Logger.getLogger(SystemDefaultBrowser.class.getName()).log(Level.WARNING, null, e);
        }
    }
    
    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        return (Utilities.isWindows() || MUSTANG_DESKTOP_BROWSE != null) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /** Creates new ExtWebBrowser */
    public SystemDefaultBrowser() {
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        if (MUSTANG_DESKTOP_BROWSE != null) {
            return new MustangBrowserImpl();
        } else if (Utilities.isWindows ()) {
            return new NbDdeBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (SystemDefaultBrowser.class, "MSG_CannotUseBrowser"));
        }
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(SystemDefaultBrowser.class, "CTL_SystemDefaultBrowserName");
        }
        return name;
    }
    
    /** Setter for browser name
     */
    public void setName(String name) {
        // system default browser name shouldn't be changed
    }

    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {
        if (!Utilities.isWindows() || MUSTANG_DESKTOP_BROWSE != null) {
            return new NbProcessDescriptor("", ""); // NOI18N
        }
        
        String b;
        String params = "";     // NOI18N
        try {
            // finds HKEY_CLASSES_ROOT\\".html" and respective HKEY_CLASSES_ROOT\\<value>\\shell\\open\\command
            // we will ignore all params here
            b = NbDdeBrowserImpl.getDefaultOpenCommand ();
            String [] args = Utilities.parseParameters (b);

            if (args == null || args.length == 0) {
                throw new NbBrowserException ();
            }
            b = args[0];
            params += " {" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
        } catch (NbBrowserException e) {
            b = "";             // NOI18N
        } catch (UnsatisfiedLinkError e) {
            // someone is customizing this on non-Win platform
            b = "iexplore";     // NOI18N
        }

        NbProcessDescriptor p = new NbProcessDescriptor (b, params);
        return p;
    }
    
    private static final class MustangBrowserImpl extends ExtBrowserImpl {
        
        public MustangBrowserImpl() {
            assert MUSTANG_DESKTOP_BROWSE != null;
        }
        
        public void setURL(URL url) {
            URL extURL = URLUtil.createExternalURL(url, /* to be safe */false);
            try {
		URI uri = extURL.toURI();
                Logger.getLogger(SystemDefaultBrowser.class.getName()).fine("Calling java.awt.Desktop.browse("+uri+")");
                MUSTANG_DESKTOP_BROWSE.browse(uri);
            } catch (URISyntaxException e) {
                assert false : e;
            } catch (IOException e) {
                // Report in GUI?
                Logger.getLogger(SystemDefaultBrowser.class.getName()).log(Level.WARNING, null, e);
            }
        }
        
    }

}
