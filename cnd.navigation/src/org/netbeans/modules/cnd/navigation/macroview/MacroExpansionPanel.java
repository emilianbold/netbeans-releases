/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.navigation.macroview;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Macro Expansion panel.
 *
 * @author Nick Krasilnikov
 */
public class MacroExpansionPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {

    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N
    private transient ExplorerManager explorerManager = new ExplorerManager();

    /** Creates new form MacroExpansionPanel. */
    public MacroExpansionPanel(boolean isView) {
        initComponents();
        setName(NbBundle.getMessage(getClass(), "CTL_MacroExpansionTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_MacroExpansionTopComponent")); // NOI18N
    }

    /**
     * Initializes document of expanded macro pane.
     *
     * @param doc - document
     */
    public void setMacroExpansionDocument(Document doc) {
        Object mimeTypeObj = doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
        String mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
        if (mimeTypeObj != null) {
            mimeType = (String) mimeTypeObj;
        }
        jMacroExpansionEditorPane.setContentType(mimeType);
        jMacroExpansionEditorPane.setDocument(doc);
    }

    /**
     * Initializes document of expanded context pane.
     *
     * @param doc - document
     */
    public void setContextExpansionDocument(Document doc) {
        Object mimeTypeObj = doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
        String mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
        if (mimeTypeObj != null) {
            mimeType = (String) mimeTypeObj;
        }
        jCodeExpansionEditorPane.setContentType(mimeType);
        jCodeExpansionEditorPane.setDocument(doc);
        doc.putProperty(JEditorPane.class, jCodeExpansionEditorPane);
    }

    /**
     * Returns position of panes divider.
     *
     * @return position
     */
    public int getDividerLocation() {
        return jSplitPane1.getDividerLocation();
    }

    /**
     * Sets position of panes divider.
     *
     * @param dividerLocation - position
     */
    public void setDividerLocation(int dividerLocation) {
        jSplitPane1.setDividerLocation(dividerLocation);
    }

    /**
     * Indicates scope for macro expansion (local or whole file).
     *
     * @return is macro expansion local
     */
    public boolean isLocalContext() {
        return localContext.isSelected();
    }

    /**
     * Sets scope for macro expansion (local or whole file).
     *
     * @param local - is scope local
     */
    public void setLocalContext(boolean local) {
        localContext.setSelected(local);
        fileContext.setSelected(!local);
    }

    /**
     * Indicates is caret synchronization enabled or not.
     *
     * @return is caret synchronization enabled
     */
    public boolean isSyncCaret() {
        return syncCaret.isSelected();
    }

    /**
     * Sets caret synchronization.
     *
     * @param sync - is caret synchronization enabled
     */
    public void setSyncCaret(boolean sync) {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CARET, sync);
        syncCaret.setSelected(sync);
    }

    /**
     * Indicates is context synchronization enabled or not.
     *
     * @return is context synchronization enabled
     */
    public boolean isSyncContext() {
        return syncContext.isSelected();
    }

    /**
     * Sets context synchronization.
     *
     * @param sync - is context synchronization enabled
     */
    public void setSyncContext(boolean sync) {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CONTEXT, sync);
        syncContext.setSelected(sync);
    }

    /**
     * Sets text in status bar.
     * 
     * @param s - text
     */
    public void setStatusBarText(String s) {
        jStatusBar.setText(s);
    }

    /**
     * Updates cursor position.
     */
    public void updateCaretPosition() {
        jCodeExpansionEditorPane.setCaretPosition(getCursorPositionFromMainDocument());
    }

    private int getCursorPositionFromMainDocument() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return 0;
        }
        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 == null) {
            return 0;
        }
        int docCarretPosition = MacroExpansionViewUtils.getDocumentOffset(doc,
                MacroExpansionViewUtils.getFileOffset(doc2, getMainDocumentCursorPosition()));
        if (docCarretPosition >= 0 && docCarretPosition < doc.getLength()) {
            return docCarretPosition;
        }
        return 0;
    }

    private int getMainDocumentCursorPosition() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return 0;
        }
        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 != null) {
            FileObject file2 = CsmUtilities.getFileObject(doc2);
            if (file2 != null) {
                JEditorPane ep = MacroExpansionViewUtils.getEditor(doc2);
                if(ep != null) {
                    int doc2CarretPosition = ep.getCaretPosition();
                    return doc2CarretPosition;
                }
            }
        }
        return 0;
    }

    private void update() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        Document mainDoc = (Document) doc.getProperty(Document.class);
        if (mainDoc == null) {
            return;
        }
        JEditorPane ep = MacroExpansionViewUtils.getEditor(doc);
        if (ep == null) {
            return;
        }
        int offset = MacroExpansionViewUtils.getDocumentOffset(mainDoc, MacroExpansionViewUtils.getFileOffset(doc, ep.getCaretPosition()));
        CsmMacroExpansion.showMacroExpansionView(mainDoc, offset);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMacroExpansionEditorPane = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jCodeExpansionPane = new javax.swing.JScrollPane();
        jCodeExpansionEditorPane = new javax.swing.JEditorPane();
        jStatusBar = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        refresh = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        localContext = new javax.swing.JToggleButton();
        fileContext = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        syncCaret = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        syncContext = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        prevMacro = new javax.swing.JButton();
        nextMacro = new javax.swing.JButton();

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setFocusable(false);
        jSplitPane1.setOneTouchExpandable(true);

        jScrollPane1.setBorder(null);

        jMacroExpansionEditorPane.setBorder(null);
        jMacroExpansionEditorPane.setEditable(false);
        jScrollPane1.setViewportView(jMacroExpansionEditorPane);

        setLayout(new java.awt.BorderLayout());

        jPanel1.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jCodeExpansionPane.setBorder(null);

        jCodeExpansionEditorPane.setBorder(null);
        jCodeExpansionPane.setViewportView(jCodeExpansionEditorPane);

        jPanel1.add(jCodeExpansionPane, java.awt.BorderLayout.CENTER);

        jStatusBar.setText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.jStatusBar.text")); // NOI18N
        jPanel1.add(jStatusBar, java.awt.BorderLayout.PAGE_END);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(1);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(28, 240));
        jToolBar1.setPreferredSize(new java.awt.Dimension(28, 240));

        refresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/refresh.png"))); // NOI18N
        refresh.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.refresh.toolTipText")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setMaximumSize(new java.awt.Dimension(24, 24));
        refresh.setMinimumSize(new java.awt.Dimension(24, 24));
        refresh.setPreferredSize(new java.awt.Dimension(24, 24));
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        jToolBar1.add(refresh);
        refresh.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.refresh.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator1.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator1);

        localContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/declscope.png"))); // NOI18N
        localContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.localContext.toolTipText")); // NOI18N
        localContext.setFocusable(false);
        localContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        localContext.setMaximumSize(new java.awt.Dimension(24, 24));
        localContext.setMinimumSize(new java.awt.Dimension(24, 24));
        localContext.setPreferredSize(new java.awt.Dimension(24, 24));
        localContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        localContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localContextActionPerformed(evt);
            }
        });
        jToolBar1.add(localContext);
        localContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.localContext.AccessibleContext.accessibleDescription")); // NOI18N

        fileContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/filescope.png"))); // NOI18N
        fileContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.fileContext.toolTipText")); // NOI18N
        fileContext.setFocusable(false);
        fileContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fileContext.setMaximumSize(new java.awt.Dimension(24, 24));
        fileContext.setMinimumSize(new java.awt.Dimension(24, 24));
        fileContext.setPreferredSize(new java.awt.Dimension(24, 24));
        fileContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fileContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileContextActionPerformed(evt);
            }
        });
        jToolBar1.add(fileContext);
        fileContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.fileContext.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator2.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator2);

        syncCaret.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/synccaret.png"))); // NOI18N
        syncCaret.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.syncCaret.toolTipText")); // NOI18N
        syncCaret.setFocusable(false);
        syncCaret.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        syncCaret.setMaximumSize(new java.awt.Dimension(24, 24));
        syncCaret.setMinimumSize(new java.awt.Dimension(24, 24));
        syncCaret.setPreferredSize(new java.awt.Dimension(24, 24));
        syncCaret.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        syncCaret.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncCaretActionPerformed(evt);
            }
        });
        jToolBar1.add(syncCaret);
        syncCaret.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.fileContext1.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator3.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator3);

        syncContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/synccontext.png"))); // NOI18N
        syncContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.syncContext.toolTipText")); // NOI18N
        syncContext.setFocusable(false);
        syncContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        syncContext.setMaximumSize(new java.awt.Dimension(24, 24));
        syncContext.setMinimumSize(new java.awt.Dimension(24, 24));
        syncContext.setPreferredSize(new java.awt.Dimension(24, 24));
        syncContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        syncContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncContextActionPerformed(evt);
            }
        });
        jToolBar1.add(syncContext);

        jSeparator4.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator4);

        prevMacro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/prevmacro.png"))); // NOI18N
        prevMacro.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.prevMacro.toolTipText")); // NOI18N
        prevMacro.setFocusable(false);
        prevMacro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevMacro.setMaximumSize(new java.awt.Dimension(24, 24));
        prevMacro.setMinimumSize(new java.awt.Dimension(24, 24));
        prevMacro.setPreferredSize(new java.awt.Dimension(24, 24));
        prevMacro.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        prevMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevMacroActionPerformed(evt);
            }
        });
        jToolBar1.add(prevMacro);
        prevMacro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.prevMacro.AccessibleContext.accessibleDescription")); // NOI18N

        nextMacro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/nextmacro.png"))); // NOI18N
        nextMacro.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.nextMacro.toolTipText")); // NOI18N
        nextMacro.setFocusable(false);
        nextMacro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextMacro.setMaximumSize(new java.awt.Dimension(24, 24));
        nextMacro.setMinimumSize(new java.awt.Dimension(24, 24));
        nextMacro.setPreferredSize(new java.awt.Dimension(24, 24));
        nextMacro.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextMacroActionPerformed(evt);
            }
        });
        jToolBar1.add(nextMacro);
        nextMacro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.nextMacro.AccessibleContext.accessibleDescription")); // NOI18N

        add(jToolBar1, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void nextMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextMacroActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getNextMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_nextMacroActionPerformed

    private void prevMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevMacroActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getPrevMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_prevMacroActionPerformed

    private void syncContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncContextActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CONTEXT, isSyncContext());
        MacroExpansionViewUtils.updateView(getMainDocumentCursorPosition());
}//GEN-LAST:event_syncContextActionPerformed

    private void syncCaretActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncCaretActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        doc.putProperty(CsmMacroExpansion.MACRO_EXPANSION_SYNC_CARET, isSyncCaret());
        updateCaretPosition();
}//GEN-LAST:event_syncCaretActionPerformed

    private void fileContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileContextActionPerformed
        fileContext.setSelected(true);
        localContext.setSelected(false);
        MacroExpansionTopComponent.setLocalContext(false);
        update();
}//GEN-LAST:event_fileContextActionPerformed

    private void localContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localContextActionPerformed
        localContext.setSelected(true);
        fileContext.setSelected(false);
        MacroExpansionTopComponent.setLocalContext(true);
        update();
}//GEN-LAST:event_localContextActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        update();
}//GEN-LAST:event_refreshActionPerformed

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return jCodeExpansionPane.requestFocusInWindow();
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton fileContext;
    private javax.swing.JEditorPane jCodeExpansionEditorPane;
    private javax.swing.JScrollPane jCodeExpansionPane;
    private javax.swing.JEditorPane jMacroExpansionEditorPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel jStatusBar;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton localContext;
    private javax.swing.JButton nextMacro;
    private javax.swing.JButton prevMacro;
    private javax.swing.JButton refresh;
    private javax.swing.JToggleButton syncCaret;
    private javax.swing.JToggleButton syncContext;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx("MacroExpansionView"); // NOI18N
    }
}
