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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which displays selected job in browser.
 *
 * @author Michal Mocnak
 */
public class OpenUrlAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (Node node : nodes) {
            OpenableInBrowser o = node.getLookup().lookup(OpenableInBrowser.class);
            
            if (null == o)
                continue;
            
            try {
                URLDisplayer.getDefault().showURL(new URL(o.getUrl()));
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
        }
    }
    
    protected boolean enable(Node[] nodes) {
        for (Node node : nodes) {
            OpenableInBrowser o = node.getLookup().lookup(OpenableInBrowser.class);
            
            if (null != o)
                return true;
        }
        
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenUrlAction.class, "LBL_OpenInBrowserAction"); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}