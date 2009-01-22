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
package org.netbeans.modules.cnd.navigation.macroview;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.model.tasks.CaretAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Alexander Simon
 */
public class MacroExpansionPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {

    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N
    private transient ExplorerManager explorerManager = new ExplorerManager();

    /** Creates new form MacroExpansionPanel */
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
        jCodeExpansionEditorPane.enableInputMethods(false);
        doc.putProperty(JEditorPane.class, jCodeExpansionEditorPane);

        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 != null) {
            FileObject file2 = CsmUtilities.getFileObject(doc2);
            if (file2 != null) {
                JEditorPane ep = getEditor(doc2);
                int doc2CarretPosition = ep.getCaretPosition();
                int docCarretPosition = getDocumentOffset(doc, getFileOffset(doc2, doc2CarretPosition));
                if (docCarretPosition >= 0 && docCarretPosition < doc.getLength()) {
                    jCodeExpansionEditorPane.setCaretPosition(docCarretPosition);
                }
            }
        }
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
     * @param local - is scole local
     */
    public void setLocalContext(boolean local) {
        localContext.setSelected(local);
        fileContext.setSelected(!local);
    }

    private void update() {
        Document doc = (Document) jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        Document mainDoc = (Document) doc.getProperty(Document.class);
        if (mainDoc == null) {
            return;
        }
        JEditorPane ep = getEditor(doc);
        if (ep == null) {
            return;
        }
        int offset = getDocumentOffset(mainDoc, getFileOffset(doc, ep.getCaretPosition()));
        CsmMacroExpansion.showMacroExpansionView(mainDoc, offset);
    }

    private int getFileOffset(Document doc, int documentOffset) {
        return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
    }

    private int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    private JEditorPane getEditor(Document doc) {
        FileObject file = CsmUtilities.getFileObject(doc);
        if (file != null) {
            DataObject dobj = null;
            try {
                dobj = DataObject.find(file);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(CaretAwareCsmFileTaskFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                JEditorPane jEditorPanes[] = ec.getOpenedPanes();
                if (jEditorPanes != null && jEditorPanes.length > 0) {
                    return jEditorPanes[0];
                }
            }
        }
        Object jEditorPane = doc.getProperty(JEditorPane.class);
        if (jEditorPane != null) {
            return (JEditorPane) jEditorPane;
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        refresh = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        localContext = new javax.swing.JToggleButton();
        fileContext = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        prevMacro = new javax.swing.JButton();
        nextMacro = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jCodeExpansionPane = new javax.swing.JScrollPane();
        jCodeExpansionEditorPane = new javax.swing.JEditorPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMacroExpansionEditorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(1);
        jToolBar1.setRollover(true);

        refresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/refresh.png"))); // NOI18N
        refresh.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.refresh.toolTipText")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        jToolBar1.add(refresh);
        refresh.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.refresh.AccessibleContext.accessibleDescription")); // NOI18N

        jToolBar1.add(jSeparator1);

        localContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/declscope.png"))); // NOI18N
        localContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.localContext.toolTipText")); // NOI18N
        localContext.setFocusable(false);
        localContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
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
        fileContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fileContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileContextActionPerformed(evt);
            }
        });
        jToolBar1.add(fileContext);
        fileContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.fileContext.AccessibleContext.accessibleDescription")); // NOI18N

        jToolBar1.add(jSeparator2);

        prevMacro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/prevmacro.png"))); // NOI18N
        prevMacro.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.prevMacro.toolTipText")); // NOI18N
        prevMacro.setFocusable(false);
        prevMacro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
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
        nextMacro.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextMacroActionPerformed(evt);
            }
        });
        jToolBar1.add(nextMacro);
        nextMacro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.nextMacro.AccessibleContext.accessibleDescription")); // NOI18N

        add(jToolBar1, java.awt.BorderLayout.LINE_START);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.9);
        jSplitPane1.setFocusable(false);
        jSplitPane1.setOneTouchExpandable(true);

        jCodeExpansionPane.setBorder(null);

        jCodeExpansionEditorPane.setBorder(null);
        jCodeExpansionPane.setViewportView(jCodeExpansionEditorPane);

        jSplitPane1.setLeftComponent(jCodeExpansionPane);

        jScrollPane1.setBorder(null);

        jMacroExpansionEditorPane.setBorder(null);
        jMacroExpansionEditorPane.setEditable(false);
        jScrollPane1.setViewportView(jMacroExpansionEditorPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void fileContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileContextActionPerformed
        fileContext.setSelected(true);
        localContext.setSelected(false);
        update();
}//GEN-LAST:event_fileContextActionPerformed

    private void localContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localContextActionPerformed
        localContext.setSelected(true);
        fileContext.setSelected(false);
        update();
}//GEN-LAST:event_localContextActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        update();
}//GEN-LAST:event_refreshActionPerformed

    private void prevMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevMacroActionPerformed
        Document doc = (Document) jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getPrevMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_prevMacroActionPerformed

    private void nextMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextMacroActionPerformed
        Document doc = (Document) jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getNextMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_nextMacroActionPerformed

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton localContext;
    private javax.swing.JButton nextMacro;
    private javax.swing.JButton prevMacro;
    private javax.swing.JButton refresh;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx("MacroExpansionView"); // NOI18N
    }
}
