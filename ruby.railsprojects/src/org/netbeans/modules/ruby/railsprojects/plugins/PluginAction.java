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
package org.netbeans.modules.ruby.railsprojects.plugins;

import java.awt.Dialog;
import org.netbeans.api.project.Project;

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class PluginAction extends NodeAction {

    protected void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        Project p = lookup.lookup(Project.class);

        if (!RubyInstallation.getInstance().isValidRuby(true)) {
            return;
        }
        
        RailsProject project = (RailsProject)p;

        PluginManager manager = new PluginManager(project);
        String pluginProblem = manager.getPluginProblem();

        if (pluginProblem != null) {
            NotifyDescriptor nd =
                new NotifyDescriptor.Message(pluginProblem, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

            return;
        }

        PluginPanel customizer = new PluginPanel(manager);
        javax.swing.JButton close =
            new javax.swing.JButton(NbBundle.getMessage(PluginAction.class, "CTL_Close"));
        close.getAccessibleContext()
             .setAccessibleDescription(NbBundle.getMessage(PluginAction.class, "AD_Close"));

        DialogDescriptor descriptor =
            new DialogDescriptor(customizer, NbBundle.getMessage(PluginAction.class, "CTL_PluginTitle"),
                true, new Object[] { close }, close, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(PluginAction.class), null); // NOI18N
        Dialog dlg = null;

        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }

        // The roots don't include Rails plugins yet anyway
        //if (customizer.isModified()) {
        //    RubyInstallation.getInstance().recomputeRoots();
        //}
    }

    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        Lookup lookup = activatedNodes[0].getLookup();
        Project project = lookup.lookup(Project.class);

        return project instanceof RailsProject;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(PluginAction.class, "CTL_PluginAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }
}
