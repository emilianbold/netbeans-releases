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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author  mkleint
 */
public class AddDependencyPanel extends javax.swing.JPanel {

    private TextValueCompleter groupCompleter;
    private TextValueCompleter artifactCompleter;
    private TextValueCompleter versionCompleter;
    private JButton okButton;

    /** Creates new form AddDependencyPanel */
    public AddDependencyPanel() {
        initComponents();
        groupCompleter = new TextValueCompleter(Collections.EMPTY_LIST, txtGroupId);
        artifactCompleter = new TextValueCompleter(Collections.EMPTY_LIST, txtArtifactId);
        versionCompleter = new TextValueCompleter(Collections.EMPTY_LIST, txtVersion);
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
        return comScope.getSelectedItem().toString();
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
        if (txtVersion.getText().trim().length() <= 0) {
            okButton.setEnabled(false);
            return;
        }
        okButton.setEnabled(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPane = new javax.swing.JTabbedPane();
        coordPanel = new javax.swing.JPanel();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblScope = new javax.swing.JLabel();
        comScope = new javax.swing.JComboBox();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        resultsPanel = new QueryPanel();

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
                .add(8, 8, 8)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(coordPanelLayout.createSequentialGroup()
                        .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblVersion)
                            .add(lblGroupId)
                            .add(lblArtifactId))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                            .add(txtArtifactId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                            .add(txtGroupId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)))
                    .add(coordPanelLayout.createSequentialGroup()
                        .add(74, 74, 74)
                        .add(comScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lblScope))
                .add(8, 8, 8))
        );
        coordPanelLayout.setVerticalGroup(
            coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblGroupId)
                    .add(txtGroupId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArtifactId)
                    .add(txtArtifactId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblVersion)
                    .add(txtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblScope)
                    .add(comScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(176, Short.MAX_VALUE))
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.coordPanel.TabConstraints.tabTitle", new Object[] {}), coordPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchLabel.text", new Object[] {})); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.text", new Object[] {})); // NOI18N

        jLabel1.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel1.text", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel2.text", new Object[] {})); // NOI18N

        org.jdesktop.layout.GroupLayout resultsPanelLayout = new org.jdesktop.layout.GroupLayout(resultsPanel);
        resultsPanel.setLayout(resultsPanelLayout);
        resultsPanelLayout.setHorizontalGroup(
            resultsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 373, Short.MAX_VALUE)
        );
        resultsPanelLayout.setVerticalGroup(
            resultsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 199, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                        .add(searchLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, searchField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)))
                    .add(jLabel2))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchPanel.TabConstraints.tabTitle", new Object[] {}), searchPanel); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 405, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comScope;
    private javax.swing.JPanel coordPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblScope;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
    
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

    private static class QueryPanel extends JPanel implements ExplorerManager.Provider {

        private BeanTreeView btv;
        private ExplorerManager manager;

        private static final Object LOCK = new Object();
        private boolean queryInProgress = false;
        private String lastQueryText;

        /** Creates new form FindResultsPanel */
        private QueryPanel() {
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            manager = new ExplorerManager();
            manager.setRootContext(new AbstractNode(Children.LEAF));
            add(btv, BorderLayout.CENTER);
        }

        void find(String queryText) {
            synchronized (LOCK) {
                if (queryInProgress) {
                    lastQueryText = queryText;
                    return;
                }
                queryInProgress = true;
                lastQueryText = null;
            }

            final List<QueryField> fields = new ArrayList<QueryField>();
            String q = queryText.trim();
            String[] splits = q.split(" "); //NOI118N

            for (String curText : splits) {
                QueryField f = new QueryField();
                f.setField(QueryField.FIELD_ANY);
                f.setValue(curText);
            }

            /*Node loadingNode = createLoadingNode();
            Children.Array array = new Children.Array();
            array.add(new Node[]{loadingNode});
            manager.setRootContext(new AbstractNode(array));*/
            Task t = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final List<NBVersionInfo> infos = RepositoryQueries.find(fields);
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            manager.setRootContext(createResultsNode(infos));
                        }
                    });
                }
            });
            
            t.addTaskListener(new TaskListener() {

                public void taskFinished(Task task) {
                    synchronized (LOCK) {
                        queryInProgress = false;
                        if (lastQueryText != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (lastQueryText != null) {
                                        find(lastQueryText);
                                    }
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

        private static Node createEmptyNode() {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                /*@Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/empty.png"); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }*/
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(QueryPanel.class, "LBL_Node_Empty"));
            return nd;
        }

        private Node createResultsNode(List<NBVersionInfo> infos) {
            Node node = null;
            Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

            for (NBVersionInfo nbvi : infos) {
                String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                List<NBVersionInfo> get = map.get(key);
                if (get == null) {
                    get = new ArrayList<NBVersionInfo>();
                    map.put(key, get);
                }
                get.add(nbvi);
            }
            Set<String> keySet = map.keySet();
            if (keySet.size() > 0) {
                Children.Array array = new Children.Array();

                List<String> keyList = new ArrayList<String>(keySet);
                Collections.sort(keyList);
                node = new AbstractNode(array);
                for (String key : keyList) {
                    array.add(new Node[]{new ArtifactNode(key, map.get(key))});
                }
            } else {
                Node empty = createEmptyNode();
                Children.Array array = new Children.Array();
                array.add(new Node[]{empty});
                node = new AbstractNode(array);
            }
            return node;

        }

        private Node createDepManagNode() {
            // TBD
            return null;
        }

        private static class ArtifactNode extends AbstractNode {

            private List<NBVersionInfo> versionInfos;

            public ArtifactNode(String name, final List<NBVersionInfo> list) {
                super(new Children.Keys<NBVersionInfo>() {
                    @Override
                    protected Node[] createNodes(NBVersionInfo arg0) {
                        return new Node[]{new VersionNode(arg0, arg0.isJavadocExists(),
                                    arg0.isSourcesExists())
                                };
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

            /*@Override
            public Image getIcon(int arg0) {
                Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/repository/ArtifactBadge.png", true); //NOI18N

                return badge;
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return getIcon(arg0);
            }*/

            public List<NBVersionInfo> getVersionInfos() {
                return new ArrayList<NBVersionInfo>(versionInfos);
            }
        }

        private static class VersionNode extends AbstractNode {

            private NBVersionInfo nbvi;
            private boolean hasJavadoc;
            private boolean hasSources;

            /** Creates a new instance of VersionNode */
            public VersionNode(NBVersionInfo versionInfo, boolean javadoc, boolean source) {
                super(Children.LEAF);

                hasJavadoc = javadoc;
                hasSources = source;
                this.nbvi = versionInfo;

                setName(versionInfo.getVersion());
                setDisplayName(versionInfo.getVersion() + " [ " + versionInfo.getType() + (versionInfo.getClassifier() != null ? ("," + versionInfo.getClassifier()) : "") + " ] " + " - " + versionInfo.getRepoId()); //NOI18N

                // setIconBaseWithExtension("org/netbeans/modules/maven/repository/DependencyJar.gif"); //NOI18N

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

    } // QueryPanel

}
