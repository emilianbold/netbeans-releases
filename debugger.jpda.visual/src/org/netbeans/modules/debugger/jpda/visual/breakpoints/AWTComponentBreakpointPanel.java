/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * AWTComponentBreakpointPanel.java
 *
 * Created on Aug 19, 2011, 8:10:58 AM
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ActionsPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ConditionsPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ControllerProvider;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public class AWTComponentBreakpointPanel extends javax.swing.JPanel implements ControllerProvider {
    
    private static final String         HELP_ID = "NetbeansDebuggerBreakpointComponentJPDA"; // NOI18N
    private AWTComponentBreakpoint      breakpoint;
    private LineBreakpoint              fakeActionsBP;
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private Controller                  controller = new CBController();
    private boolean                     createBreakpoint = false;
    
    private static AWTComponentBreakpoint createBreakpoint () {
        AWTComponentBreakpoint.ComponentDescription componentDescription = null;
        ScreenshotUIManager activeScreenshotManager = ScreenshotUIManager.getActive();
        if (activeScreenshotManager != null) {
            ComponentInfo ci = activeScreenshotManager.getSelectedComponent();
            if (ci instanceof AWTComponentInfo) {
                componentDescription = new AWTComponentBreakpoint.ComponentDescription(
                        ci,
                        ((AWTComponentInfo) ci).getThread().getDebugger(),
                        ((AWTComponentInfo) ci).getComponent());
            }
        }
        if (componentDescription == null) {
            componentDescription = new AWTComponentBreakpoint.ComponentDescription("");
        }
        AWTComponentBreakpoint cb = new AWTComponentBreakpoint(componentDescription);
        /*cb.setPrintText (
            NbBundle.getBundle (LineBreakpointPanel.class).getString 
                ("CTL_Line_Breakpoint_Print_Text")
        );*/
        return cb;
    }
    

    public AWTComponentBreakpointPanel() {
        this (createBreakpoint (), true);
    }
    
    /** Creates new form AWTComponentBreakpointPanel */
    public AWTComponentBreakpointPanel(AWTComponentBreakpoint cb) {
        this(cb, false);
    }
    
    public AWTComponentBreakpointPanel(AWTComponentBreakpoint cb, boolean createBreakpoint) {
        this.breakpoint = cb;
        this.createBreakpoint = createBreakpoint;
        initComponents();
        int type = cb.getType();
        componentTextField.setText(cb.getComponent().getComponentInfo().getDisplayName());
        addRemoveCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_ADD) != 0 || (type & AWTComponentBreakpoint.TYPE_REMOVE) != 0);
        showHideCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_SHOW) != 0 || (type & AWTComponentBreakpoint.TYPE_HIDE) != 0);
        repaintCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0);
        conditionsPanel = new ConditionsPanel(HELP_ID);
        conditionsPanel.setupConditionPaneContext();
        conditionsPanel.showClassFilter(false);
        conditionsPanel.setCondition(cb.getCondition());
        conditionsPanel.setHitCountFilteringStyle(cb.getHitCountFilteringStyle());
        conditionsPanel.setHitCount(cb.getHitCountFilter());
        cPanel.add(conditionsPanel, "Center");  // NOI18N
        
        fakeActionsBP = LineBreakpoint.create("", 0);
        fakeActionsBP.setPrintText (
            NbBundle.getBundle (AWTComponentBreakpointPanel.class).getString 
                ("CTL_Component_Breakpoint_Print_Text")
        );

        actionsPanel = new ActionsPanel (fakeActionsBP);
        aPanel.add (actionsPanel, "Center");  // NOI18N

        
    }

    @Override
    public Controller getController() {
        return controller;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sPanel = new javax.swing.JPanel();
        componentLabel = new javax.swing.JLabel();
        componentTextField = new javax.swing.JTextField();
        componentActionLabel = new javax.swing.JLabel();
        addRemoveCheckBox = new javax.swing.JCheckBox();
        showHideCheckBox = new javax.swing.JCheckBox();
        repaintCheckBox = new javax.swing.JCheckBox();
        cPanel = new javax.swing.JPanel();
        aPanel = new javax.swing.JPanel();
        pushPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        sPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "TTL_ComponentBreakpointSettings"))); // NOI18N

        componentLabel.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.componentLabel.text")); // NOI18N

        componentTextField.setEditable(false);
        componentTextField.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.componentTextField.text")); // NOI18N

        componentActionLabel.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.componentActionLabel.text")); // NOI18N

        addRemoveCheckBox.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.addRemoveCheckBox.text")); // NOI18N

        showHideCheckBox.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.showHideCheckBox.text")); // NOI18N

        repaintCheckBox.setText(org.openide.util.NbBundle.getMessage(AWTComponentBreakpointPanel.class, "AWTComponentBreakpointPanel.repaintCheckBox.text")); // NOI18N

        javax.swing.GroupLayout sPanelLayout = new javax.swing.GroupLayout(sPanel);
        sPanel.setLayout(sPanelLayout);
        sPanelLayout.setHorizontalGroup(
            sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sPanelLayout.createSequentialGroup()
                .addComponent(componentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(componentTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))
            .addGroup(sPanelLayout.createSequentialGroup()
                .addComponent(componentActionLabel)
                .addContainerGap())
            .addGroup(sPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(showHideCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRemoveCheckBox, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(127, Short.MAX_VALUE))
            .addGroup(sPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(repaintCheckBox)
                .addContainerGap(279, Short.MAX_VALUE))
        );
        sPanelLayout.setVerticalGroup(
            sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sPanelLayout.createSequentialGroup()
                .addGroup(sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(componentLabel)
                    .addComponent(componentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(componentActionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addRemoveCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showHideCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(repaintCheckBox))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(sPanel, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cPanel, gridBagConstraints);

        aPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(aPanel, gridBagConstraints);

        javax.swing.GroupLayout pushPanelLayout = new javax.swing.GroupLayout(pushPanel);
        pushPanel.setLayout(pushPanelLayout);
        pushPanelLayout.setHorizontalGroup(
            pushPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 366, Short.MAX_VALUE)
        );
        pushPanelLayout.setVerticalGroup(
            pushPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 168, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pushPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aPanel;
    private javax.swing.JCheckBox addRemoveCheckBox;
    private javax.swing.JPanel cPanel;
    private javax.swing.JLabel componentActionLabel;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JTextField componentTextField;
    private javax.swing.JPanel pushPanel;
    private javax.swing.JCheckBox repaintCheckBox;
    private javax.swing.JPanel sPanel;
    private javax.swing.JCheckBox showHideCheckBox;
    // End of variables declaration//GEN-END:variables

    private class CBController implements Controller {

        @Override
        public boolean ok() {
            breakpoint.setCondition (conditionsPanel.getCondition());
            breakpoint.setHitCountFilter(conditionsPanel.getHitCount(), conditionsPanel.getHitCountFilteringStyle());
            breakpoint.setSuspend(fakeActionsBP.getSuspend());
            breakpoint.setPrintText(fakeActionsBP.getPrintText());
            if (createBreakpoint)
                DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
            return true;
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        

    }
}
