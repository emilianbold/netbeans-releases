/*
 * SimpleExtBrowserImpl.java
 *
 * Created on October 18, 2003, 10:35 AM
 */

package org.netbeans.modules.extbrowser;

import java.beans.*;
import java.io.IOException;
import java.net.URL;

import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.execution.NbProcessDescriptor;
import org.openide.ErrorManager;

/**
 *
 * @author  snajper
 */

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