/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.cnd.diagnostics.clank.ui//ClankDiagnosticsDetails//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ClankDiagnosticsDetailsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.cnd.diagnostics.clank.ui.ClankDiagnosticsDetailsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ClankDiagnosticsDetailsAction",
        preferredID = "ClankDiagnosticsDetailsTopComponent"
)
@Messages({
    "CTL_ClankDiagnosticsDetailsAction=ClankDiagnosticsDetails",
    "CTL_ClankDiagnosticsDetailsTopComponent=ClankDiagnosticsDetails Window",
    "HINT_ClankDiagnosticsDetailsTopComponent=This is a ClankDiagnosticsDetails window"
})
public final class ClankDiagnosticsDetailsTopComponent extends TopComponent implements ExplorerManager.Provider {

    static final String PREFERRED_ID = "ClankDiagnosticsDetailsTopComponent";
    private final ExplorerManager manager = new ExplorerManager();
    private BeanTreeView btv;

    public ClankDiagnosticsDetailsTopComponent() {
        initComponents();
        setName(Bundle.CTL_ClankDiagnosticsDetailsTopComponent());
        setToolTipText(Bundle.HINT_ClankDiagnosticsDetailsTopComponent());
        btv = new BeanTreeView();

        errorsPanel.setLayout(new BorderLayout());
        errorsPanel.add(btv, BorderLayout.CENTER);
        btv.setRootVisible(false);
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = manager.getSelectedNodes();

                if (selectedNodes.length == 1 && selectedNodes[0] instanceof ClankDiagnosticInfoNode) {
                    try {
                        //show details
                        ClankDiagnosticInfoNode node = (ClankDiagnosticInfoNode) selectedNodes[0];
                        //CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, HEIGHT)
                        CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(node.error);
                        FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                        FileObject fo = CndFileUtils.toFileObject(fSystem, node.note.getSourceFileName());
                        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, true, true);
                        int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getStartOffset());
                        int[] endlineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getEndOffset());
                        CharSequence text = csmErrorFile.getText(node.note.getStartOffset(), node.note.getEndOffset());
                        editorTextArea.setText(text.toString());
                    } catch (FileStateInvalidException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    void setData(ClankCsmErrorInfo info) {
        manager.setRootContext(new AbstractNode(new ClankDiagnosticChildren(info)));
        btv.revalidate();
    }

    public static synchronized ClankDiagnosticsDetailsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof ClankDiagnosticsDetailsTopComponent) {
            return (ClankDiagnosticsDetailsTopComponent) win;
        }
        if (win == null) {
            Logger.getLogger(ClankDiagnosticsDetailsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
        } else {
            Logger.getLogger(ClankDiagnosticsDetailsTopComponent.class.getName()).warning(
                    "There seem to be multiple components with the '" + PREFERRED_ID
                    + "' ID. That is a potential source of errors and unexpected behavior.");
        }

        ClankDiagnosticsDetailsTopComponent result = new ClankDiagnosticsDetailsTopComponent();
        Mode outputMode = WindowManager.getDefault().findMode("output");

        if (outputMode != null) {
            outputMode.dockInto(result);
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        errorsPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        editorTextArea = new javax.swing.JTextArea();

        jSplitPane1.setDividerLocation(300);

        javax.swing.GroupLayout errorsPanelLayout = new javax.swing.GroupLayout(errorsPanel);
        errorsPanel.setLayout(errorsPanelLayout);
        errorsPanelLayout.setHorizontalGroup(
            errorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
        );
        errorsPanelLayout.setVerticalGroup(
            errorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(errorsPanel);

        descriptionPanel.setLayout(new java.awt.BorderLayout());

        editorTextArea.setEditable(false);
        editorTextArea.setColumns(20);
        editorTextArea.setRows(5);
        jScrollPane1.setViewportView(editorTextArea);

        descriptionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(descriptionPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea editorTextArea;
    private javax.swing.JPanel errorsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
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

    private class ClankDiagnosticChildren extends Children.Keys<ClankDiagnosticInfo> {

        private final ClankCsmErrorInfo error;

        public ClankDiagnosticChildren(ClankCsmErrorInfo info) {
            this.error = info;
            setKeys(ClankCsmErrorInfoAccessor.getDefault().getDelegate(info).notes());
        }

        @Override
        protected Node[] createNodes(ClankDiagnosticInfo key) {
            return new Node[]{new ClankDiagnosticInfoNode(error, key)};
        }

    }

    private class ClankDiagnosticInfoNode extends AbstractNode {

        private final ClankDiagnosticInfo note;
        private final ClankCsmErrorInfo error;

        public ClankDiagnosticInfoNode(ClankCsmErrorInfo error, ClankDiagnosticInfo note) {
            super(Children.LEAF);
            this.error = error;
            this.note = note;
            setName(note.getMessage());
        }

        @Override
        public String getHtmlDisplayName() {
            try {
                CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(error);
                FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                FileObject fo = CndFileUtils.toFileObject(fSystem, note.getSourceFileName());
                CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, true, true);
                int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, note.getStartOffset());
                int[] endlineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, note.getEndOffset());
                StringBuilder htmlName = new StringBuilder("<html>");
                htmlName.append("<b>").append(CndPathUtilities.getBaseName(note.getSourceFileName())).append(" at ");
                htmlName.append(lineColumnByOffset[0]).append(":").append(lineColumnByOffset[1]).append(" to ");
                htmlName.append(endlineColumnByOffset[0]).append(":").append(endlineColumnByOffset[1]).append("</b>");
                htmlName.append(" ").append(note.getMessage()).append("</html>");
                return htmlName.toString();
            } catch (FileStateInvalidException ex) {
                //Exceptions.printStackTrace(ex);
            }
            return getName();
        }

        @Override
        public SystemAction getDefaultAction() {
            return super.getDefaultAction(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("/org/netbeans/modules/cnd/diagnostics/clank/resources/warning.gif");//NOI18N
        }

    }
}
