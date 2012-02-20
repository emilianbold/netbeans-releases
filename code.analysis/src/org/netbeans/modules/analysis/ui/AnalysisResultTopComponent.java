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
package org.netbeans.modules.analysis.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.analysis.RunAnalysis;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.analysis.ui//AnalysisResult//EN",
autostore = false)
@TopComponent.Description(preferredID = AnalysisResultTopComponent.PREFERRED_ID,
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.analysis.ui.AnalysisResultTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AnalysisResultAction",
preferredID = "AnalysisResultTopComponent")
@Messages({
    "CTL_AnalysisResultAction=AnalysisResult",
    "CTL_AnalysisResultTopComponent=AnalysisResult Window",
    "HINT_AnalysisResultTopComponent=This is a AnalysisResult window"
})
public final class AnalysisResultTopComponent extends TopComponent implements ExplorerManager.Provider {

    static final String PREFERRED_ID = "AnalysisResultTopComponent";
    private final ExplorerManager manager = new ExplorerManager();

    private Lookup context;
    private BeanTreeView btv;
    
    public AnalysisResultTopComponent() {
        initComponents();
        setName(Bundle.CTL_AnalysisResultTopComponent());
        setToolTipText(Bundle.HINT_AnalysisResultTopComponent());

        btv = new BeanTreeView();

        btvHolder.setLayout(new BorderLayout());
        btvHolder.add(btv, BorderLayout.CENTER);

        btv.setRootVisible(false);

        prevAction = new PreviousError(this);
        nextAction = new NextError(this);

        PCLImpl l = new PCLImpl();

        prevAction.addPropertyChangeListener(l);
        nextAction.addPropertyChangeListener(l);

        setData(Lookup.EMPTY, Collections.<Analyzer, List<ErrorDescription>>emptyMap());

        getActionMap().put("jumpNext", nextAction);
        getActionMap().put("jumpPrev", prevAction);

        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = manager.getSelectedNodes();

                if (selectedNodes.length == 1) {
                    ErrorDescription ed = selectedNodes[0].getLookup().lookup(ErrorDescription.class);
                    CharSequence description = ed != null ? ed.getDetails() : null;
                    descriptionPanel.setText(description != null ? description.toString() : null);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        refreshButton = new javax.swing.JButton();
        nextError = new javax.swing.JButton();
        previousError = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        btvHolder = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionPanel = new javax.swing.JTextPane();
        byCategory = new javax.swing.JToggleButton();

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.refreshButton.toolTipText")); // NOI18N
        refreshButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        refreshButton.setContentAreaFilled(false);
        refreshButton.setMaximumSize(new java.awt.Dimension(24, 24));
        refreshButton.setMinimumSize(new java.awt.Dimension(24, 24));
        refreshButton.setPreferredSize(new java.awt.Dimension(24, 24));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        nextError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/nextmatch.png"))); // NOI18N
        nextError.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.nextError.toolTipText")); // NOI18N
        nextError.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        nextError.setContentAreaFilled(false);
        nextError.setMaximumSize(new java.awt.Dimension(24, 24));
        nextError.setMinimumSize(new java.awt.Dimension(24, 24));
        nextError.setPreferredSize(new java.awt.Dimension(24, 24));
        nextError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextErrorActionPerformed(evt);
            }
        });

        previousError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/prevmatch.png"))); // NOI18N
        previousError.setToolTipText(org.openide.util.NbBundle.getBundle(AnalysisResultTopComponent.class).getString("AnalysisResultTopComponent.previousError.toolTipText")); // NOI18N
        previousError.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        previousError.setContentAreaFilled(false);
        previousError.setMaximumSize(new java.awt.Dimension(24, 24));
        previousError.setMinimumSize(new java.awt.Dimension(24, 24));
        previousError.setPreferredSize(new java.awt.Dimension(24, 24));
        previousError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousErrorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout btvHolderLayout = new javax.swing.GroupLayout(btvHolder);
        btvHolder.setLayout(btvHolderLayout);
        btvHolderLayout.setHorizontalGroup(
            btvHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        btvHolderLayout.setVerticalGroup(
            btvHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 298, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(btvHolder);

        descriptionPanel.setContentType(org.openide.util.NbBundle.getMessage(AnalysisResultTopComponent.class, "AnalysisResultTopComponent.descriptionPanel.contentType")); // NOI18N
        descriptionPanel.setEditable(false);
        jScrollPane1.setViewportView(descriptionPanel);

        jSplitPane1.setRightComponent(jScrollPane1);

        byCategory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/analysis/ui/resources/categorize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(byCategory, org.openide.util.NbBundle.getMessage(AnalysisResultTopComponent.class, "AnalysisResultTopComponent.byCategory.text")); // NOI18N
        byCategory.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        byCategory.setContentAreaFilled(false);
        byCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                byCategoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(previousError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nextError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(byCategory, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(previousError, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(nextError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(byCategory)
                .addContainerGap())
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        RunAnalysis.showDialogAndRunAnalysis();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void nextErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextErrorActionPerformed
        nextAction.actionPerformed(null);
    }//GEN-LAST:event_nextErrorActionPerformed

    private void previousErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousErrorActionPerformed
        prevAction.actionPerformed(null);
    }//GEN-LAST:event_previousErrorActionPerformed

    private void byCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_byCategoryActionPerformed
        manager.setRootContext(Nodes.constructSemiLogicalView(hints, byCategory.isSelected()));
    }//GEN-LAST:event_byCategoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btvHolder;
    private javax.swing.JToggleButton byCategory;
    private javax.swing.JTextPane descriptionPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton nextError;
    private javax.swing.JButton previousError;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    List<Node> nodesForNext;
    boolean empty;
    List<Node> seenNodes;
    final PreviousError prevAction;
    final NextError nextAction;

    Map<Analyzer, List<ErrorDescription>> hints;

    public void setData(Lookup context, Map<Analyzer, List<ErrorDescription>> provider2Hints) {
        this.context = context;
        this.hints = provider2Hints;
        manager.setRootContext(Nodes.constructSemiLogicalView(provider2Hints, byCategory.isSelected()));
        if (btv != null) {
            btv.expandAll();
        }
        refreshButton.setEnabled(context != Lookup.EMPTY);
        nodesForNext = null;
        empty = provider2Hints.isEmpty();
        fireActionEnabledChange();
    }

    void fireActionEnabledChange() {
        prevAction.fireEnabledChanged();
        nextAction.fireEnabledChanged();
    }

    public static synchronized AnalysisResultTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            throw new IllegalStateException();
        }
        if (win instanceof AnalysisResultTopComponent) {
            return (AnalysisResultTopComponent) win;
        }
            throw new IllegalStateException();
    }


    private class PCLImpl implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name == null || "enabled".equals(name)) {
                previousError.setEnabled(prevAction.isEnabled());
                nextError.setEnabled(nextAction.isEnabled());
            }
        }

    }
}
