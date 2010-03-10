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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.support.DelayedDocumentChangeListener;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.spi.nodes.MavenNodeFactory;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
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

    private final TextValueCompleter groupCompleter;
    private final TextValueCompleter artifactCompleter;
    private final TextValueCompleter versionCompleter;
    private final JButton okButton;
    private final QueryPanel queryPanel;
    private DMListPanel artifactList;

    private Color defaultProgressC, curProgressC, defaultVersionC;
    private static final int PROGRESS_STEP = 10;
    private static final int CYCLE_LOWER_LIMIT = -3;
    private static final int CYCLE_UPPER_LIMIT = 5;
    private int varianceStep, variance;
    private Timer progressTimer = new Timer(100, this);


    private static final String DELIMITER = " : ";

    private NotificationLineSupport nls;
    private RepositoryInfo nbRepo;

    /** Creates new form AddDependencyPanel */
    public AddDependencyPanel(MavenProject mavenProject, Project prj) {
        this(mavenProject, true, prj);
    }
    public AddDependencyPanel(MavenProject mavenProject, boolean showDepMan, Project prj) {
        this.project = mavenProject;
        this.nbRepo = RepositoryPreferences.getInstance().getRepositoryInfoById("netbeans");
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

        queryPanel = new QueryPanel();
        resultsPanel.add(queryPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                DelayedDocumentChangeListener.create(
                searchField.getDocument(), queryPanel, 500));

        /*searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }
        });*/

        defaultProgressC = progressLabel.getForeground();
        defaultVersionC = txtVersion.getForeground();
        setSearchInProgressUI(false);
        if (showDepMan) {
            artifactList = new DMListPanel(project);
            artifactPanel.add(artifactList, BorderLayout.CENTER);
        } else {
            tabPane.setEnabledAt(1, false);
        }
        chkNbOnly.setVisible(nbRepo != null);
        if (nbRepo != null) {
            String packaging = mavenProject.getPackaging();
            chkNbOnly.setSelected(NbMavenProject.TYPE_NBM.equals(packaging) ||
                    NbMavenProject.TYPE_NBM_APPLICATION.equals(packaging));
        }

        pnlOpenProjects.add(new OpenListPanel(prj), BorderLayout.CENTER);

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

    /** For gaining access to DialogDisplayer instance to manage
     * warning messages
     */
    public void attachDialogDisplayer(DialogDescriptor dd) {
        nls = dd.getNotificationLineSupport();
        if (nls == null) {
            nls = dd.createNotificationLineSupport();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        assert nls != null : " The notificationLineSupport was not attached to the panel."; //NOI18N
    }

    void setSelectedScope(String type) {
        comScope.setSelectedItem(type);
    }

    private void checkValidState() {
        String gId = txtGroupId.getText().trim();
        if (gId.length() <= 0) {
            gId = null;
        }
        String aId = txtArtifactId.getText().trim();
        if (aId.length() <= 0) {
            aId = null;
        }
        String version = txtVersion.getText().trim();
        if (version.length() <= 0) {
            version = null;
        }

        boolean dmDefined = tabPane.isEnabledAt(1);
        if (artifactList != null) {
            Color c = defaultVersionC;
            String warn = null;
            if (dmDefined) {
                if (findConflict(artifactList.getDMDeps(), gId, aId, version, null) == 1) {
                    c = Color.RED;
                    warn = NbBundle.getMessage(AddDependencyPanel.class, "MSG_VersionConflict");
                }
            }
            txtVersion.setForeground(c);
            if (warn != null) {
                nls.setWarningMessage(warn);
            } else {
                nls.clearMessages();
            }
        }

        if (gId == null) {
            okButton.setEnabled(false);
            return;
        }
        if (aId == null) {
            okButton.setEnabled(false);
            return;
        }
        if (version == null && !dmDefined) {
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
        chkNbOnly = new javax.swing.JCheckBox();
        progressLabel = new javax.swing.JLabel();
        pnlDepMan = new javax.swing.JPanel();
        artifactsLabel = new javax.swing.JLabel();
        artifactPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pnlOpen = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        pnlOpenProjects = new javax.swing.JPanel();

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
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, coordPanelLayout.createSequentialGroup()
                        .add(txtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
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

        txtGroupId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtGroupId.AccessibleContext.accessibleDescription")); // NOI18N
        txtArtifactId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtArtifactId.AccessibleContext.accessibleDescription")); // NOI18N
        txtVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtVersion.AccessibleContext.accessibleDescription")); // NOI18N
        comScope.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.comScope.AccessibleContext.accessibleDescription")); // NOI18N

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

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 76, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(chkNbOnly, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.chkNbOnly.text")); // NOI18N
        chkNbOnly.setToolTipText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.chkNbOnly.toolTipText")); // NOI18N
        chkNbOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNbOnlyActionPerformed(evt);
            }
        });

        progressLabel.setForeground(java.awt.SystemColor.textInactiveText);
        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.progressLabel.text", new Object[] {})); // NOI18N
        progressLabel.setToolTipText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.progressLabel.toolTipText", new Object[] {})); // NOI18N

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(searchPanelLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(searchLabel)
                        .add(4, 4, 4)
                        .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                                .add(searchField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(chkNbOnly))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, progressLabel)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, searchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                            .add(searchPanelLayout.createSequentialGroup()
                                .add(resultsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 205, Short.MAX_VALUE)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(106, 106, 106)))))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchLabel)
                    .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chkNbOnly))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(resultsLabel)
                        .add(progressLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
        resultsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.resultsLabel.AccessibleContext.accessibleDescription")); // NOI18N

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchPanel.TabConstraints.tabTitle", new Object[] {}), searchPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(artifactsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.artifactsLabel.text", new Object[] {})); // NOI18N

        artifactPanel.setBorder(getNbScrollPaneBorder());
        artifactPanel.setLayout(new java.awt.BorderLayout());

        jLabel2.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel2.text", new Object[] {})); // NOI18N
        artifactPanel.add(jLabel2, java.awt.BorderLayout.PAGE_END);

        org.jdesktop.layout.GroupLayout pnlDepManLayout = new org.jdesktop.layout.GroupLayout(pnlDepMan);
        pnlDepMan.setLayout(pnlDepManLayout);
        pnlDepManLayout.setHorizontalGroup(
            pnlDepManLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlDepManLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlDepManLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(artifactPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .add(artifactsLabel))
                .addContainerGap())
        );
        pnlDepManLayout.setVerticalGroup(
            pnlDepManLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlDepManLayout.createSequentialGroup()
                .addContainerGap()
                .add(artifactsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(artifactPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlDepMan.TabConstraints.tabTitle", new Object[] {}), pnlDepMan); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel3.text")); // NOI18N

        pnlOpenProjects.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout pnlOpenLayout = new org.jdesktop.layout.GroupLayout(pnlOpen);
        pnlOpen.setLayout(pnlOpenLayout);
        pnlOpenLayout.setHorizontalGroup(
            pnlOpenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlOpenLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlOpenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlOpenProjects, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .add(jLabel3))
                .addContainerGap())
        );
        pnlOpenLayout.setVerticalGroup(
            pnlOpenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlOpenLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlOpenProjects, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlOpen.TabConstraints.tabTitle"), pnlOpen); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, coordPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(coordPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
        );

        tabPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.tabPane.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_searchPanelComponentShown
        searchField.requestFocus();
    }//GEN-LAST:event_searchPanelComponentShown

    private void chkNbOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNbOnlyActionPerformed
        queryPanel.stateChanged(new ChangeEvent(searchField.getDocument()));
    }//GEN-LAST:event_chkNbOnlyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel artifactPanel;
    private javax.swing.JLabel artifactsLabel;
    private javax.swing.JCheckBox chkNbOnly;
    private javax.swing.JComboBox comScope;
    private javax.swing.JPanel coordPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblScope;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlDepMan;
    private javax.swing.JPanel pnlOpen;
    private javax.swing.JPanel pnlOpenProjects;
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
        // also include properties/expressions that could be related to version
        // management
        List<String> propList = new ArrayList<String>();
        for (Object propKey : project.getProperties().keySet()) {
            String key = (String)propKey;
            if (key.endsWith(".version")) { //NOI18N
                // is this the correct heuristics?
                propList.add("${" + key + "}");
            }
        }
        Collections.sort(propList);
        vers.addAll(propList);
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

    private static List<Dependency> getDepencenciesFromDM (MavenProject project) {
        MavenProject localProj = project;
        DependencyManagement curDM;
        List<Dependency> result = new ArrayList<Dependency>();
        //mkleint: without the managementKey checks I got some entries multiple times.
        // do we actually need to traverse the parent poms, are they completely resolved anyway?
        //XXX
        Set<String> knownKeys = new HashSet<String>();

        while (localProj != null) {
            curDM = localProj.getDependencyManagement();
            if (curDM != null) {
                @SuppressWarnings("unchecked")
                List<Dependency> ds = curDM.getDependencies();
                for (Dependency d : ds) {
                    if (knownKeys.contains(d.getManagementKey())) {
                        continue;
                    }
                    result.add(d);
                    knownKeys.add(d.getManagementKey());
                }
            }
            localProj = localProj.getParent();
        }

        return result;
    }

    /**
     * @return 0 -> no conflicts, 1 -> conflict in version, 2 -> conflict in scope
     */
    private static int findConflict (List<Dependency> deps, String groupId, String artifactId, String version, String scope) {
        if (deps == null) {
            return 0;
        }
        for (Dependency dep : deps) {
            if (artifactId != null && artifactId.equals(dep.getArtifactId()) &&
                    groupId != null && groupId.equals(dep.getGroupId())) {
                if (version != null && !version.equals(dep.getVersion())) {
                    return 1;
                }
                if (scope != null) {
                    if (!scope.equals(dep.getScope())) {
                        return 2;
                    }
                } else if (dep.getScope() != null) {
                    return 2;
                }

            }
        }

        return 0;
    }

    void setFields(String groupId, String artifactId, String version) {
        boolean sameGrId = false;
        if (groupId != null && groupId.equals(project.getGroupId())) {
            groupId = "${project.groupId}"; //NOI18N
            sameGrId = true;
        }
        txtGroupId.setText(groupId);
        txtArtifactId.setText(artifactId);
        if (sameGrId && version != null && version.equals(project.getVersion())) {
            version = "${project.version}"; //NOI18N
        }
        txtVersion.setText(version);
    }

    private static Node noResultsRoot;

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

    private static final Object LOCK = new Object();

    private class QueryPanel extends JPanel implements ExplorerManager.Provider,
            Comparator<String>, PropertyChangeListener, ChangeListener {

        private BeanTreeView btv;
        private ExplorerManager manager;

        private String inProgressText, lastQueryText, curTypedText;

        private Color defSearchC;

        /** Creates new form FindResultsPanel */
        private QueryPanel() {
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            btv.setUseSubstringInQuickSearch(true);
            manager = new ExplorerManager();
            manager.setRootContext(getNoResultsRoot());
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            defSearchC = AddDependencyPanel.this.searchField.getForeground();
            manager.addPropertyChangeListener(this);
            AddDependencyPanel.this.resultsLabel.setLabelFor(btv);
            btv.getAccessibleContext().setAccessibleDescription(AddDependencyPanel.this.resultsLabel.getAccessibleContext().getAccessibleDescription());
        }

        /** delayed change of query text */
        @Override
        public void stateChanged (ChangeEvent e) {
            Document doc = (Document)e.getSource();
            try {
                curTypedText = doc.getText(0, doc.getLength()).trim();
            } catch (BadLocationException ex) {
                // should never happen, nothing we can do probably
                return;
            }

            AddDependencyPanel.this.searchField.setForeground(defSearchC);

            if (curTypedText.length() > 0) {
                RepositoryInfo[] infos;
                if (chkNbOnly.isSelected() && AddDependencyPanel.this.nbRepo != null) {
                    infos = new RepositoryInfo[] { AddDependencyPanel.this.nbRepo };
                } else {
                    infos = new RepositoryInfo[0];
                }
                find(curTypedText, infos);
            }
        }

        /**
         *
         * @param queryText
         * @param infos Repositories to search in, null means all repos
         */
        void find(String queryText, final RepositoryInfo... infos) {
            synchronized (LOCK) {
                if (inProgressText != null) {
                    lastQueryText = queryText;
                    return;
                }
                inProgressText = queryText;
                lastQueryText = null;
            }

            AddDependencyPanel.this.setSearchInProgressUI(true);

            final List<QueryField> fields = new ArrayList<QueryField>();
            final List<QueryField> fieldsNonClasses = new ArrayList<QueryField>();
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
                    if (!QueryField.FIELD_CLASSES.equals(fld)) {
                        fieldsNonClasses.add(f);
                    }
                }
            }

            Task t = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    List<NBVersionInfo> tempInfos = null;
                    boolean tempIsError = false;
                    //first try with classes search included,
                    try {
                        tempInfos = RepositoryQueries.find(fields, infos);
                    } catch (BooleanQuery.TooManyClauses exc) {
                        // if failing, then exclude classes from search..
                        try {
                            tempInfos = RepositoryQueries.find(fieldsNonClasses, infos);
                            //TODO show that classes were excluded somehow?
                        } catch (BooleanQuery.TooManyClauses exc2) {
                            // if still failing, report to the user
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    AddDependencyPanel.this.searchField.setForeground(Color.RED);
                                    AddDependencyPanel.this.nls.setWarningMessage(NbBundle.getMessage(AddDependencyPanel.class, "MSG_TooGeneral"));
                                }
                            });
                            tempIsError = true;
                        }
                    }

                    final List<NBVersionInfo> infos = tempInfos;
                    final boolean isError = tempIsError;

                    final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

                    if (infos != null) {
                        for (NBVersionInfo nbvi : infos) {
                            String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                            List<NBVersionInfo> get = map.get(key);
                            if (get == null) {
                                get = new ArrayList<NBVersionInfo>();
                                map.put(key, get);
                            }
                            get.add(nbvi);
                        }
                    }

                    final List<String> keyList = new ArrayList<String>(map.keySet());
                    // sort specially using our comparator, see compare method
                    Collections.sort(keyList, QueryPanel.this);

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            manager.setRootContext(createResultsNode(keyList, map));
                            if (!isError) {
                                AddDependencyPanel.this.searchField.setForeground(defSearchC);
                                AddDependencyPanel.this.nls.clearMessages();
                            }
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
                                        find(lastQueryText);
                                    }
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    AddDependencyPanel.this.setSearchInProgressUI(false);
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


        private Node createResultsNode(List<String> keyList, Map<String, List<NBVersionInfo>> map) {
            Node node;
            if (keyList.size() > 0) {
                Children.Array array = new Children.Array();
                node = new AbstractNode(array);

                for (String key : keyList) {
                    array.add(new Node[]{createFilterWithDefaultAction(MavenNodeFactory.createArtifactNode(key, map.get(key)), false)});
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
                return s1.compareTo(s2);
            } else {
                return s1.compareTo(s2);
            }
        }

        /** PropertyChangeListener impl, stores maven coordinates of selected artifact */
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selNodes = manager.getSelectedNodes();
                changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
            }
        }

    } // QueryPanel

    private static final Object DM_DEPS_LOCK = new Object();
    private class DMListPanel extends JPanel implements ExplorerManager.Provider,
            AncestorListener, ActionListener, PropertyChangeListener, Runnable {

        private BeanTreeView btv;
        private ExplorerManager manager;
        private MavenProject project;
        private Node noDMRoot;

        private List<Dependency> dmDeps;

        public DMListPanel(MavenProject project) {
            this.project = project;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            btv.setUseSubstringInQuickSearch(true);
            //lv.setDefaultProcessor(this);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            addAncestorListener(this);
            AddDependencyPanel.this.artifactsLabel.setLabelFor(btv);

            // disable tab if DM section not defined
            RequestProcessor.getDefault().post(this);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private NBVersionInfo convert2VInfo(Dependency dep) {
            return new NBVersionInfo(null, dep.getGroupId(), dep.getArtifactId(),
                    dep.getVersion(), dep.getType(), null, null, null, dep.getClassifier());
        }

        private List<Dependency> getDMDeps() {
            synchronized (DM_DEPS_LOCK) {
                return dmDeps;
            }
        }

        private void loadArtifacts() {
            List<Dependency> deps = getDMDeps();
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
                    array.add(new Node[]{ createFilterWithDefaultAction(MavenNodeFactory.createVersionNode(convert2VInfo(dep), true), true) });
                }
                manager.setRootContext(root);
            }
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
            changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
        }

        /** Loads dependencies outside EQ thread, updates tab state in EQ */
        public void run() {
            synchronized (DM_DEPS_LOCK) {
                dmDeps = getDepencenciesFromDM(project);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    boolean dmEmpty = dmDeps.isEmpty();
                    AddDependencyPanel.this.tabPane.setEnabledAt(1, !dmEmpty);
                    if (dmEmpty) {
                        AddDependencyPanel.this.tabPane.setToolTipTextAt(1, NbBundle.getMessage(AddDependencyPanel.class, "TXT_No_DM"));
                    }
                }
            });
        }

    }


    private class OpenListPanel extends JPanel implements ExplorerManager.Provider,
            PropertyChangeListener, Runnable, ActionListener {

        private BeanTreeView btv;
        private ExplorerManager manager;
        private Project project;

        public OpenListPanel(Project project) {
            this.project = project;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            btv.setUseSubstringInQuickSearch(true);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);

            RequestProcessor.getDefault().post(this);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            Node[] selNodes = manager.getSelectedNodes();
            changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
        }

        /** Loads dependencies outside EQ thread, updates tab state in EQ */
        public void run() {
            Project[] prjs = OpenProjects.getDefault().getOpenProjects();
            final List<Node> toRet = new ArrayList<Node>();
            for (Project p : prjs) {
                if (p == project) {
                    continue;
                }
                NbMavenProject mav = p.getLookup().lookup(NbMavenProject.class);
                if (mav != null) {
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    toRet.add(createFilterWithDefaultAction(lvp.createLogicalView(), true));
                }
            }
            Children.Array ch = new Children.Array();
            ch.add(toRet.toArray(new Node[0]));
            Node root = new AbstractNode(ch);
            getExplorerManager().setRootContext(root);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    boolean opEmpty = toRet.isEmpty();
                    AddDependencyPanel.this.tabPane.setEnabledAt(2, !opEmpty);
                    if (opEmpty) {
                        AddDependencyPanel.this.tabPane.setToolTipTextAt(1, NbBundle.getMessage(AddDependencyPanel.class, "TXT_No_Opened"));
                    }
                }
            });
        }

        public void actionPerformed(ActionEvent e) {
            // empty impl, disables default action
        }

    }


    private class DefAction extends AbstractAction implements ContextAwareAction {
        private final boolean close;
        private final Lookup lookup;

        public DefAction(boolean closeNow, Lookup look) {
            this.close = closeNow;
            lookup = look;
        }

        public void actionPerformed(ActionEvent e) {
            Project prj = lookup.lookup(Project.class);
            boolean set = false;
            if (prj != null) {
                NbMavenProject mav = prj.getLookup().lookup(NbMavenProject.class);
                MavenProject m = mav.getMavenProject();
                AddDependencyPanel.this.setFields(m.getGroupId(), m.getArtifactId(), m.getVersion());
                set = true;
            }
            if (!set) {
                NBVersionInfo vi = lookup.lookup(NBVersionInfo.class);
                if (vi != null) {
                    //in dm panel we want to pass empty version
                    String ver = AddDependencyPanel.this.queryPanel.isVisible() ? vi.getVersion() : "";
                    AddDependencyPanel.this.setFields(vi.getGroupId(), vi.getArtifactId(), ver);
                    set = true;
                }
            }
            if (set) {
                if (close) {
                    AddDependencyPanel.this.getOkButton().doClick();
                } else {
                    //reset completion.
                    AddDependencyPanel.this.artifactCompleter.setLoading(true);
                    AddDependencyPanel.this.versionCompleter.setLoading(true);
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            AddDependencyPanel.this.populateArtifact();
                            AddDependencyPanel.this.populateVersion();
                        }
                    });
                }
            } else {
                AddDependencyPanel.this.setFields("","",""); //NOI18N
                //reset completion.
                AddDependencyPanel.this.artifactCompleter.setValueList(Collections.<String>emptyList());
                AddDependencyPanel.this.versionCompleter.setValueList(Collections.<String>emptyList());
            }
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new DefAction(close, actionContext);
        }

    }

    private void changeSelection(Lookup context) {
        new DefAction(false, context).actionPerformed(null);
    }

    private Node createFilterWithDefaultAction(final Node nd, boolean leaf) {
        Children child = leaf ? Children.LEAF : new FilterNode.Children(nd) {
            @Override
            protected Node[] createNodes(Node key) {
                return new Node[] { createFilterWithDefaultAction(key, true)};
            }

        };

        return new FilterNode(nd, child) {
            @Override
            public Action getPreferredAction() {
                return new DefAction(true, nd.getLookup());
            }
        };
    }
}
