/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import org.netbeans.modules.cnd.ui.options.LocalToolsPanelModel;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.netbeans.modules.cnd.ui.options.ToolsPanelModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Post the ToolsPanel as a standalone dialog.
 *
 * @author gordonp
 */
public class BuildToolsAction extends CallableSystemAction implements PropertyChangeListener {
    
    private String title;
    private String name;
    private JButton jOK = null;
    private ToolsPanel tp;
    private ToolsPanelModel model;
    
    public BuildToolsAction() {
        name = NbBundle.getMessage(BuildToolsAction.class, "LBL_BuildToolsName"); // NOI18N
        title = NbBundle.getMessage(BuildToolsAction.class, "LBL_BuildToolsTitle"); // NOI18N
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void performAction() {
        initBuildTools(new LocalToolsPanelModel(), new ArrayList<String>());
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(ToolsPanel.PROP_VALID) &&
                ev.getSource() instanceof ToolsPanel) {
            jOK.setEnabled(((Boolean) ev.getNewValue()).booleanValue());
        }
    }
    
    public ToolsPanelModel getModel() {
        return model;
    }
    
    /**
     * Initialize the build tools
     *
     * @returns true if the user pressed OK, false if Cancel
     */
    public boolean initBuildTools(ToolsPanelModel model, ArrayList<String> errs) {
        if (Boolean.getBoolean("netbeans.cnd.bta_debug")) { // NOI18N
            // The following should be shown in the TP, but did not get implemented for CND 5.5.1
            for (String err : errs) { // GRP - FIXME
                System.err.println(err);
            }
        }
        tp = new ToolsPanel(model);
        tp.addPropertyChangeListener(this);
        jOK = new JButton(NbBundle.getMessage(BuildToolsAction.class, "BTN_OK")); // NOI18N
        tp.setPreferredSize(new Dimension(500, 365));
        tp.update();
        DialogDescriptor dd = new DialogDescriptor((Object) tp, getTitle(), true, 
                new Object[] { jOK, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == jOK) {
            tp.applyChanges(true);
            return true;
        }
        return false;
    }
    
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
}
