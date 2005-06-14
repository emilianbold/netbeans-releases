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
import java.awt.Image;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import org.netbeans.core.NbMainExplorer;

/**
 * Action which opens <code>CurrentProjectNode.ProjectsTab</code> default (runtime tab) component.
 *
 * @author  Peter Zavadsky
 */
public class ViewRuntimeTabAction extends AbstractAction
implements HelpCtx.Provider {
    

    public ViewRuntimeTabAction() {
        putValue(NAME, NbBundle.getMessage(ViewRuntimeTabAction.class,
                "CTL_ViewRuntimeTabAction"));
        putValue ("iconBase", "org/netbeans/core/resources/environment.gif"); // NOI18N
    }
    
    
    public void actionPerformed(ActionEvent evt) {
        final TopComponent runtimeTab = NbMainExplorer.MainTab.findEnvironmentTab();
        runtimeTab.open();
        runtimeTab.requestActive();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ViewRuntimeTabAction.class);
    }

}
