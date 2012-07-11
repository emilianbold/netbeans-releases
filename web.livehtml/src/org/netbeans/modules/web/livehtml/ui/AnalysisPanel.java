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

import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.json.simple.JSONArray;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.livehtml.AnalysisModel;
import org.netbeans.modules.web.livehtml.AnalysisModelListener;
import org.netbeans.modules.web.livehtml.Change;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author petr-podzimek
 */
@NbBundle.Messages({
    "CTL_RevisionLabel_ToolTip=Revision index", 
    "CTL_StartAnalysisButton_ToolTip=Start analysis of selected URL or file",
    "CTL_ReformatRevisionsButton_ToolTip=Revision is reformated when pressed",
    "CTL_AnalysisComboBox_ToolTip=Select existing analysis of enter URL for analysis"})
public class AnalysisPanel extends javax.swing.JPanel implements AnalysisModelListener, AnalysisListener {
    
    private AnalysisModel analysisModel = new AnalysisModel();
    
    private Map<Analysis, Revision> lastSelectedRevisions = new HashMap<Analysis, Revision>();
    private ToolTipSupport toolTipSupport = null;
    
    /**
     * Creates new form AnalysisPanel
     */
    public AnalysisPanel() {
        UIManager.put("Slider.paintValue", Boolean.FALSE);
        initComponents();
        analysisModel.addAnalysisModelListener(this);
        revisionEditorPane.getDocument().putProperty(JEditorPane.class, revisionEditorPane);
        ToolTipManager.sharedInstance().registerComponent(revisionEditorPane);

        updateAnalysis(null);
        updateRevisions(null);
        updateSourceUrl(analysisModel.getSourceUrl());
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(revisionEditorPane);
        if (editorUI != null) {
            toolTipSupport = editorUI.getToolTipSupport();
            toolTipSupport.setEnabled(true);
            toolTipSupport.setToolTipText("XXX XXX XXX");
        }
        
    }

    private void updateAnalysis(Analysis analysis) {
        if (analysisModel == null) {
            return;
        }
        if (analysis == null && !analysisModel.getAnalyses().isEmpty()) {
            analysis = analysisModel.getAnalyses().get(analysisModel.getAnalyses().size() - 1);
        }
        final Analysis selectedAnalysis = getSelectedAnalysis();
        if (selectedAnalysis != null) {
            selectedAnalysis.removeAnalysisListener(this);
        }
        
        if (analysis != null) {
            analysis.addAnalysisListener(this);
            updateRevisions(analysis);
        } else {
            updateRevisions(null);
        }
        
    }
    
    private void updateRevisions(Analysis analysis) {
        
        if (analysis == null || analysis.getRevisionsCount() <= 0) {
            revisionSlider.setEnabled(false);
            revisionSlider.setMaximum(1);
        } else {
            revisionSlider.setEnabled(analysis.getRevisionsCount() > 1);
            revisionSlider.setMaximum(analysis.getRevisionsCount());
        }
        
        updateRevision(getSelectedRevision());
    }
    
    private void updateRevision(Revision revision) {
        final Analysis selectedAnalysis = getSelectedAnalysis();
        if (selectedAnalysis != null && revision != null) {
            int index = Utilities.getRevisionIndex(revision, selectedAnalysis);
            revisionLabel.setText(index + " / " + (selectedAnalysis.getRevisionsCount()));
            
            revisionSlider.setValue(index);
            final Rectangle visibleRect = revisionEditorPane.getVisibleRect();
            revisionEditorPane.setText(revision.getContent());
            revisionEditorPane.setVisible(true);
            revisionEditorPane.getDocument().putProperty(Change.class, revision.getChanges());
            revisionEditorPane.scrollRectToVisible(visibleRect);
        } else {
            revisionLabel.setText("- / -");
            
            revisionEditorPane.setText(null);
            revisionEditorPane.setVisible(false);
            revisionEditorPane.getDocument().putProperty(Change.class, null);
        }
        
    }

    public void updateSourceUrl(URL sourceUrl) {
        analysisModel.setSourceUrl(sourceUrl);
        analysisComboBox.setEditable(sourceUrl == null);
        setAnalysises(analysisModel.getAnalyses());
    }

    public synchronized void setAnalysises(List<Analysis> analysises) {
        final Analysis selectedAnalysis = getSelectedAnalysis();
        analysisComboBox.removeAllItems();
        
        if (analysisComboBox.isEditable()) {
            analysisComboBox.addItem(null);
        }
        if (analysises != null) {
            List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
            for (Analysis analysis : analysises) {
                final AnalysisItem analysisItem = new AnalysisItem(analysis);
                analysisItems.add(analysisItem);
            }
            
            Collections.sort(analysisItems);
            
            for (AnalysisItem analysisItem : analysisItems) {
                analysisComboBox.addItem(analysisItem);
            }
            
        }
        selectAnalysis(selectedAnalysis);
    }
    
    private Analysis getSelectedAnalysis() {
        final AnalysisItem selectedAnalysisItem = getSelectedAnalysisItem();
        if (selectedAnalysisItem != null) {
            return selectedAnalysisItem.getAnalysis();
        }
        return null;
    }

    private AnalysisItem getSelectedAnalysisItem() {
        final Object selectedItem = analysisComboBox.getSelectedItem();
        if (selectedItem != null && selectedItem instanceof AnalysisItem) {
            AnalysisItem analysisItem = (AnalysisItem) selectedItem;
            return analysisItem;
        } else {
            return null;
        }
    }

    private String getSelectedUrl() {
        final Object selectedItem = analysisComboBox.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }
        if (selectedItem instanceof String) {
            return (String) selectedItem;
        }
        if (selectedItem instanceof AnalysisItem) {
            AnalysisItem analysisItem = (AnalysisItem) selectedItem;
            return analysisItem.getAnalysis().getSourceUrl().toExternalForm();
        }
        return null;
    }
    
    private Revision getSelectedRevision() {
        final Analysis selectedAnalysis = getSelectedAnalysis();
        Revision selectedRevision = null;
        Revision lastSelectedRevision = lastSelectedRevisions.get(selectedAnalysis);
        if (selectedAnalysis != null && lastSelectedRevision != null && 
                Utilities.getRevisionIndex(lastSelectedRevision, selectedAnalysis) <= (selectedAnalysis.getRevisionsCount()) &&
                revisionSlider.getValue() > 0 && 
                revisionSlider.getValue() <= selectedAnalysis.getRevisionsCount()) {
            selectedRevision = selectedAnalysis.getRevision(revisionSlider.getValue(), reformatRevisionButton.isSelected());
            return selectedRevision;
        }
        if (selectedAnalysis != null && selectedAnalysis.getRevisionsCount() > 0) {
            selectedRevision = selectedAnalysis.getRevision(selectedAnalysis.getRevisionsCount(), reformatRevisionButton.isSelected());
        }
        
        return selectedRevision;
    }
    
    private void selectAnalysis(Analysis analysis) {
        if (analysis == null) {
            analysisComboBox.setSelectedItem(null);
            return;
        }
        final AnalysisItem analysisItem = getAnalysisItem(analysis);
        analysisComboBox.setSelectedItem(analysisItem);
    }
    
    private AnalysisItem getAnalysisItem(Analysis analysis) {
        if (analysis == null) {
            return null;
        }
        for (int i = 0; i < analysisComboBox.getItemCount(); i++) {
            final Object item = analysisComboBox.getItemAt(i);
            if (item instanceof AnalysisItem) {
                AnalysisItem analysisItem = (AnalysisItem) item;
                if (analysis == analysisItem.getAnalysis()) {
                    return analysisItem;
                }
            }
        }
        return null;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getTooBarPanel() {
        return toolBarPanel;
    }

    @Override
    public void analysisAdded(Analysis analysis) {
        updateSourceUrl(analysisModel.getSourceUrl());
        updateAnalysis(analysis);
        selectAnalysis(analysis);
    }

    @Override
    public void revisionAdded(long timeStamp) {
        updateRevisions(getSelectedAnalysis());
    }

    private static BrowserSupport bs;
    private static BrowserSupport getPrivateBrowserSupport() {
        // there seems to be some problem in Chrome's WebKit Debugging protocol:
        // if the same browser tab is reload with the same or different URL then
        // frequnetly Chrome crashs - an internal dark blue error page is displayed
        // in Chrome saying that something went wrong. For now let's not reuse the same
        // tab but always open a new one. If no better solution is found then perhaps
        // we could use Chrome's API to close old tab before opening a new one.
        if (/*bs == null*/ true) {
            bs = BrowserSupport.create();
            bs.disablePageInspector();
            bs.enabledLiveHTML();
        }
        return bs;
    }
    
    private void updateCallStack(JSONArray callStack) {
        System.out.println("Adding Revision callStack=" + Utilities.convertCallStack(callStack));
    }
    
    private void updateData(StringBuilder data) {
        if (data != null && data.length() != 0) {
            System.out.println("Adding Revision data=" + data);
            System.out.println("---");
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBarPanel = new javax.swing.JPanel();
        analysisComboBox = new javax.swing.JComboBox();
        reformatRevisionButton = new javax.swing.JToggleButton();
        startAnalysisButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        revisionScrollPane = new javax.swing.JScrollPane();
        revisionEditorPane = new javax.swing.JEditorPane();
        revisionLabel = new javax.swing.JLabel();
        revisionSlider = new javax.swing.JSlider();

        analysisComboBox.setToolTipText(Bundle.CTL_AnalysisComboBox_ToolTip());
        analysisComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                analysisComboBoxItemStateChanged(evt);
            }
        });

        reformatRevisionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/pretty.png"))); // NOI18N
        reformatRevisionButton.setToolTipText(Bundle.CTL_ReformatRevisionsButton_ToolTip());
        reformatRevisionButton.setFocusable(false);
        reformatRevisionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reformatRevisionButtonActionPerformed(evt);
            }
        });

        startAnalysisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/go.png"))); // NOI18N
        startAnalysisButton.setToolTipText(Bundle.CTL_StartAnalysisButton_ToolTip());
        startAnalysisButton.setFocusable(false);
        startAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAnalysisButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolBarPanelLayout = new javax.swing.GroupLayout(toolBarPanel);
        toolBarPanel.setLayout(toolBarPanelLayout);
        toolBarPanelLayout.setHorizontalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolBarPanelLayout.createSequentialGroup()
                .addComponent(analysisComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startAnalysisButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reformatRevisionButton))
        );
        toolBarPanelLayout.setVerticalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(startAnalysisButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(analysisComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE)
            .addComponent(reformatRevisionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        revisionEditorPane.setEditable(false);
        revisionEditorPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/html"));
        revisionEditorPane.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "CTL_Clear_analyses_Label")); // NOI18N
        revisionEditorPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                revisionEditorPaneMouseMoved(evt);
            }
        });
        revisionScrollPane.setViewportView(revisionEditorPane);

        revisionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        revisionLabel.setToolTipText(Bundle.CTL_RevisionLabel_ToolTip());

        revisionSlider.setMinimum(1);
        revisionSlider.setMinorTickSpacing(1);
        revisionSlider.setPaintTicks(true);
        revisionSlider.setSnapToTicks(true);
        revisionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                revisionSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(revisionSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revisionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
            .addComponent(revisionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(revisionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(revisionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revisionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void revisionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_revisionSliderStateChanged
        // TODO add your handling code here:
        if (revisionSlider.getValueIsAdjusting()) {
            Revision lastSelectedRevision = getSelectedRevision();
            lastSelectedRevisions.put(getSelectedAnalysis(), lastSelectedRevision);
            updateRevision(lastSelectedRevision);
        }
    }//GEN-LAST:event_revisionSliderStateChanged

    private void revisionEditorPaneMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_revisionEditorPaneMouseMoved
            final int index = revisionEditorPane.viewToModel(evt.getPoint());

            final boolean reformat = reformatRevisionButton.isSelected();
            final Analysis selectedAnalysis = getSelectedAnalysis();
            if (selectedAnalysis != null && selectedAnalysis.getTimeStampsCount() > 0) {
                for (int i = revisionSlider.getValue(); i > 0; i--) {
                    final Revision revision = selectedAnalysis.getRevision(i, reformat);
                    if (revision != null) {
                        for (Change o : revision.getChanges()) {
                            if (index >= o.getOffset() && index <= o.getOffset()+o.getLength() && o.getRevisionIndex() != -1) {
                                updateCallStack(revision.getStacktrace());
                                updateData(revision.getData());
                                return;
                            }
                        }
                    }
                }
            }
        final Revision selectedRevision = getSelectedRevision();
        
        if (selectedRevision != null) {
            updateCallStack(selectedRevision.getStacktrace());
            updateData(selectedRevision.getData());
        }
            
    }//GEN-LAST:event_revisionEditorPaneMouseMoved

    private void startAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAnalysisButtonActionPerformed
        // TODO add your handling code here:
        analysisComboBox.actionPerformed(evt);
        
        URL url_;
        
        /// Hack 
        if (analysisModel.getSourceUrl() == null) {
            String selectedUrl = getSelectedUrl();
            if (selectedUrl == null || selectedUrl.isEmpty()) {
                Analysis selectedAnalysis = getSelectedAnalysis();
                if (selectedAnalysis == null) {
                    selectedUrl = "http://www.netbeans.org/";
                } else {
                    selectedUrl = selectedAnalysis.getSourceUrl().toExternalForm();
                }
            }
        /// Hack 

            if (selectedUrl == null) {
                return;
            }
            
            url_ = null;
            try {
                url_ = new URL(selectedUrl);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            
        } else {
            url_ = analysisModel.getSourceUrl();
        }
                  
        final URL url = url_;
        final Analysis resolvedAnalysis = analysisModel.addAnalysis(url);
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = File.createTempFile("livehtml", "dummy");
                    FileObject fo = FileUtil.toFileObject(f);
                    getPrivateBrowserSupport().load(url, fo);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
    }//GEN-LAST:event_startAnalysisButtonActionPerformed

    private void reformatRevisionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reformatRevisionButtonActionPerformed
        updateAnalysis(getSelectedAnalysis());
    }//GEN-LAST:event_reformatRevisionButtonActionPerformed

    private void analysisComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_analysisComboBoxItemStateChanged
        updateAnalysis(getSelectedAnalysis());
    }//GEN-LAST:event_analysisComboBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox analysisComboBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToggleButton reformatRevisionButton;
    private javax.swing.JEditorPane revisionEditorPane;
    private javax.swing.JLabel revisionLabel;
    private javax.swing.JScrollPane revisionScrollPane;
    private javax.swing.JSlider revisionSlider;
    private javax.swing.JButton startAnalysisButton;
    private javax.swing.JPanel toolBarPanel;
    // End of variables declaration//GEN-END:variables

}
