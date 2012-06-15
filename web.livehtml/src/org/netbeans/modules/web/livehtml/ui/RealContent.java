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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.livehtml.Change;
import org.netbeans.modules.web.livehtml.Model;
import org.netbeans.modules.web.livehtml.Revision;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author david
 */
public class RealContent extends javax.swing.JPanel implements ChangeListener {

    private Model model;
    private Timer timer;
    private boolean beautify;
    private int currentChangeIndex = -1;
    private Revision change;
    
    /**
     * Creates new form RealContent
     */
    public RealContent(Model m, boolean beautify) {
        this.model = m;
        this.model.addChangeListener(this);
        this.beautify = beautify;
        timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateContent();
            }
        });
        timer.setRepeats(false);
        initComponents();
        ToolTipManager.sharedInstance().registerComponent(jEditorPane);
        jEditorPane.getDocument().putProperty(JEditorPane.class, this);
        jSlider.setEnabled(false);
        refresh();
        initEditorPane();
        jTabbedPane1.remove(jDataPanel);
    }
    
    static class MyEditorPane extends JEditorPane {

        private RealContent content;

        public MyEditorPane(RealContent content) {
            super();
            this.content = content;
        }
        
//        @Override
//        public String getToolTipText(MouseEvent event) {
//            System.err.print("x");
//            Point p = getMousePosition();
//            if (p == null) {
//                return super.getToolTipText(event);
//            }
//            int offset = viewToModel(p);
//            if (offset == -1) {
//                return super.getToolTipText(event);
//            }
//            Revision change = content.model.getChange(content.currentChangeIndex, content.beautify);
//            for (Change o : change.getChanges()) {
//                if (offset >= o.getOffset() && offset <= o.getOffset()+o.getLength() && o.getRevisionIndex() != -1) {
//                    Revision change2 = content.model.getChange(o.getRevisionIndex(), false);
//                    StringBuilder sb = convertCallStack(change2.getStacktrace());
//                    return "This change was introduce in revision no. "+o.getRevisionIndex()+" by\n\n"+sb.toString();
//                }
//            }
//            return super.getToolTipText(event);
//        }
        
        public void showToolTip(int index) {
            if (index == -1) {
                if (content.change != null) {
                    content.updateCallStack(content.change.getStacktrace());
                } else {
                    content.jStackTraceTextPane.setText("");
                }
            } else {
                Revision change2 = content.model.getChange(index-1, false);
                StringBuilder sb = convertCallStack(change2.getStacktrace());
                content.jStackTraceTextPane.setText("This change was introduce in revision no. "+
                        (index-1)+" by\n\n"+sb.toString());
            }
            
        }
    }
    
    private void initEditorPane() {
        jEditorPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/html"));
    }


    private void refresh() {
        if (model.getChangesCount() == 0) {
            jSlider.setEnabled(false);
            return;
        }
        jSlider.setEnabled(true);
        jSlider.setMinimum(0);
        // slider must have one extra position for "all changes" view
        jSlider.setMaximum((model.getChangesCount()-1));
        jChangesLabel.setText(Integer.toString(model.getChangesCount()));
    }
    
    private void updateContent() {
        int value = jSlider.getValue();
        if (value >= 0 && value < model.getChangesCount()) {
            showSingleRevision(value);
        } else {
            showAllChanges();
        }
    }
    
    private void showAllChanges() {
        jEditorPane.getDocument().putProperty(Change.class, null);
        jEditorPane.setText("all");
        jStackTraceTextPane.setText("");
    }
    
    private void showSingleRevision(int changeIndex) {
        if (currentChangeIndex == changeIndex) {
            return;
        }
        currentChangeIndex = changeIndex;
        change = model.getChange(changeIndex, beautify);
        jEditorPane.getDocument().putProperty(Change.class, change.getChanges());
        //jEditorPane.getDocument().putProperty(Origin.class, change.getOrigins());
        jEditorPane.setText(change.getContent());
        updateCallStack(change.getStacktrace());
        jDocumentLabel.setText(change.getChanges().isEmpty() ?
                "Document (only WHITESPACE changes):"
                : NbBundle.getMessage(RealContent.class, "RealContent.jDocumentLabel.text"));
        StringBuilder data = change.getData();
        if (data == null) {
            jDataTextPane.setText("");
            jTabbedPane1.remove(jDataPanel);
        } else {
            jDataTextPane.setText(data.toString());
            jDataTextPane.setCaretPosition(0);
            jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jDataPanel.TabConstraints.tabTitle"), jDataPanel); // NOI18N
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

        jSlider = new javax.swing.JSlider();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jStackTraceTextPane = new javax.swing.JTextPane();
        jDataPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jDataTextPane = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jDocumentLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane = new MyEditorPane(this);
        jChangesLabel = new javax.swing.JLabel();
        jStartButton = new javax.swing.JButton();
        jPrevButton = new javax.swing.JButton();
        jNextButton = new javax.swing.JButton();
        jEndButton = new javax.swing.JButton();

        jSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStateChanged(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.8);
        jSplitPane1.setContinuousLayout(true);

        jScrollPane2.setViewportView(jStackTraceTextPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jScrollPane3.setViewportView(jDataTextPane);

        javax.swing.GroupLayout jDataPanelLayout = new javax.swing.GroupLayout(jDataPanel);
        jDataPanel.setLayout(jDataPanelLayout);
        jDataPanelLayout.setHorizontalGroup(
            jDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
        );
        jDataPanelLayout.setVerticalGroup(
            jDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jDataPanel.TabConstraints.tabTitle"), jDataPanel); // NOI18N

        jSplitPane1.setBottomComponent(jTabbedPane1);

        org.openide.awt.Mnemonics.setLocalizedText(jDocumentLabel, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jDocumentLabel.text")); // NOI18N

        jEditorPane.setEditable(false);
        jScrollPane1.setViewportView(jEditorPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jDocumentLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jDocumentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jChangesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jChangesLabel, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jChangesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jStartButton, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jStartButton.text")); // NOI18N
        jStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jPrevButton, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jPrevButton.text")); // NOI18N
        jPrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPrevButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jNextButton, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jNextButton.text")); // NOI18N
        jNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNextButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jEndButton, org.openide.util.NbBundle.getMessage(RealContent.class, "RealContent.jEndButton.text")); // NOI18N
        jEndButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEndButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jStartButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPrevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jEndButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jChangesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jNextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jEndButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStartButton, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(jSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jEndButton, jNextButton, jPrevButton, jStartButton});

    }// </editor-fold>//GEN-END:initComponents

    private void jSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStateChanged
        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }//GEN-LAST:event_jSliderStateChanged

    private void jPrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPrevButtonActionPerformed
        if (jSlider.getValue() > 0) {
            jSlider.setValue(jSlider.getValue()-1);
        }
    }//GEN-LAST:event_jPrevButtonActionPerformed

    private void jNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNextButtonActionPerformed
        if (jSlider.getValue() < jSlider.getMaximum()) {
            jSlider.setValue(jSlider.getValue()+1);
        }
    }//GEN-LAST:event_jNextButtonActionPerformed

    private void jEndButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEndButtonActionPerformed
        jSlider.getModel().setValue(jSlider.getMaximum());
    }//GEN-LAST:event_jEndButtonActionPerformed

    private void jStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartButtonActionPerformed
        jSlider.getModel().setValue(0);
    }//GEN-LAST:event_jStartButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jChangesLabel;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JTextPane jDataTextPane;
    private javax.swing.JLabel jDocumentLabel;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JButton jEndButton;
    private javax.swing.JButton jNextButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jPrevButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSlider jSlider;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jStackTraceTextPane;
    private javax.swing.JButton jStartButton;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        if (model.getChangesCount() == 0) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    void setBeautify(boolean beautify) {
        this.beautify = beautify;
        currentChangeIndex = -1;
        updateContent();
    }

    private static StringBuilder convertCallStack(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (Object o : arr) {
            JSONObject js = (JSONObject)o;
            sb.append(js.get("function"));
            sb.append(" ");
            sb.append(js.get("lineNumber"));
            sb.append(":");
            sb.append(js.get("columnNumber"));
            sb.append(" at ");
            sb.append(js.get("script"));
            sb.append("\n");
        }
        return sb;
    }
    
    private void updateCallStack(JSONArray arr) {
        StringBuilder sb = convertCallStack(arr);
        jStackTraceTextPane.setText(sb.toString());
        jStackTraceTextPane.setCaretPosition(0);
    }

}
