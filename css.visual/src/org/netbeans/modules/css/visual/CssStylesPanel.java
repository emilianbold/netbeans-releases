/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.spi.CssStylesPanelProvider;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class CssStylesPanel extends javax.swing.JPanel {

     private RuleEditorController controller;
     private Collection<? extends CssStylesPanelProvider> providers;
     private ActionListener toolbarListener;
     
     private JComponent activePanel;
    
    /**
     * Creates new form CssStylesPanel
     */
    public CssStylesPanel() {
        initComponents();

        //assumption: should not change in time, otherwise we need to listen
        providers = Lookup.getDefault().lookupAll(CssStylesPanelProvider.class);
        
        //the bottom component
        controller = RuleEditorController.createInstance();
        splitPane.setBottomComponent(controller.getRuleEditorComponent());
        
        //toolbar
        toolbarListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                //linear search, but should be max 2 or 3 items
                for(CssStylesPanelProvider provider : providers) {
                    if(provider.getPanelID().equals(command)) {
                        setActivePanel(provider.getContent());
                    }
                }
            }
        };
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Button group for document and source buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        
        boolean first = true;
        for(CssStylesPanelProvider provider : providers) {
            JToggleButton button = new JToggleButton();
            button.setText(provider.getPanelDisplayName());
            button.setActionCommand(provider.getPanelID());
            
            button.setFocusPainted(false);
            button.addActionListener(toolbarListener);
            buttonGroup.add(button);
            toolBar.add(button);
            
            button.setSelected(first);
            if(first) {
                setActivePanel(provider.getContent());
                first = false;
            }
        }
        
        //the top component
        topPanel.add(toolBar, BorderLayout.PAGE_START);

        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
    }
    
    private void setActivePanel(JComponent panel) {
        if(panel == activePanel) {
            //no change
        }
        
        if(activePanel != null) {
            topPanel.remove(activePanel);
        }
        
        topPanel.add(panel, BorderLayout.CENTER);
        activePanel = panel;

        revalidate();
        repaint();
    }
    
     /**
     * Returns the default {@link RuleEditorController} associated with this
     * rule editor top component.
     */
    public RuleEditorController getRuleEditorController() {
        return controller;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        topPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerSize(4);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        topPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setTopComponent(topPanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
