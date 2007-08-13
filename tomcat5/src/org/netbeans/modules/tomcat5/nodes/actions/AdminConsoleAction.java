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

package org.netbeans.modules.tomcat5.nodes.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.TomcatInstanceNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action which opens the Tomcat Administration console in a browser.
 *
 * @author sherold
 */
public class AdminConsoleAction extends NodeAction {
    
    protected void performAction (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            final TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        TomcatManager tm = cookie.getTomcatManager();
                        String adminUrl = tm.getServerUri() + "/admin"; // NOI18N
                        try {
                            URLDisplayer.getDefault().showURL(new URL(adminUrl));
                        } catch (MalformedURLException e) {
                            Logger.getLogger(AdminConsoleAction.class.getName()).log(Level.INFO, null, e);
                        }
                    }
                });
            }
        }
    }

    protected boolean enable (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie == null) {
                return false;
            }
        }
        return true;
    }

    public String getName () {
        return NbBundle.getMessage(AdminConsoleAction.class, "LBL_AdminConsoleAction");
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
}
