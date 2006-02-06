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
package org.netbeans.modules.subversion.ui.browser;

import org.netbeans.modules.subversion.SVNRoot;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import java.awt.Dialog;
import java.net.MalformedURLException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 *
 * @author Tomas Stupka
 */
public final class BrowserAction extends CallableSystemAction {
    
    public void performAction() {                
        
        SVNUrl svnURL = null;
        try {
            //svnURL = new SVNUrl("https://peterp.czech.sun.com/svnsecure"); 
            svnURL = new SVNUrl("http://peterp.czech.sun.com/svn"); 
            //svnURL = new SVNUrl("file:///data/subversion/");
        } catch (MalformedURLException ex) {
            ex.printStackTrace(); 
            return;
        }
        
        BrowserSelector selector = new BrowserSelector("LBL_RepositoryBrowser", false, false, false);        
        selector.setup(new SVNRoot(svnURL, SVNRevision.HEAD));
        
        DialogDescriptor dd = new DialogDescriptor(selector.getBrowserPanel(), "test dialog");
        dd.setModal(true);
        dd.setValid(true);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            SVNRoot[] svnRoots = selector.getSelectedRoots();
            for (int i = 0; i < svnRoots.length; i++) {
                System.out.println(" url " + svnRoots[i]);
            }
        } else {
            
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(BrowserAction.class, "CTL_BrowserAction");
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}