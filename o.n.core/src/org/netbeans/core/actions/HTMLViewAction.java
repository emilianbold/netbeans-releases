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

package org.netbeans.core.actions;

import org.openide.awt.HtmlBrowser;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.core.IDESettings;

/** Activates last opened HTML browser or opens a HTML Browser on the home URL 
 *  specified in IDESettings using HtmlBrowser.URLDisplayer.showURL().
*
* @author Ian Formanek
*/
public class HTMLViewAction extends CallableSystemAction {
    
    public HTMLViewAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    public static final String MODE_NAME = "webbrowser"; // NOI18N

    protected String iconResource () {
        return "org/netbeans/core/resources/actions/htmlView.gif"; // NOI18N
    }

    public void performAction() {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle(HTMLViewAction.class).getString("CTL_OpeningBrowser"));
        try {
            Mode mode = WindowManager.getDefault().findMode(MODE_NAME);
            if (mode != null) {
                TopComponent [] comps = mode.getTopComponents ();
                if (comps.length > 0) {
                    comps[0].open ();
                    comps[0].requestActive ();
                }
		else {
		    HtmlBrowser.URLDisplayer.getDefault().showURL(
			    new java.net.URL(HtmlBrowser.getHomePage ()
			    ));
		}
            }
        } catch (java.net.MalformedURLException e) {
            if (!HtmlBrowser.getHomePage ().
              startsWith ("http://") // NOI18N
            ) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(
                        new java.net.URL("http://" + HtmlBrowser.getHomePage () // NOI18N
                    ));
                } catch (java.net.MalformedURLException e1) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(
                        IDESettings.getRealHomeURL ());
                }
            } else
                HtmlBrowser.URLDisplayer.getDefault().showURL(IDESettings.getRealHomeURL ());
        }
        org.openide.awt.StatusDisplayer.getDefault().setStatusText (""); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(HTMLViewAction.class).getString("HTMLView");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HTMLViewAction.class);
    }

}
