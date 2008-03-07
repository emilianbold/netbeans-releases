/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
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
        JPanel panel = new JPanel();
        tp.addPropertyChangeListener(this);
        jOK = new JButton(NbBundle.getMessage(BuildToolsAction.class, "BTN_OK")); // NOI18N
        tp.setPreferredSize(new Dimension(900, 550));
        tp.update();
        DialogDescriptor dd = new DialogDescriptor((Object) constructOuterPanel(tp), getTitle(), true, 
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
    
    private JPanel constructOuterPanel(JPanel innerPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        panel.add(innerPanel, gridBagConstraints);
        return panel;
    }
    
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
}
