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
package org.netbeans.modules.maven.nodes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author  mkleint
 */
public class AddDependencyPanel extends javax.swing.JPanel implements ActionListener {

    private MavenProject project;

    private TextValueCompleter groupCompleter;
    private TextValueCompleter artifactCompleter;
    private TextValueCompleter versionCompleter;
    private JButton okButton;
    private QueryPanel queryPanel;

    private Color defaultProgressC, curProgressC;
    private static final int PROGRESS_STEP = 10;
    private static final int CYCLE_LOWER_LIMIT = -3;
    private static final int CYCLE_UPPER_LIMIT = 5;
    private int varianceStep, variance;
    private Timer progressTimer = new Timer(100, this);

    private DMListPanel artifactList;

    private static final String DELIMITER = " : ";

    /** Creates new form AddDependencyPanel */
    public AddDependencyPanel(MavenProject project) {
        this.project = project;
        initComponents();
        groupCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtGroupId);
        artifactCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtArtifactId);
        versionCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtVersion);
        txtGroupId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (txtGroupId.getText().trim().length() > 0) {
                    artifactCompleter.setLoading(true);
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            populateArtifact();
                        }
                    });
                }
            }
        });

        txtArtifactId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (txtGroupId.getText().trim().length() > 0 &&
                    txtArtifactId.getText().trim().length() > 0) {
                    versionCompleter.setLoading(true);
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            populateVersion();
                        }
                    });
                }
            }
        });

        okButton = new JButton(NbBundle.getMessage(AddDependencyPanel.class, "BTN_OK"));

        DocumentListener docList = new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                checkValidState();
            }

            public void insertUpdate(DocumentEvent e) {
                checkValidState();
            }

            public void removeUpdate(DocumentEvent e) {
                checkValidState();
            }
        };
        txtGroupId.getDocument().addDocumentListener(docList);
        txtVersion.getDocument().addDocumentListener(docList);
        txtArtifactId.getDocument().addDocumentListener(docList);
        checkValidState();
        groupCompleter.setLoading(true);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                populateGroupId();
            }
        });

        queryPanel = new QueryPanel(this);
        resultsPanel.add(queryPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }
        });

        defaultProgressC = progressLabel.getForeground();
        setSearchInProgressUI(false);

        artifactList = new DMListPanel(this, project);
        artifactPanel.add(artifactList, BorderLayout.CENTER);

    }

    public JButton getOkButton() {
        return okButton;
    }

    public String getGroupId() {
        return txtGroupId.getText().trim();
    }

    public String getArtifactId() {
        return txtArtifactId.getText().trim();
    }

    public String getVersion() {
        return txtVersion.getText().trim();
    }

    public String getScope() {
        String scope = comScope.getSelectedItem().toString();
        if ("compile".equals(scope)) { //NOI18N
            //compile is the default scope, no need to explicitly define.
            scope = null;
        }
        return scope;
    }

    private void checkValidState() {
        if (txtGroupId.getText().trim().length() <= 0) {
            okButton.setEnabled(false);
            return;
        }
        if (txtArtifactId.getText().trim().length() <= 0) {
            okButton.setEnabled(false);
            return;
        }
        boolean depMngActive = tabPane.getSelectedIndex() == 1;
        if (txtVersion.getText().trim().length() <= 0 && !depMngActive) {
            okButton.setEnabled(false);
            return;
        }
        
        okButton.setEnabled(true);
    }

    private static Border getNbScrollPaneBorder () {
        Border b = UIManager.getBorder("Nb.ScrollPane.border");
        if (b == null) {
            Color c = UIManager.getColor("controlShadow");
            b = new LineBorder(c != null ? c : Color.GRAY);
        }
        return b;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coordPanel = new javax.swing.JPanel();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblScope = new javax.swing.JLabel();
        comScope = new javax.swing.JComboBox();
        tabPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        resultsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        artifactsLabel = new javax.swing.JLabel();
        artifactPanel = new javax.swing.JPanel();

        lblGroupId.setLabelFor(txtGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroupId, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_GroupId")); // NOI18N

        lblArtifactId.setLabelFor(txtArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(lblArtifactId, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_ArtifactId")); // NOI18N

        lblVersion.setLabelFor(txtVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblVersion, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_Version")); // NOI18N

        lblScope.setLabelFor(comScope);
        org.openide.awt.Mnemonics.setLocalizedText(lblScope, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_Scope")); // NOI18N

        comScope.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "compile", "runtime", "test", "provided" }));

        org.jdesktop.layout.GroupLayout coordPanelLayout = new org.jdesktop.layout.GroupLayout(coordPanel);
        coordPanel.setLayout(coordPanelLayout);
        coordPanelLayout.setHorizontalGroup(
            coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblArtifactId)
                    .add(lblGroupId)
                    .add(lblVersion))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, coordPanelLayout.createSequentialGroup()
                        .add(txtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lblScope)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        coordPanelLayout.setVerticalGroup(
            coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, coordPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblGroupId))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArtifactId)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblVersion)
                    .add(comScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblScope)
                    .add(txtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(43, 43, 43))
        );

        searchPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                searchPanelComponentShown(evt);
            }
        });

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchLabel.text", new Object[] {})); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.text", new Object[] {})); // NOI18N

        jLabel1.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel1.text", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.resultsLabel.text", new Object[] {})); // NOI18N

        resultsPanel.setBorder(getNbScrollPaneBorder());
        resultsPanel.setLayout(new java.awt.BorderLayout());

        progressLabel.setForeground(java.awt.SystemColor.textInactiveText);
        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.progressLabel.text", new Object[] {})); // NOI18N
        progressLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.progressLabel.toolTipText", new Object[] {})); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(progressLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(progressLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, searchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))
                    .add(searchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(resultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 202, Short.MAX_VALUE)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(searchPanelLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(searchLabel)
                        .add(4, 4, 4)
                        .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(searchPanelLayout.createSequentialGroup()
                                .add(jLabel1)
                                .add(14, 14, 14))
                            .add(searchField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchLabel)
                    .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resultsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchPanel.TabConstraints.tabTitle", new Object[] {}), searchPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(artifactsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.artifactsLabel.text", new Object[] {})); // NOI18N

        artifactPanel.setBorder(getNbScrollPaneBorder());
        artifactPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(artifactPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                    .add(artifactsLabel))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(artifactsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(artifactPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jPanel2.TabConstraints.tabTitle", new Object[] {}), jPanel2); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, coordPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(111, 111, 111)
                .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(coordPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(335, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_searchPanelComponentShown
        // TODO add your handling code here:
        searchField.requestFocus();
    }//GEN-LAST:event_searchPanelComponentShown

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel artifactPanel;
    private javax.swing.JLabel artifactsLabel;
    private javax.swing.JComboBox comScope;
    private javax.swing.JPanel coordPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblScope;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration

    private void populateGroupId() {
        assert !SwingUtilities.isEventDispatchThread();
        final List<String> lst = new ArrayList<String>(RepositoryQueries.getGroups());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                groupCompleter.setValueList(lst);
            }
        });

    }

    private void populateArtifact() {
        assert !SwingUtilities.isEventDispatchThread();

        final List<String> lst = new ArrayList<String>(RepositoryQueries.getArtifacts(txtGroupId.getText().trim()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                artifactCompleter.setValueList(lst);
            }
        });

    }

    private void populateVersion() {
        assert !SwingUtilities.isEventDispatchThread();

        List<NBVersionInfo> lst = RepositoryQueries.getVersions(txtGroupId.getText().trim(), txtArtifactId.getText().trim());
        final List<String> vers = new ArrayList<String>();
        for (NBVersionInfo rec : lst) {
            if (!vers.contains(rec.getVersion())) {
                vers.add(rec.getVersion());
            }
        }
        Collections.sort(vers);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                versionCompleter.setValueList(vers);
            }
        });

    }

    private void setSearchInProgressUI (boolean inProgress) {
        if (inProgress && progressLabel.isVisible()) {
            return;
        }
        if (inProgress) {
            progressLabel.setForeground(defaultProgressC);
            progressLabel.setVisible(true);
            curProgressC = defaultProgressC;
            varianceStep = 1;
            variance = 0;
            progressTimer.start();
        } else {
            progressLabel.setVisible(false);
            progressTimer.stop();
        }
    }

    /** ActionListener for progressTimer, performs color changing **/
    public void actionPerformed(ActionEvent e) {
        int curVariance = PROGRESS_STEP * variance;
        curProgressC = new Color(Math.min(255, Math.max(0, defaultProgressC.getRed() + curVariance)),
                Math.min(255, Math.max(0, defaultProgressC.getGreen() + curVariance)),
                Math.min(255, Math.max(0, defaultProgressC.getBlue() + curVariance)));
        progressLabel.setForeground(curProgressC);
        if (variance == CYCLE_LOWER_LIMIT || variance == CYCLE_UPPER_LIMIT) {
            varianceStep = -varianceStep;
        }
        variance += varianceStep;
    }

    private static class QueryPanel extends JPanel implements ExplorerManager.Provider, Comparator<String>, PropertyChangeListener {

        private BeanTreeView btv;
        private ExplorerManager manager;

        private static final Object LOCK = new Object();
        private static final int TYPE_DELAY = 500;
        private String inProgressText, lastQueryText, curTypedText;
        private Timer typeTimer;

        private static Node noResultsRoot;
        private AddDependencyPanel depPanel;
        private Color defSearchC;

        /** Creates new form FindResultsPanel */
        private QueryPanel(AddDependencyPanel depPanel) {
            this.depPanel = depPanel;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(false);
            manager = new ExplorerManager();
            manager.setRootContext(getNoResultsRoot());
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            defSearchC = depPanel.searchField.getForeground();
            manager.addPropertyChangeListener(this);
            depPanel.resultsLabel.setLabelFor(btv);
        }

        void maybeFind (String text) {
            curTypedText = text.trim();
            depPanel.searchField.setForeground(defSearchC);

            if (typeTimer == null) {
                typeTimer = new Timer(TYPE_DELAY, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (curTypedText.length() < 3) {
                            depPanel.searchField.setForeground(Color.RED);
                        } else {
                            depPanel.searchField.setForeground(defSearchC);
                            find(curTypedText);
                        }
                    }
                });
                typeTimer.setRepeats(false);
            }

            typeTimer.restart();
        }

        void find(String queryText) {
            synchronized (LOCK) {
                if (inProgressText != null) {
                    lastQueryText = queryText;
                    return;
                }
                inProgressText = queryText;
                lastQueryText = null;
            }

            depPanel.setSearchInProgressUI(true);

            final List<QueryField> fields = new ArrayList<QueryField>();
            String q = queryText.trim();
            String[] splits = q.split(" "); //NOI118N

            List<String> fStrings = new ArrayList<String>();
            fStrings.add(QueryField.FIELD_GROUPID);
            fStrings.add(QueryField.FIELD_ARTIFACTID);
            fStrings.add(QueryField.FIELD_VERSION);
            fStrings.add(QueryField.FIELD_NAME);
            fStrings.add(QueryField.FIELD_DESCRIPTION);
            fStrings.add(QueryField.FIELD_CLASSES);

            for (String curText : splits) {
                for (String fld : fStrings) {
                    QueryField f = new QueryField();
                    f.setField(fld);
                    f.setValue(curText);
                    fields.add(f);
                }
            }

            Task t = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final List<NBVersionInfo> infos = RepositoryQueries.find(fields);

                    Node node = null;
                    final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

                    for (NBVersionInfo nbvi : infos) {
                        String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                        List<NBVersionInfo> get = map.get(key);
                        if (get == null) {
                            get = new ArrayList<NBVersionInfo>();
                            map.put(key, get);
                        }
                        get.add(nbvi);
                    }
                    final List<String> keyList = new ArrayList<String>(map.keySet());
                    // sort specially using our comparator, see compare method
                    Collections.sort(keyList, QueryPanel.this);

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            manager.setRootContext(createResultsNode(keyList, map));
                        }
                    });
                }
            });

            t.addTaskListener(new TaskListener() {

                public void taskFinished(Task task) {
                    synchronized (LOCK) {
                        String localText = inProgressText;
                        inProgressText = null;
                        if (lastQueryText != null && !lastQueryText.equals(localText)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (lastQueryText != null) {
                                        maybeFind(lastQueryText);
                                    }
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    depPanel.setSearchInProgressUI(false);
                                }
                            });
                        }
                    }
                }
            });
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private static Node getNoResultsRoot() {
            if (noResultsRoot == null) {
                AbstractNode nd = new AbstractNode(Children.LEAF) {

                    @Override
                    public Image getIcon(int arg0) {
                        return ImageUtilities.loadImage("org/netbeans/modules/maven/resources/empty.png"); //NOI18N
                    }

                    @Override
                    public Image getOpenedIcon(int arg0) {
                        return getIcon(arg0);
                    }
                };
                nd.setName("Empty"); //NOI18N

                nd.setDisplayName(NbBundle.getMessage(QueryPanel.class, "LBL_Node_Empty"));

                Children.Array array = new Children.Array();
                array.add(new Node[]{nd});
                noResultsRoot = new AbstractNode(array);
            }

            return noResultsRoot;
        }

        private Node createResultsNode(List<String> keyList, Map<String, List<NBVersionInfo>> map) {
            Node node;
            if (keyList.size() > 0) {
                Children.Array array = new Children.Array();
                node = new AbstractNode(array);

                for (String key : keyList) {
                    array.add(new Node[]{new ArtifactNode(key, map.get(key))});
                }
            } else {
                node = getNoResultsRoot();
            }
            return node;
        }

        /** Impl of comparator, sorts artifacts asfabetically with exception
         * of items that contain current query string, which take precedence.
         */
        public int compare(String s1, String s2) {

            int index1 = s1.indexOf(inProgressText);
            int index2 = s2.indexOf(inProgressText);

            if (index1 >= 0 || index2 >=0) {
                if (index1 < 0) {
                    return 1;
                } else if (index2 < 0) {
                    return -1;
                }
                return index1 - index2;
            } else {
                return s1.compareTo(s2);
            }
        }

        /** PropertyChangeListener impl, stores maven coordinates of selected artifact */
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selNodes = manager.getSelectedNodes();
                if (selNodes.length == 1 && selNodes[0] instanceof VersionNode) {
                    NBVersionInfo vi = ((VersionNode)selNodes[0]).getNBVersionInfo();
                    depPanel.txtGroupId.setText(vi.getGroupId());
                    depPanel.txtArtifactId.setText(vi.getArtifactId());
                    depPanel.txtVersion.setText(vi.getVersion());
                } else {
                    depPanel.txtGroupId.setText("");
                    depPanel.txtArtifactId.setText("");
                    depPanel.txtVersion.setText("");
                }
            }
        }

        private static class ArtifactNode extends AbstractNode {

            private List<NBVersionInfo> versionInfos;

            public ArtifactNode(String name, final List<NBVersionInfo> list) {
                super(new Children.Keys<NBVersionInfo>() {
                    @Override
                    protected Node[] createNodes(NBVersionInfo arg0) {
                        return new Node[]{new VersionNode(arg0, false)};
                    }

                    @Override
                    protected void addNotify() {
                        setKeys(list);
                    }
                });
                this.versionInfos = list;
                setName(name);
                setDisplayName(name);
            }

            @Override
            public Image getIcon(int arg0) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/resources/ArtifactBadge.png", true); //NOI18N

                return badge;
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return getIcon(arg0);
            }

            public List<NBVersionInfo> getVersionInfos() {
                return new ArrayList<NBVersionInfo>(versionInfos);
            }
        }


    } // QueryPanel

    private static class DMListPanel extends JPanel implements ExplorerManager.Provider,
            AncestorListener, ActionListener, PropertyChangeListener {

        private ListView lv;
        private ExplorerManager manager;
        private MavenProject project;
        private Node noDMRoot;
        private AddDependencyPanel depPanel;

        public DMListPanel(AddDependencyPanel depPanel, MavenProject project) {
            this.depPanel = depPanel;
            this.project = project;
            lv = new ListView();
            //lv.setDefaultProcessor(this);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(lv, BorderLayout.CENTER);
            addAncestorListener(this);
            depPanel.artifactsLabel.setLabelFor(lv);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private NBVersionInfo convert2VInfo(Dependency dep) {
            return new NBVersionInfo(null, dep.getGroupId(), dep.getArtifactId(),
                    dep.getVersion(), dep.getType(), null, null, null, dep.getClassifier());
        }

        private void loadArtifacts() {
            List<Dependency> deps = getDepencenciesFromDM();
            if (deps == null || deps.isEmpty()) {
                if (noDMRoot == null) {
                    AbstractNode nd = new AbstractNode(Children.LEAF) {

                        @Override
                        public Image getIcon(int arg0) {
                            return ImageUtilities.loadImage("org/netbeans/modules/maven/resources/empty.png"); //NOI18N
                        }

                        @Override
                        public Image getOpenedIcon(int arg0) {
                            return getIcon(arg0);
                        }
                    };
                    nd.setName("Empty"); //NOI18N

                    nd.setDisplayName(NbBundle.getMessage(DMListPanel.class, "LBL_DM_Empty"));

                    Children.Array array = new Children.Array();
                    array.add(new Node[]{nd});
                    noDMRoot = new AbstractNode(array);
                }
                manager.setRootContext(noDMRoot);
            } else {
                Children.Array array = new Children.Array();
                Node root = new AbstractNode(array);

                for (Dependency dep : deps) {
                    array.add(new Node[]{ new VersionNode(convert2VInfo(dep), true) });
                }

                manager.setRootContext(root);
            }
        }

        private List<Dependency> getDepencenciesFromDM () {
            MavenProject localProj = project;
            DependencyManagement curDM;
            List<Dependency> result = new ArrayList<Dependency>();

            while (localProj.hasParent()) {                
                localProj = localProj.getParent();
                curDM = localProj.getDependencyManagement();
                if (curDM != null) {
                    @SuppressWarnings("unchecked")
                    List<Dependency> ds = curDM.getDependencies();
                    result.addAll(ds);
                }
            }

            return result;
        }

        public void ancestorAdded(AncestorEvent event) {
            loadArtifacts();
        }

        public void ancestorRemoved(AncestorEvent event) {
        }

        public void ancestorMoved(AncestorEvent event) {
        }

        public void actionPerformed(ActionEvent e) {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            Node[] selNodes = manager.getSelectedNodes();
            if (selNodes.length == 1 && selNodes[0] instanceof VersionNode) {
                NBVersionInfo vi = ((VersionNode)selNodes[0]).getNBVersionInfo();
                depPanel.txtGroupId.setText(vi.getGroupId());
                depPanel.txtArtifactId.setText(vi.getArtifactId());
                depPanel.txtVersion.setText("");
            } else {
                depPanel.txtGroupId.setText("");
                depPanel.txtArtifactId.setText("");
                depPanel.txtVersion.setText("");
            }
        }

    }

    private static class VersionNode extends AbstractNode {

        private NBVersionInfo nbvi;
        private boolean fromDepMng;

        /** Creates a new instance of VersionNode */
        public VersionNode(NBVersionInfo versionInfo, boolean fromDepMng) {
            super(Children.LEAF);

            this.nbvi = versionInfo;
            this.fromDepMng = fromDepMng;

            setName(versionInfo.getVersion());

            StringBuilder sb = new StringBuilder();
            if (fromDepMng) {
                sb.append(nbvi.getGroupId());
                sb.append(DELIMITER);
                sb.append(nbvi.getArtifactId());
                sb.append(DELIMITER);
            } else {
                sb.append(nbvi.getVersion());
            }
            sb.append(" [ ");
            sb.append(nbvi.getType());
            String classifier = nbvi.getClassifier();
            if (classifier != null) {
                sb.append(",");
                sb.append(classifier);
            }
            sb.append(" ] ");
            String repo = nbvi.getRepoId();
            if (repo != null) {
                sb.append(" - ");
                sb.append(repo);
            }

            setDisplayName(sb.toString());

            setIconBaseWithExtension("org/netbeans/modules/maven/resources/DependencyJar.gif"); //NOI18N

        }

        /*@Override
        public java.awt.Image getIcon(int param) {
            java.awt.Image retValue = super.getIcon(param);
            if (hasJavadoc) {
                retValue = ImageUtilities.mergeImages(retValue,
                        ImageUtilities.loadImage("org/netbeans/modules/maven/repository/DependencyJavadocIncluded.png"),//NOI18N
                        12, 12);
            }
            if (hasSources) {
                retValue = ImageUtilities.mergeImages(retValue,
                        ImageUtilities.loadImage("org/netbeans/modules/maven/repository/DependencySrcIncluded.png"),//NOI18N
                        12, 8);
            }
            return retValue;

        }*/

        public NBVersionInfo getNBVersionInfo() {
            return nbvi;
        }

        @Override
        public String getShortDescription() {
            return nbvi.toString();
        }
    }


}
