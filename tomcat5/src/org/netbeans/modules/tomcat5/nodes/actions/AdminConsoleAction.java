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

package org.netbeans.modules.tomcat5.nodes.actions;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.TomcatInstanceNode;
import org.openide.ErrorManager;
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
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
