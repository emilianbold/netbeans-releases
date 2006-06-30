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

import java.beans.*;
import java.io.IOException;
import java.net.URL;

import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.execution.NbProcessDescriptor;
import org.openide.ErrorManager;

/** Class that implements browsing.
 *  It starts new process whenever it is asked to display URL.
 */
public class SimpleExtBrowserImpl extends ExtBrowserImpl {

    public SimpleExtBrowserImpl(ExtWebBrowser extBrowserFactory) {
        super();
        this.extBrowserFactory = extBrowserFactory;
        if (ExtWebBrowser.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
            ExtWebBrowser.getEM().log("SimpleExtBrowserImpl created from factory: " + extBrowserFactory);    // NOI18N
        }
    }

    /** Given URL is displayed. 
      *  Configured process is started to satisfy this request. 
      */
    public void setURL(URL url) {
        if (url == null) {
            return;
        }
        
        try {
            url = URLUtil.createExternalURL(url, false);
            NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
            if (np != null) {
                np.exec(new SimpleExtBrowser.BrowserFormat((url == null)? "": url.toString())); // NOI18N
            }
            this.url = url;
        } catch (IOException ex) {
            org.openide.DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(SimpleExtBrowserImpl.class, "EXC_Invalid_Processor"), 
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE
                )
            );
        }
    }

}