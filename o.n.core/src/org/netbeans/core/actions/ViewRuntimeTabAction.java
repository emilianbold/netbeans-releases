/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.actions;

import java.awt.event.ActionEvent;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import org.netbeans.core.NbMainExplorer;

/**
 * Action which opend <code>CurrentProjectNode.ProjectsTab</code> default component.
 *
 * @author  Peter Zavadsky
 */
public class ViewRuntimeTabAction extends SystemAction {
    
    public void actionPerformed(ActionEvent evt) {
        final TopComponent runtimeTab = NbMainExplorer.MainTab.createEnvironmentTab();
        runtimeTab.open();
        runtimeTab.requestFocus();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ViewRuntimeTabAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(ViewRuntimeTabAction.class,
                "CTL_ViewRuntimeTabAction");
    }
    
    protected String iconResource () {
        return "org/netbeans/core/resources/environment.gif"; // NOI18N
    }

}
