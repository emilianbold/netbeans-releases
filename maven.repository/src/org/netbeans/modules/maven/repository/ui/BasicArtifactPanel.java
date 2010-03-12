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


package org.netbeans.modules.maven.repository.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.repository.M2RepositoryBrowserTopComponent;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkleint
 */
public class BasicArtifactPanel extends TopComponent implements MultiViewElement {
    
    private MultiViewElementCallback callback;
    private JToolBar toolbar;

    /** Creates new form BasicArtifactPanel */
    public BasicArtifactPanel(Lookup lookup) {
        super(lookup);
        initComponents();
        lstVersions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1) {
                    Object obj = lstVersions.getSelectedValue();
                    if (obj instanceof String) {
                       //Loading.. text #160353
                        return;
                    }
                    NBVersionInfo info = (NBVersionInfo) obj;
                    if (info != null) {
                        int pos = callback.getTopComponent().getTabPosition();
                        ArtifactViewerFactory fact = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
                        TopComponent tc = fact.createTopComponent(info);
                        tc.openAtTabPosition(pos);
                        callback.getTopComponent().close();
                        tc.requestActive();
                    }
                }
            }
            public void mouseWheelMoved(MouseWheelEvent e) {}
            public void mouseDragged(MouseEvent e) {}
            public void mouseMoved(MouseEvent e) {}
        });
        lstVersions.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NBVersionInfo) {
                    NBVersionInfo info = (NBVersionInfo)value;
                    ((JLabel)c).setText(info.getVersion());
                }
                return c;
            }
        });
        lstVersions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            this.jPanel1.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel2.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel3.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel4.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel5.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
    }

    private String computeSize(long size) {
        long kbytes = size / 1024;
        if (kbytes == 0) {
            return NbBundle.getMessage(BasicArtifactPanel.class, "TXT_Bytes", size);
        }
        long mbytes = kbytes / 1024;
        if (mbytes == 0) {
            return NbBundle.getMessage(BasicArtifactPanel.class, "TXT_kb", kbytes);
        }
        return NbBundle.getMessage(BasicArtifactPanel.class, "TXT_Mb", mbytes);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblPackaging = new javax.swing.JLabel();
        txtPackaging = new javax.swing.JTextField();
        lblClassifier = new javax.swing.JLabel();
        txtClassifier = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSize = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtLastModified = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSHA = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstVersions = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstClassifiers = new javax.swing.JList();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jPanel1.border.title"))); // NOI18N

        lblGroupId.setLabelFor(txtGroupId);
        lblGroupId.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblGroupId.text")); // NOI18N

        txtGroupId.setEditable(false);

        lblArtifactId.setLabelFor(txtArtifactId);
        lblArtifactId.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblArtifactId.text")); // NOI18N

        txtArtifactId.setEditable(false);

        lblVersion.setLabelFor(txtVersion);
        lblVersion.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblVersion.text")); // NOI18N

        txtVersion.setEditable(false);

        lblPackaging.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblPackaging.text")); // NOI18N

        txtPackaging.setEditable(false);

        lblClassifier.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblClassifier.text")); // NOI18N

        txtClassifier.setEditable(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblGroupId)
                            .add(lblArtifactId)
                            .add(lblVersion)
                            .add(lblPackaging))
                        .add(18, 18, 18)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                            .add(txtGroupId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                            .add(txtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                            .add(txtPackaging, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lblClassifier)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(txtClassifier, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGroupId)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArtifactId)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblVersion)
                    .add(txtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPackaging)
                    .add(txtPackaging, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblClassifier)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(txtClassifier, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(1, 1, 1)))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_PrimaryArtifact"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel1.text")); // NOI18N

        txtSize.setEditable(false);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel2.text")); // NOI18N

        txtLastModified.setEditable(false);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel3.text")); // NOI18N

        txtSHA.setEditable(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtLastModified, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                    .add(txtSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                    .add(txtSHA, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(txtLastModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(txtSHA, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(104, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_Versions"))); // NOI18N

        jScrollPane2.setViewportView(lstVersions);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel4.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_SecondaryArtifacts"))); // NOI18N

        jScrollPane3.setViewportView(lstClassifiers);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(15, 15, 15))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(123, 123, 123))
        );

        jScrollPane1.setViewportView(jPanel3);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblClassifier;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblPackaging;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JList lstClassifiers;
    private javax.swing.JList lstVersions;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtClassifier;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtLastModified;
    private javax.swing.JTextField txtPackaging;
    private javax.swing.JTextField txtSHA;
    private javax.swing.JTextField txtSize;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables

    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new M2RepositoryBrowserTopComponent.EditorToolbar();
            toolbar.setFloatable(false);
            Action[] a = new Action[1];
            Action[] actions = getLookup().lookup(a.getClass());
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);
            for (Action act : actions) {
                JButton btn = new JButton();
                Actions.connect(btn, act);
                toolbar.add(btn);
                toolbar.addSeparator(space);
            }
        }
        return toolbar;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentOpened() {
        final Artifact artifact = getLookup().lookup(Artifact.class);
        assert artifact != null;
        
        final NBVersionInfo info = getLookup().lookup(NBVersionInfo.class);
        if (info != null) {
            txtGroupId.setText(info.getGroupId());
            txtArtifactId.setText(info.getArtifactId());
            txtVersion.setText(info.getVersion());
            txtPackaging.setText(info.getType());
            txtClassifier.setText(info.getClassifier());
            txtSize.setText(computeSize(info.getSize()));
            txtLastModified.setText("" + new Date(info.getLastModified()));
        } else {
            txtGroupId.setText(artifact.getGroupId());
            txtArtifactId.setText(artifact.getArtifactId());
            txtVersion.setText(artifact.getVersion());
            txtPackaging.setText(artifact.getType());
            txtClassifier.setText(artifact.getClassifier());
        }


        final DefaultListModel dlm = new DefaultListModel();
        dlm.addElement(NbBundle.getMessage(BasicArtifactPanel.class, "TXT_Loading"));
        lstVersions.setModel(dlm);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final List<NBVersionInfo> infos = RepositoryQueries.getVersions(artifact.getGroupId(), artifact.getArtifactId());
                final ArtifactVersion av = new DefaultArtifactVersion(artifact.getVersion());
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dlm.removeAllElements();
                        for (NBVersionInfo ver : infos) {
                            if (!av.equals(new DefaultArtifactVersion(ver.getVersion()))) {
                                dlm.addElement(ver);
                            }
                        }
                    }
                });
            }
        });
        final DefaultListModel mdl = new DefaultListModel();
        mdl.addElement(NbBundle.getMessage(BasicArtifactPanel.class, "TXT_Loading"));
        lstClassifiers.setModel(mdl);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                List<NBVersionInfo> infos = RepositoryQueries.getRecords(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                final Set<String> classifiers = new TreeSet<String>();
                boolean hasJavadoc = false;
                boolean hasSource = false;
                for (NBVersionInfo inf : infos) {
                    if (inf.getClassifier() != null) {
                        classifiers.add(inf.getClassifier());
                    }
                    if (inf.isJavadocExists()) {
                        hasJavadoc = true;
                    }
                    if (inf.isSourcesExists()) {
                        hasSource = true;
                    }
                }
                if (hasSource) {
                    classifiers.add("source");
                }
                if (hasJavadoc) {
                    classifiers.add("javadoc");
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mdl.removeAllElements();
                        for (String ver : classifiers) {
                            mdl.addElement(ver);
                        }
                    }
                });
            }
        });


        File artFile = FileUtilities.convertArtifactToLocalRepositoryFile(artifact);
        if (artFile.exists()) {
            try {
                String sha = RepositoryUtil.calculateSHA1Checksum(artFile);
                txtSHA.setText(sha);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                txtSHA.setText(NbBundle.getMessage(BasicArtifactPanel.class, "MSG_FailedSHA1"));
            }
        } else {
            txtSHA.setText(NbBundle.getMessage(BasicArtifactPanel.class, "MSG_NOSHA"));
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }


}
