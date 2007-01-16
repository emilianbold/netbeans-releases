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
    
    protected String iconResource () {
        return "org/netbeans/core/resources/actions/htmlView.gif"; // NOI18N
    }

    public void performAction() {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle(HTMLViewAction.class).getString("CTL_OpeningBrowser"));
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new java.net.URL(HtmlBrowser.getHomePage ()
                    ));
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
