/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class SourcesPanelVisual extends JPanel {

    private static final long serialVersionUID = -358263102348820543L;
    static final LocalServer DEFAULT_LOCAL_SERVER;

    private final WebFolderNameProvider webFolderNameProvider;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    MutableComboBoxModel localServerComboBoxModel = new LocalServerComboBoxModel();
    private final LocalServerComboBoxEditor localServerComboBoxEditor = new LocalServerComboBoxEditor();

    static {
        String msg = NbBundle.getMessage(SourcesPanelVisual.class, "LBL_UseProjectFolder",
                File.separator + ConfigureProjectPanel.DEFAULT_SOURCE_FOLDER);
        DEFAULT_LOCAL_SERVER = new LocalServer(null, null, msg, false);
    }

    /** Creates new form SourcesPanelVisual */
    public SourcesPanelVisual(WebFolderNameProvider webFolderNameProvider) {
        this.webFolderNameProvider = webFolderNameProvider;
        initComponents();
        init();
    }

    private void init() {
        localServerComboBox.setModel(localServerComboBoxModel);
        localServerComboBox.setRenderer(new LocalServerComboBoxRenderer());
        localServerComboBox.setEditor(localServerComboBoxEditor);
    }

    void addSourcesListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeSourcesListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourcesLabel = new javax.swing.JLabel();
        localServerComboBox = new javax.swing.JComboBox();
        locateButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        localServerLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(sourcesLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_Sources")); // NOI18N

        localServerComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(locateButton, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_LocateLocalServer")); // NOI18N
        locateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_BrowseLocalServer")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(localServerLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "TXT_LocalServer")); // NOI18N
        localServerLabel.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(sourcesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(localServerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(localServerComboBox, 0, 319, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(locateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourcesLabel)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(locateButton)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localServerLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        LocalServer ls = (LocalServer) localServerComboBox.getSelectedItem();
        String newLocation = Utils.browseLocationAction(this, ls.getDocumentRoot());
        if (newLocation == null) {
            return;
        }

        String projectLocation = new File(newLocation, webFolderNameProvider.getWebFolderName()).getAbsolutePath();
        for (int i = 0; i < localServerComboBoxModel.getSize(); i++) {
            LocalServer element = (LocalServer) localServerComboBoxModel.getElementAt(i);
            if (projectLocation.equals(element.getSrcRoot())) {
                localServerComboBox.setSelectedIndex(i);
                return;
            }
        }
        LocalServer localServer = new LocalServer(newLocation, projectLocation);
        localServerComboBoxModel.addElement(localServer);
        localServerComboBox.setSelectedItem(localServer);
        Utils.sortComboBoxModel(localServerComboBoxModel);
    }//GEN-LAST:event_browseButtonActionPerformed

    private void locateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locateButtonActionPerformed
        // XXX
        String message = "Not implemented yet."; // NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                message,
                message,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(descriptor);
    }//GEN-LAST:event_locateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel localServerLabel;
    private javax.swing.JButton locateButton;
    private javax.swing.JLabel sourcesLabel;
    // End of variables declaration//GEN-END:variables

    static boolean isProjectFolder(LocalServer localServer) {
        return DEFAULT_LOCAL_SERVER.equals(localServer);
    }

    LocalServer getSourcesLocation() {
        return (LocalServer) localServerComboBox.getSelectedItem();
    }

    MutableComboBoxModel getLocalServerModel() {
        return localServerComboBoxModel;
    }

    void setLocalServerModel(MutableComboBoxModel localServers) {
        localServerComboBoxModel = localServers;
        localServerComboBox.setModel(localServerComboBoxModel);
    }

    void selectSourcesLocation(LocalServer localServer) {
        localServerComboBox.setSelectedItem(localServer);
    }

    static class LocalServer implements Comparable<LocalServer> {
        private final String virtualHost;
        private final String documentRoot;
        private final boolean editable;
        private String srcRoot;

        public LocalServer(final LocalServer localServer) {
            this.virtualHost = localServer.virtualHost;
            this.documentRoot = localServer.documentRoot;
            this.srcRoot = localServer.srcRoot;
            this.editable = localServer.editable;
        }

        public LocalServer(String srcRoot) {
            this(null, null, srcRoot);
        }

        public LocalServer(String documentRoot, String srcRoot) {
            this(null, documentRoot, srcRoot);
        }

        public LocalServer(String virtualHost, String documentRoot, String srcRoot) {
            this(virtualHost, documentRoot, srcRoot, true);
        }

        public LocalServer(String virtualHost, String documentRoot, String srcRoot, boolean editable) {
            this.virtualHost = virtualHost;
            this.documentRoot = documentRoot;
            this.srcRoot = srcRoot;
            this.editable = editable;
        }

        public String getVirtualHost() {
            return virtualHost;
        }

        public String getDocumentRoot() {
            return documentRoot;
        }

        public String getSrcRoot() {
            return srcRoot;
        }

        public void setSrcRoot(String srcRoot) {
            if (!editable) {
                throw new UnsupportedOperationException("srcRoot cannot be changed because instance is not editable");
            }
            this.srcRoot = srcRoot;
        }

        public boolean isEditable() {
            return editable;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getName());
            sb.append("[virtualHost: ");
            sb.append(virtualHost);
            sb.append(", documentRoot: ");
            sb.append(documentRoot);
            sb.append(", srcRoot: ");
            sb.append(srcRoot);
            sb.append(", editable: ");
            sb.append(editable);
            sb.append("]");
            return sb.toString();
        }

        public int compareTo(LocalServer ls) {
            if (!editable) {
                return -1;
            }
            return srcRoot.compareTo(ls.getSrcRoot());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LocalServer other = (LocalServer) obj;
            if (virtualHost != other.virtualHost && (virtualHost == null || !virtualHost.equals(other.virtualHost))) {
                return false;
            }
            if (documentRoot != other.documentRoot && (documentRoot == null || !documentRoot.equals(other.documentRoot))) {
                return false;
            }
            if (editable != other.editable) {
                return false;
            }
            if (srcRoot != other.srcRoot && (srcRoot == null || !srcRoot.equals(other.srcRoot))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (virtualHost != null ? virtualHost.hashCode() : 0);
            hash = 97 * hash + (documentRoot != null ? documentRoot.hashCode() : 0);
            hash = 97 * hash + (editable ? 1 : 0);
            hash = 97 * hash + (srcRoot != null ? srcRoot.hashCode() : 0);
            return hash;
        }
    }

    static interface WebFolderNameProvider {
        String getWebFolderName();
    }

    private static class LocalServerComboBoxModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = 193082264935872743L;

        public LocalServerComboBoxModel() {
            addElement(DEFAULT_LOCAL_SERVER);
        }
    }

    private static class LocalServerComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 31965318763243602L;

        public LocalServerComboBoxRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof LocalServer;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((LocalServer) value).getSrcRoot());

            // never selected
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            return this;
        }
    }

    private class LocalServerComboBoxEditor implements ComboBoxEditor, UIResource, DocumentListener {
        private static final long serialVersionUID = -4527321803090719483L;

        private final JTextField component = new JTextField();
        private LocalServer activeItem;

        public LocalServerComboBoxEditor() {
            component.setOpaque(true);
            component.getDocument().addDocumentListener(this);
        }

        public Component getEditorComponent() {
            return component;
        }

        public void setItem(Object anObject) {
            if (anObject == null) {
                return;
            }
            assert anObject instanceof LocalServer;
            activeItem = (LocalServer) anObject;
            component.setText(activeItem.getSrcRoot());
        }

        public Object getItem() {
            return new LocalServer(activeItem);
        }

        public void selectAll() {
            component.selectAll();
            component.requestFocus();
        }

        public void addActionListener(ActionListener l) {
            component.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            component.removeActionListener(l);
        }

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            boolean enabled = false;
            if (activeItem.isEditable()) {
                enabled = true;
                activeItem.setSrcRoot(component.getText().trim());
            }
            component.setEnabled(enabled);
            changeSupport.fireChange();
        }
    }
}
