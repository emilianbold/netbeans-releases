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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.browser.api.WebBrowserPane;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisListener;
import org.netbeans.modules.web.livehtml.AnalysisModel;
import org.netbeans.modules.web.livehtml.AnalysisModelListener;
import org.netbeans.modules.web.livehtml.AnalysisStorage;
import org.netbeans.modules.web.domdiff.Change;
import org.netbeans.modules.web.livehtml.ReformatSupport;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;
import org.netbeans.modules.web.livehtml.filter.RevisionFilterPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author petr-podzimek
 */
public class AnalysisPanel extends javax.swing.JPanel {
    
    private static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N
    
    private static final boolean SHOW_FILTER_BUTTON = true;
    private static final boolean SHOW_PREVIEW_BUTTON = true;
    
    private AnalysisModel analysisModel = new AnalysisModel();
    
    private ToolTipSupport toolTipSupport = null;
    
    private AnalysisModelListener analysisModelListener = new PrivateAnalysisModelListener();
    private AnalysisListener analysisListener = new PrivateAnalysisListener();
    
    private static WebBrowserPane previewBrowserSupport = null;
    protected Project projectContext;

    /**
     * Creates new form AnalysisPanel
     */
    public AnalysisPanel() {
        
        // JSlider GTK LnF quick fix.
        UIManager.put("Slider.paintValue", Boolean.FALSE);
        
        initComponents();
        analysisModel.addAnalysisModelListener(analysisModelListener);
        revisionEditorPane.getDocument().putProperty(JEditorPane.class, revisionEditorPane);
        

        setSourceUrl(analysisModel.getSourceUrl());
        updateAnalysis(null);
        updateRevisions(null);
        
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(revisionEditorPane);
        if (editorUI != null) {
            toolTipSupport = editorUI.getToolTipSupport();
            toolTipSupport.setEnabled(true);
        }

    }

    protected void hideComboBox() {
        analysisComboBox.setVisible(false);
    }
    
    protected JPanel getMainPanel() {
        return mainPanel;
    }

    protected JPanel getTooBarPanel() {
        return toolBarPanel;
    }

    protected final void setSourceUrl(URL sourceUrl) {
        setSourceUrl(sourceUrl, null);
    }
    
    protected final void setSourceUrl(URL sourceUrl, Project p) {
        analysisModel.setSourceUrl(sourceUrl);
        analysisComboBox.setEditable(sourceUrl == null);
        
        setAnalysises(analysisModel.getAnalyses());
        projectContext = p;
    }

    private void selectAnalysis(Analysis analysis) {
        if (analysis == null) {
            analysisComboBox.setSelectedItem(null);
            return;
        }
        final AnalysisItem analysisItem = getAnalysisItem(analysis);
        analysisComboBox.setSelectedItem(analysisItem);
    }
    
    private void updateAnalysis(Analysis analysis) {
        if (analysisModel == null) {
            return;
        }
        final List<Analysis> analyses = analysisModel.getAnalyses();
        if (analysis == null && !analyses.isEmpty()) {
            final int lastAnalysisIndex = analysisModel.getAnalyses().size() - 1;
            analysis = analyses.get(lastAnalysisIndex);
        }
        final Analysis selectedAnalysis = getSelectedAnalysis();
        if (selectedAnalysis != null) {
            selectedAnalysis.removeAnalysisListener(analysisListener);
        }
        
        if (analysis != null) {
            analysis.addAnalysisListener(analysisListener);
            updateRevisions(analysis);
        } else {
            updateRevisions(null);
        }
        
//        startAnalysisButton.setEnabled(analysis == null || analysis.getFinished() != null);
    }
    
    private void updateRevisions(Analysis analysis) {
        
        if (analysis == null || analysis.getRevisionsCount() <= 0) {
            revisionSlider.setEnabled(false);
            revisionSlider.setMaximum(1);
        } else {
            revisionSlider.setEnabled(analysis.getRevisionsCount() > 1);
            revisionSlider.setMaximum(analysis.getRevisionsCount());
        }
        
        selectRevision(getSelectedRevision());
    }
    
    private void selectRevision(Revision revision) {
        final AnalysisItem selectedAnalysisItem = getSelectedAnalysisItem();
        final Analysis selectedAnalysis = selectedAnalysisItem == null ? null : selectedAnalysisItem.resolveAnalysis();
        
        if (selectedAnalysis != null && revision != null) {
            int index = revision.getIndex();
            
            revisionLabel.setText(selectedAnalysisItem.getRevisionLabel(index));
            
            revisionSlider.setEnabled(selectedAnalysis.getRevisionsCount() > 1);
            revisionSlider.setMaximum(selectedAnalysis.getRevisionsCount());
            revisionSlider.setValue(index);
            
            final Rectangle visibleRect = revisionEditorPane.getVisibleRect();
            
            revisionEditorPane.setText(reformatRevisionButton.isSelected() ? revision.getReformattedContent() : revision.getContent());
            revisionEditorPane.setVisible(true);
            revisionEditorPane.getDocument().putProperty(Change.class, reformatRevisionButton.isSelected() ? revision.getReformattedChanges() : revision.getChanges());
            
            revisionEditorPane.scrollRectToVisible(visibleRect);
        } else {
            revisionLabel.setText("- / -");
            
            revisionEditorPane.setText(null);
            revisionEditorPane.setVisible(false);
            revisionEditorPane.getDocument().putProperty(Change.class, null);
            
            revisionSlider.setEnabled(false);
            revisionSlider.setMaximum(1);
            revisionSlider.setValue(1);
        }
        
    }

    private synchronized void setAnalysises(List<Analysis> analysises) {
        final Analysis selectedAnalysis = getSelectedAnalysis();
        analysisComboBox.removeAllItems();
        
        if (analysisComboBox.isEditable()) {
            analysisComboBox.addItem(null);
        }
        if (analysises != null) {
            List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
            for (Analysis analysis : analysises) {
                if (!(analysis instanceof FilteredAnalysis)) {
                    final AnalysisItem analysisItem = new AnalysisItem(analysis);
                    analysisItem.setFilteredAnalysis(analysisModel.getFilteredAnalysis(analysis));
                    
                    analysisItems.add(analysisItem);
                }
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
            return selectedAnalysisItem.resolveAnalysis();
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
            return analysisItem.resolveAnalysis().getSourceUrl().toExternalForm();
        }
        return null;
    }
    
    private Revision getSelectedRevision() {
        Analysis selectedAnalysis = getSelectedAnalysis();
        if (selectedAnalysis == null) {
            return null;
        }
        if (revisionSlider.getValue() >= 0) {
            return selectedAnalysis.getRevision(revisionSlider.getValue());
        }
        return null;
    }
    
    private AnalysisItem getAnalysisItem(Analysis analysis) {
        if (analysis == null) {
            return null;
        }
        for (int i = 0; i < analysisComboBox.getItemCount(); i++) {
            final Object item = analysisComboBox.getItemAt(i);
            if (item instanceof AnalysisItem) {
                AnalysisItem analysisItem = (AnalysisItem) item;
                if (analysis.equals(analysisItem.resolveAnalysis())) {
                    return analysisItem;
                }
            }
        }
        return null;
    }

    //TODO: Finish correct BrowserSupport.
    private WebBrowserPane bs;
    private WebBrowserPane getPrivateBrowserSupport() {
        // there seems to be some problem in Chrome's WebKit Debugging protocol:
        // if the same browser tab is reload with the same or different URL then
        // frequnetly Chrome crashs - an internal dark blue error page is displayed
        // in Chrome saying that something went wrong. For now let's not reuse the same
        // tab but always open a new one. If no better solution is found then perhaps
        // we could use Chrome's API to close old tab before opening a new one.
        if (/*bs == null*/ true) {
            WebBrowser wb = findChrome();
            WebBrowserFeatures features = new WebBrowserFeatures(true, true, false, false, false, true);
            bs = wb.createNewBrowserPane(features);
            bs.setProjectContext(Lookups.singleton("dummy"));
        }
        return bs;
    }
    
    private void previewRevision(Revision revision) {
        if (revision == null) {
            return;
        }
        
        final StringBuilder documentText = revision.getPreviewContent();
        
        if (documentText == null) {
            return;
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = File.createTempFile("livehtml", "dummy");
                    FileWriter fileWriter = new FileWriter(f);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    
                    bufferedWriter.write(documentText.toString());
                    bufferedWriter.close();
                    
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    
                    //TODO: This part of code must be changed - now it will open Chrome Tab for every "file". 
                    getPreviewBrowserSupport().showURL(fo.toURL());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private synchronized WebBrowserPane getPreviewBrowserSupport() {
        if (previewBrowserSupport == null) {
            WebBrowser wb = findChrome();
            WebBrowserFeatures features = new WebBrowserFeatures(false, false, false, false, false, false);
            previewBrowserSupport = wb.createNewBrowserPane(features);
            previewBrowserSupport.setProjectContext(Lookups.singleton("dummy"));
        }
        return previewBrowserSupport;
    }

    private static WebBrowser findChrome() {
        for (WebBrowser wb : WebBrowsers.getInstance().getAll(false, false, false)) {
            if (wb.hasNetBeansIntegration() && !wb.isEmbedded()) {
                return wb;
            }
        }
        return null;
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
        filterButton = new javax.swing.JButton();
        previewRevisionToggleButton = new javax.swing.JToggleButton();
        mainPanel = new javax.swing.JPanel();
        revisionScrollPane = new javax.swing.JScrollPane();
        revisionEditorPane = new MyEditorPane(this);
        revisionLabel = new javax.swing.JLabel();
        revisionSlider = new javax.swing.JSlider();
        jPreviousButton = new javax.swing.JButton();
        jNextButton = new javax.swing.JButton();

        analysisComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.analysisComboBox.toolTip")); // NOI18N
        analysisComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                analysisComboBoxItemStateChanged(evt);
            }
        });

        reformatRevisionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/pretty.png"))); // NOI18N
        reformatRevisionButton.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.reformatRevisionsButton.toolTipText")); // NOI18N
        reformatRevisionButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        reformatRevisionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reformatRevisionButtonActionPerformed(evt);
            }
        });

        startAnalysisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/go.png"))); // NOI18N
        startAnalysisButton.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.startAnalysisButton.toolTipText")); // NOI18N
        startAnalysisButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        startAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAnalysisButtonActionPerformed(evt);
            }
        });

        filterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/filter.png"))); // NOI18N
        filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.filterButton.toolTipText")); // NOI18N
        filterButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        previewRevisionToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/livehtml/resources/preview.png"))); // NOI18N
        previewRevisionToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.previewRevisionButton.toolTipText")); // NOI18N
        previewRevisionToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        previewRevisionToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewRevisionToggleButtonActionPerformed(evt);
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
                .addComponent(reformatRevisionButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewRevisionToggleButton))
        );
        toolBarPanelLayout.setVerticalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(analysisComboBox)
            .addComponent(startAnalysisButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(reformatRevisionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(previewRevisionToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        filterButton.setVisible(SHOW_FILTER_BUTTON);
        previewRevisionToggleButton.setVisible(SHOW_PREVIEW_BUTTON);

        revisionEditorPane.setEditable(false);
        revisionEditorPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/html"));
        revisionScrollPane.setViewportView(revisionEditorPane);

        revisionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        revisionSlider.setMinimum(1);
        revisionSlider.setMinorTickSpacing(1);
        revisionSlider.setPaintTicks(true);
        revisionSlider.setSnapToTicks(true);
        revisionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                revisionSliderStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jPreviousButton, org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.jPreviousButton.text")); // NOI18N
        jPreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPreviousButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jNextButton, org.openide.util.NbBundle.getMessage(AnalysisPanel.class, "AnalysisPanel.jNextButton.text")); // NOI18N
        jNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jPreviousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revisionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revisionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(revisionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(revisionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(revisionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPreviousButton)
                    .addComponent(jNextButton))
                .addGap(7, 7, 7)
                .addComponent(revisionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void revisionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_revisionSliderStateChanged
            Revision lastSelectedRevision = getSelectedRevision();
            if (lastSelectedRevision != null) {
                selectRevision(lastSelectedRevision);
                if (previewRevisionToggleButton.isSelected()) {
                    previewRevision(lastSelectedRevision);
                }
            }
    }//GEN-LAST:event_revisionSliderStateChanged

    private void startAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAnalysisButtonActionPerformed
        final AnalysisItem selectedAnalysisItem = getSelectedAnalysisItem();
        Analysis selectedAnalysis = null;
        if (selectedAnalysisItem != null) {
            selectedAnalysisItem.setFilteredAnalysis(null);
            selectedAnalysis = selectedAnalysisItem.getAnalysis();
        }
        
        if (selectedAnalysis == null) {
            analysisComboBox.actionPerformed(evt);
        } else {
            selectedAnalysis.makeFinished();
        }
        
        URL url_;
        
        /// Hack 
        if (analysisModel.getSourceUrl() == null) {
            String selectedUrl = getSelectedUrl();
            if (selectedUrl == null || selectedUrl.isEmpty()) {
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
        final Analysis resolvedAnalysis = analysisModel.resolveAnalysis(url);
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = File.createTempFile("livehtml", "dummy");
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    getPrivateBrowserSupport().showURL(url);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
    }//GEN-LAST:event_startAnalysisButtonActionPerformed

    private void reformatRevisionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reformatRevisionButtonActionPerformed
        final Revision selectedRevision = getSelectedRevision();
        selectRevision(selectedRevision);
        if (previewRevisionToggleButton.isSelected()) {
            previewRevision(selectedRevision);
        }
    }//GEN-LAST:event_reformatRevisionButtonActionPerformed

    private void analysisComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_analysisComboBoxItemStateChanged
        updateAnalysis(getSelectedAnalysis());
    }//GEN-LAST:event_analysisComboBoxItemStateChanged

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        final AnalysisItem selectedAnalysisItem = getSelectedAnalysisItem();
        
        if (selectedAnalysisItem == null) {
            return;
        }
        
        Analysis selectedAnalysis = selectedAnalysisItem.getAnalysis();
        
        if (selectedAnalysis == null) {
            return;
        }
        FilteredAnalysis filteredAnalysis = selectedAnalysisItem.getFilteredAnalysis();
        
        RevisionFilterPanel revisionFilterPanel = new RevisionFilterPanel();
        revisionFilterPanel.setAnalysis(selectedAnalysis);
        revisionFilterPanel.setFilteredAnalysis(filteredAnalysis);
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(revisionFilterPanel, "Revision filter");
        
        Object result = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (result != DialogDescriptor.OK_OPTION) {
            return;
        }
        
        filteredAnalysis = revisionFilterPanel.createFilteredAnalysis();
        
        if (filteredAnalysis == null) {
            selectedAnalysisItem.setFilteredAnalysis(null);
            updateAnalysis(getSelectedAnalysis());
        } else {
            AnalysisStorage.getInstance().addFilteredAnalysis(filteredAnalysis);
        }
        
    }//GEN-LAST:event_filterButtonActionPerformed

    private void previewRevisionToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewRevisionToggleButtonActionPerformed
        if (previewRevisionToggleButton.isSelected()) {
            previewRevision(getSelectedRevision());
        } else {
            previewRevision(null);
        }
        
    }//GEN-LAST:event_previewRevisionToggleButtonActionPerformed

    private void jNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNextButtonActionPerformed
        if (revisionSlider.isEnabled() && revisionSlider.getModel().getValue() < revisionSlider.getModel().getMaximum()) {
            revisionSlider.getModel().setValue(revisionSlider.getModel().getValue()+1);
        }
    }//GEN-LAST:event_jNextButtonActionPerformed

    private void jPreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPreviousButtonActionPerformed
        if (revisionSlider.isEnabled() && revisionSlider.getModel().getValue() > revisionSlider.getModel().getMinimum()) {
            revisionSlider.getModel().setValue(revisionSlider.getModel().getValue()-1);
        }
    }//GEN-LAST:event_jPreviousButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox analysisComboBox;
    private javax.swing.JButton filterButton;
    private javax.swing.JButton jNextButton;
    private javax.swing.JButton jPreviousButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToggleButton previewRevisionToggleButton;
    private javax.swing.JToggleButton reformatRevisionButton;
    private javax.swing.JEditorPane revisionEditorPane;
    private javax.swing.JLabel revisionLabel;
    private javax.swing.JScrollPane revisionScrollPane;
    private javax.swing.JSlider revisionSlider;
    private javax.swing.JButton startAnalysisButton;
    private javax.swing.JPanel toolBarPanel;
    // End of variables declaration//GEN-END:variables

    private class PrivateAnalysisModelListener implements AnalysisModelListener {
        
        @Override
        public void analysisAdded(Analysis analysis) {
            setSourceUrl(analysisModel.getSourceUrl());
            updateAnalysis(analysis);
            selectAnalysis(analysis);
        }

        @Override
        public void analysisRemoved(Analysis analysis) {
            setSourceUrl(analysisModel.getSourceUrl());
            updateAnalysis(analysis);
            selectAnalysis(analysis);
        }

    }
    
    private class PrivateAnalysisListener implements AnalysisListener {
        
        @Override
        public void revisionAdded(final Analysis analysis, final String timeStamp) {
            final AnalysisItem selectedAnalysisItem = getSelectedAnalysisItem();
            if (selectedAnalysisItem == null) {
                return;
            }
            final Analysis selectedAnalysis = selectedAnalysisItem.getAnalysis();
            final FilteredAnalysis selectedFilteredAnalysis = selectedAnalysisItem.getFilteredAnalysis();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (analysis == selectedAnalysis) {
                        updateRevisions(selectedAnalysis);
                        return;
                    }
                    if (analysis == selectedFilteredAnalysis) {
                        updateRevisions(selectedFilteredAnalysis);
                    }
                }
            });
        }

    }

    static class MyEditorPane extends JEditorPane {

        private AnalysisPanel content;
        private int lastIndex = -3;

        public MyEditorPane(AnalysisPanel content) {
            super();
            this.content = content;
        }
        
        private JComponent createToolTip (int revisionIndex) {
            RevisionToolTipPanel panel = new RevisionToolTipPanel(content.projectContext);
            panel.setBorder(BorderFactory.createLineBorder(panel.getForeground()));
            Revision revision = content.getSelectedAnalysis().getRevision(revisionIndex);
            StringBuilder toolTipTitle = new StringBuilder("Rev #");
            toolTipTitle.append(revision.getIndex());
            panel.setRevision(revision, toolTipTitle.toString(), content.reformatRevisionButton.isSelected());
            return panel;
        }
        
        public void showToolTip(int index) {
            if (index == -1) {
                content.toolTipSupport.setToolTipVisible(false);
                lastIndex = -3;
            } else {
                if (lastIndex == -3 || lastIndex != index) {
                    lastIndex = index;
                    content.toolTipSupport.setToolTip(createToolTip(index),
                                PopupManager.ViewPortBounds, 
                                PopupManager.Largest, 
                                0, 
                                0, 
                                ToolTipSupport.FLAGS_LIGHTWEIGHT_TOOLTIP);
                    content.toolTipSupport.setToolTipVisible(true);
                }
            }
        }

        public void showToolTip() {
            Revision r = content.getSelectedRevision();
            if (r != null) {
                showToolTip(r.getIndex());
            }
        }
    }
    
}
