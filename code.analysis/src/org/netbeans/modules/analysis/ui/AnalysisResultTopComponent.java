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
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.analysis.AnalysisResult;
import org.netbeans.modules.analysis.DescriptionReader;
import org.netbeans.modules.analysis.RunAnalysis;
import org.netbeans.modules.analysis.RunAnalysisPanel.DialogState;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.analysis.ui//AnalysisResult//EN",
autostore = false)
@TopComponent.Description(preferredID = AnalysisResultTopComponent.PREFERRED_ID,
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position=12000)
@ActionID(category = "Window", id = "org.netbeans.modules.analysis.ui.AnalysisResultTopComponent")
@ActionReference(path = "Menu/Window/Output", position = 330)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AnalysisResultAction",
preferredID = "AnalysisResultTopComponent")
@Messages({
    "CTL_AnalysisResultAction=Inspector Window",
    "CTL_AnalysisResultTopComponent=Inspector Window",
    "HINT_AnalysisResultTopComponent=This is an Inspector Window"
})
public final class AnalysisResultTopComponent extends TopComponent implements ExplorerManager.Provider {

    static final String PREFERRED_ID = "AnalysisResultTopComponent";
    private final ExplorerManager manager = new ExplorerManager();

    private Lookup context;
    private DialogState dialogState;
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

        setData(Lookup.EMPTY, null, new AnalysisResult(Collections.<AnalyzerFactory, List<ErrorDescription>>emptyMap(), Collections.<Node>emptyList()));

        getActionMap().put("jumpNext", nextAction);
        getActionMap().put("jumpPrev", prevAction);

        HTMLEditorKit hek = new HTMLEditorKit();
        StyleSheet styleSheet = (hek).getStyleSheet();

        styleSheet.addRule("h1 { font-weight: bold; font-size: 100% }");
        hek.setStyleSheet(styleSheet);
        descriptionPanel.setEditorKit(hek);

        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = manager.getSelectedNodes();

                if (selectedNodes.length == 1) {
                    DescriptionReader rd = selectedNodes[0].getLookup().lookup(DescriptionReader.class);
                    CharSequence description = rd != null ? rd.getDescription() : null;
                    descriptionPanel.setText(description != null ? description.toString() : null);
                }
            }
        });

        descriptionPanel.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == EventType.ACTIVATED && e.getURL() != null) {
                    URLDisplayer.getDefault().showURL(e.getURL());
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
        byCategory.setMaximumSize(new java.awt.Dimension(24, 24));
        byCategory.setMinimumSize(new java.awt.Dimension(24, 24));
        byCategory.setPreferredSize(new java.awt.Dimension(24, 24));
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(previousError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nextError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(byCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(byCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        RunAnalysis.showDialogAndRunAnalysis(context, dialogState);
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void nextErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextErrorActionPerformed
        nextAction.actionPerformed(null);
    }//GEN-LAST:event_nextErrorActionPerformed

    private void previousErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousErrorActionPerformed
        prevAction.actionPerformed(null);
    }//GEN-LAST:event_previousErrorActionPerformed

    private void byCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_byCategoryActionPerformed
        manager.setRootContext(Nodes.constructSemiLogicalView(analysisResult, byCategory.isSelected()));
        updatePrevNextButtonsForNewRootContext();
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

    AnalysisResult analysisResult;

    public void setData(Lookup context, DialogState dialogState, AnalysisResult analysisResult) {
        this.context = context;
        this.dialogState = dialogState;
        this.analysisResult = analysisResult;
        manager.setRootContext(Nodes.constructSemiLogicalView(analysisResult, byCategory.isSelected()));
        if (btv != null) {
            btv.expandAll();
        }
        refreshButton.setEnabled(context != Lookup.EMPTY);
        updatePrevNextButtonsForNewRootContext();
    }
    
    private void updatePrevNextButtonsForNewRootContext() {
        descriptionPanel.setText(null);
        nodesForNext = null;
        seenNodes = null;
        empty = analysisResult.provider2Hints.isEmpty();
        fireActionEnabledChange();
        
        if (!byCategory.isSelected() && nextAction.isEnabled() && !empty) {
            nextAction.actionPerformed(null);
        }
    }

    void fireActionEnabledChange() {
        prevAction.fireEnabledChanged();
        nextAction.fireEnabledChanged();
    }

    public static synchronized AnalysisResultTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof AnalysisResultTopComponent) {
            return (AnalysisResultTopComponent) win;
        }
        if (win == null) {
            Logger.getLogger(AnalysisResultTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
        } else {
            Logger.getLogger(AnalysisResultTopComponent.class.getName()).warning(
                    "There seem to be multiple components with the '" + PREFERRED_ID +
                    "' ID. That is a potential source of errors and unexpected behavior.");
        }
        
        AnalysisResultTopComponent result = new AnalysisResultTopComponent();
        Mode outputMode = WindowManager.getDefault().findMode("output");
        
        if (outputMode != null) {
            outputMode.dockInto(result);
        }
        return result;
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
