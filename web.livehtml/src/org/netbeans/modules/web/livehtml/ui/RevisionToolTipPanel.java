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
package org.netbeans.modules.web.livehtml.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.ui.data.DataToolTipProvider;
import org.netbeans.modules.web.livehtml.ui.stacktrace.StackTraceToolTipProvider;

/**
 *
 * @author petr-podzimek
 */
public class RevisionToolTipPanel extends javax.swing.JPanel {
    
    private List<RevisionToolTipService> revisionToolTipServices = new ArrayList<RevisionToolTipService>();
    private Component lastUserSelectedTab = null;

    /**
     * Creates new form RevisionToolTipPanel
     */
    public RevisionToolTipPanel(Project p) {
        initComponents();
        
        revisionToolTipServices.add(new StackTraceToolTipProvider(p));
//        revisionToolTipServices.add(new ChangesToolTipProvider());
        revisionToolTipServices.add(new DataToolTipProvider());
        
        SingleSelectionModel singleSelectionModel = toolTipTabbedPane.getModel();
        if (singleSelectionModel != null) {
            singleSelectionModel.addChangeListener(new PrivateChangeListener());
        }
    }

    public void setRevision(final Revision revision, final String toolTipTitle, final boolean reformatContent) {
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                titleLabel.setText(toolTipTitle);
                titleLabel.setToolTipText(toolTipTitle);

                toolTipTabbedPane.removeAll();
                
                if (revision != null) {
                    for (RevisionToolTipService revisionToolTipService : revisionToolTipServices) {
                        final boolean canProcess = revisionToolTipService.canProcess(revision, reformatContent);
                        if (canProcess) {
                            revisionToolTipService.setRevision(revision, reformatContent);
                            toolTipTabbedPane.addTab(revisionToolTipService.getDisplayName(), revisionToolTipService);
                        } else {
                            revisionToolTipService.clearRevision();
                        }
                        revisionToolTipService.revalidate();
                        revisionToolTipService.repaint();
                    }
                }

                if (lastUserSelectedTab != null) {
                    final int index = toolTipTabbedPane.indexOfComponent(lastUserSelectedTab);
                    if (index > 0) {
                        toolTipTabbedPane.setSelectedComponent(lastUserSelectedTab);
                    }
                }

                revalidate();
                repaint();
            }
        });
        
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolTipTabbedPane = new javax.swing.JTabbedPane();
        titleLabel = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(350, 250));
        setMinimumSize(new java.awt.Dimension(200, 200));
        setPreferredSize(new java.awt.Dimension(350, 250));

        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(RevisionToolTipPanel.class, "RevisionToolTipPanel.titleLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolTipTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolTipTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTabbedPane toolTipTabbedPane;
    // End of variables declaration//GEN-END:variables

    private class PrivateChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            final Component selectedComponent = toolTipTabbedPane.getSelectedComponent();
            final int selectedIndex = toolTipTabbedPane.getSelectedIndex();
            if (selectedComponent != null && 
                    selectedIndex >= 0 && 
                    toolTipTabbedPane.getTabCount() > 1) { // this eliminate selection when first Tab is added (and selected).
                lastUserSelectedTab = selectedComponent;
            }
        }
        
    }
    
}
