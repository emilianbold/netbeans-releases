/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.actions;

import org.openide.TopManager;
import org.openide.awt.HtmlBrowser;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.core.IDESettings;

/** Activates last opened HTML browser or opens a HTML Browser on the home URL 
 *  specified in IDESettings using TopManager.showUrl().
*
* @author Ian Formanek
*/
public class HTMLViewAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 281181711813174400L;

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/htmlView.gif"; // NOI18N
    }

    public void performAction() {
        TopManager tm = TopManager.getDefault();
        tm.setStatusText (NbBundle.getBundle(HTMLViewAction.class).
            getString("CTL_OpeningBrowser")
        );
        try {
            boolean notFound = true;
            
            // is browser open on current workspace?
            Workspace workspace = tm.getWindowManager ().getCurrentWorkspace ();
            Mode mode = workspace.findMode (HtmlBrowser.BrowserComponent.MODE_NAME);
            if (mode != null) {
                TopComponent [] comps = mode.getTopComponents ();
                if (comps.length > 0) {
                    comps[0].open ();
                    comps[0].requestFocus ();
                    notFound = false;
                }
            }
            // is it open on any workspace?
            if (notFound) {
                Workspace [] workspaces = tm.getWindowManager ().getWorkspaces ();
                if (workspaces != null) {
                    for (int i=0; i<workspaces.length; i++) {
                        mode = workspaces[i].findMode (HtmlBrowser.BrowserComponent.MODE_NAME);
                        if (mode != null) {
                            TopComponent [] comps = mode.getTopComponents ();
                            if (comps.length > 0) {
                                comps[0].open ();
                                comps[0].requestFocus ();
                                notFound = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (notFound) {
                tm.showUrl (new java.net.URL (
                    HtmlBrowser.getHomePage ()
                ));
            }
        } catch (java.net.MalformedURLException e) {
            if (!HtmlBrowser.getHomePage ().
              startsWith ("http://")
            ) {
                try {
                    tm.showUrl (new java.net.URL (
                        "http://" + HtmlBrowser.getHomePage ()
                    ));
                } catch (java.net.MalformedURLException e1) {
                    tm.showUrl (IDESettings.getRealHomeURL ());
                }
            } else
                tm.showUrl (IDESettings.getRealHomeURL ());
        }
        tm.setStatusText (""); // NOI18N
    }

    public String getName() {
        return NbBundle.getBundle(HTMLViewAction.class).getString("HTMLView");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (HTMLViewAction.class);
    }

}
