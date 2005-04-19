/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.extbrowser;

import org.openide.util.Utilities;
import java.net.URL;

/**
 * @author  snajper
 */
public class DelegatingWebBrowserImpl extends ExtBrowserImpl {
        
    private NbDdeBrowserImpl ddeImpl;
    private UnixBrowserImpl unixImpl;
    private SimpleExtBrowserImpl simpleImpl;
  
    /** Creates a new instance of DelegatingWebBrowserImpl */
    public DelegatingWebBrowserImpl() {
    }
    
    /** Creates a new instance of DelegatingWebBrowserImpl */
    public DelegatingWebBrowserImpl(ExtWebBrowser extBrowserFactory) {
        this.extBrowserFactory = extBrowserFactory;
    }

    public ExtBrowserImpl getImplementation() {
        String pName = extBrowserFactory.getBrowserExecutable().getProcessName().toUpperCase();
                
        if (pName != null) {
            
            // Windows -> DDE browser if it is Mozilla, or Netscape 4.x or Netscape 7.x or Internet Explorer
            // Netscape6 is also simple command-line
            if (Utilities.isWindows()) {
                if (pName.indexOf("IEXPLORE.EXE") > -1 ||       // NOI18N
                    pName.indexOf("NETSCP.EXE") > -1 ||         // NOI18N
                    pName.indexOf("MOZILLA.EXE") > -1 ||        // NOI18N
                    pName.indexOf("FIREFOX.EXE") > -1 ||        // NOI18N
                    pName.indexOf("NETSCAPE.EXE") > -1) {       // NOI18N
                        if (ddeImpl == null) {
                            ddeImpl = new NbDdeBrowserImpl(extBrowserFactory);
                        }
                        return ddeImpl;
                }

            // Unix (but not MacOSX) -> if Netscape or Mozilla, create Unix browser
            } else if (Utilities.isUnix() && Utilities.getOperatingSystem() != Utilities.OS_MAC) {
                if (pName.indexOf("MOZILLA") > -1 ||            // NOI18N
                    pName.indexOf("NETSCAPE") > -1 || 
                    pName.indexOf("FIREFOX") > -1) {           // NOI18N
                        if (unixImpl == null) {
                            unixImpl = new UnixBrowserImpl(extBrowserFactory);
                        }
                        return unixImpl;
                }
            }
        }
        
        // otherwise simple command-line browser
        if (simpleImpl == null) {
            simpleImpl = new SimpleExtBrowserImpl(extBrowserFactory);
        }
        return simpleImpl;
    }

    /** 
     *  Sets current URL.
     * @param url URL to show in the browser.
     */
    public void setURL(URL url) {
        getImplementation().setURL(url);        
    }
        
}
