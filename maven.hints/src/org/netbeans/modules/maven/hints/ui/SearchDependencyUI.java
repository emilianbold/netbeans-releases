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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.hints.ui;

import java.awt.Image;
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
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.model.Dependency;
import org.netbeans.modules.maven.hints.ui.nodes.ArtifactNode;
import org.netbeans.modules.maven.hints.ui.nodes.VersionNode;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author  Anuradha G
 */
public class SearchDependencyUI extends javax.swing.JPanel implements ExplorerManager.Provider {

    private ExplorerManager explorerManager = new ExplorerManager();
    private JButton addButton = new JButton(NbBundle.getMessage(SearchDependencyUI.class, "BTN_Add"));
    private BeanTreeView beanTreeView;
    private NBVersionInfo nbvi;
    private RequestProcessor.Task task;
    private boolean retrigger = false;
    private Project project;
    /** Creates new form SearchDependencyUI */
    public SearchDependencyUI(String clazz, Project mavProj) {
        initComponents();
        project = mavProj;
        beanTreeView = (BeanTreeView) treeView;
        beanTreeView.setPopupAllowed(false);
        beanTreeView.setRootVisible(false);
        addButton.setEnabled(false);

        txtClassName.setText(clazz);
        txtClassName.selectAll();
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent arg0) {
                if (arg0.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {//NOI18N

                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                   
                    for (Node node : selectedNodes) {
                        if (node instanceof VersionNode) {

                            nbvi=((VersionNode) node).getNBVersionInfo();
        
                            
                            break;

                        }else if(node instanceof ArtifactNode){
                            NBVersionInfo info=null;
                            ArtifactNode an=(ArtifactNode) node;
                            List<NBVersionInfo> infos = an.getVersionInfos();
                            for (NBVersionInfo nbvi : infos) {
                                if(info==null || nbvi.getVersion().compareTo(info.getVersion())>0){
                                
                                  info=nbvi;
                                }
                            }
                            nbvi=info;
                        }
                    }
                    if(nbvi!=null){
                     lblSelected.setText(nbvi.getGroupId()+" : "+nbvi.getArtifactId()
                             +" : "+nbvi.getVersion()+ " [ " + nbvi.getType() 
                             + (nbvi.getClassifier() != null ? ("," + nbvi.getClassifier()) : "")+" ]");
                    }else{
                     lblSelected.setText(null);
                    }
                    addButton.setEnabled(nbvi!=null);

                }
            }
        });
        explorerManager.setRootContext(createEmptyNode());
        load();
        txtClassName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                load();
            }

            public void removeUpdate(DocumentEvent e) {
                load();
            }

            public void changedUpdate(DocumentEvent e) {
                load();
            }
            
        });
    }

    public NBVersionInfo getSelectedVersion() {
        
        return nbvi;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public synchronized void load() {
        if (task == null) {
            task = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    final String[] search = new String[1];
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                lblSelected.setText(null);
                                beanTreeView.setRootVisible(true);
                                search[0] = getClassSearchName();
//for debugging purposes only lblMatchingArtifacts.setText(search[0]);
                            }
                        });
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    
                    Node node;
                    boolean visible = false;
                    explorerManager.setRootContext(createLoadingNode());
                    if (search[0].length() > 0) {
                        List<NBVersionInfo> infos = RepositoryQueries.findVersionsByClass(search[0]);
                        //the actual lucene query takes much longer than our queue
                        // timeout, we should not start new tasks until this one is
                        //finished.. and this one should either finish with correct data
                        // or immediately retrigger a new search.
                        synchronized (SearchDependencyUI.this) {
                            if (retrigger) {
                                retrigger = false;
                                task.schedule(20);
                                return;
                            }
                        }
                        Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

                        for (NBVersionInfo nbvi : infos) {
                            String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId();
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
                            node = new AbstractNode(array);
                            List<String> keyList = new ArrayList<String>(keySet);
                            Collections.sort(keyList, new HeuristicsComparator());
                            for (String key : keyList) {
                                array.add(new Node[]{new ArtifactNode(key, map.get(key))});
                            }
                            visible = false;
                        } else {
                            node = createEmptyNode();
                            visible = true;
                        }
                    } else {
                        node = createEmptyNode();
                        visible = true;
                    }
                    final Node fNode = node;
                    final boolean fVisible = visible;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            beanTreeView.setRootVisible(fVisible);
                            explorerManager.setRootContext(fNode);
                        }
                    });
                    
                }
            }, true);
        }
        if (!task.isFinished() && task.getDelay() == 0) {
            retrigger = true;
            //if running, just flag the 'retrigger' variable,
            //chances are the task is currently doing a lucene search..
        } else {
            task.schedule(500);
        }
    }

    public String getClassSearchName() {
        return txtClassName.getText().trim();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblClassName = new javax.swing.JLabel();
        txtClassName = new javax.swing.JTextField();
        treeView = new BeanTreeView();
        lblMatchingArtifacts = new javax.swing.JLabel();
        lblSelected = new javax.swing.JLabel();

        lblClassName.setText(org.openide.util.NbBundle.getMessage(SearchDependencyUI.class, "LBL_Class_Name")); // NOI18N

        treeView.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, javax.swing.UIManager.getDefaults().getColor("ComboBox.selectionBackground")));

        lblMatchingArtifacts.setText(org.openide.util.NbBundle.getMessage(SearchDependencyUI.class, "LBL_Matching_artifacts")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblSelected, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .add(txtClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblMatchingArtifacts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblClassName)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblMatchingArtifacts)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(treeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblSelected, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblClassName;
    private javax.swing.JLabel lblMatchingArtifacts;
    private javax.swing.JLabel lblSelected;
    private javax.swing.JScrollPane treeView;
    private javax.swing.JTextField txtClassName;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public static Node createLoadingNode() {
        AbstractNode nd = new AbstractNode(Children.LEAF) {

            @Override
            public Image getIcon(int arg0) {
                return Utilities.loadImage("org/netbeans/modules/maven/hints/wait.gif");
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return getIcon(arg0);
            }
        };
        nd.setName("Loading"); //NOI18N

        nd.setDisplayName(NbBundle.getMessage(SearchDependencyUI.class, "Node_Loading"));
        return nd;
    }

    public static Node createEmptyNode() {
        AbstractNode nd = new AbstractNode(Children.LEAF) {

            @Override
            public Image getIcon(int arg0) {
                return Utilities.loadImage("org/netbeans/modules/maven/hints/empty.png");
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return getIcon(arg0);
            }
        };
        nd.setName("Empty"); //NOI18N

        nd.setDisplayName(NbBundle.getMessage(SearchDependencyUI.class, "Node_Empty"));
        return nd;
    }
    
    //TODO
    // for netbeans projects, org.netbeans.api is the prefered item in the list
    // for web/ejb/ear projects, javax.* are probably preferred.
    // possibly items from groupids that are already present in the pom should also be
    // put up front.
    private class HeuristicsComparator implements Comparator<String> {
        private Set<String> privilegedGroupIds = new HashSet<String>();
        
        private HeuristicsComparator() {
            String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
            if (NbMavenProject.TYPE_NBM.equalsIgnoreCase(packaging)) {
                privilegedGroupIds.add("org.netbeans.api"); //NOI18N
            }
            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(packaging) || 
                NbMavenProject.TYPE_EAR.equalsIgnoreCase(packaging) || 
                NbMavenProject.TYPE_EJB.equalsIgnoreCase(packaging)) {
                privilegedGroupIds.add("javax.activation");//NOI18N
                privilegedGroupIds.add("javax.ejb");//NOI18N
                privilegedGroupIds.add("javax.faces");//NOI18N
                privilegedGroupIds.add("javax.j2ee");//NOI18N
                privilegedGroupIds.add("javax.jdo");//NOI18N
                privilegedGroupIds.add("javax.jms");//NOI18N
                privilegedGroupIds.add("javax.mail");//NOI18N
                privilegedGroupIds.add("javax.management");//NOI18N
                privilegedGroupIds.add("javax.naming");//NOI18N
                privilegedGroupIds.add("javax.persistence");//NOI18N
                privilegedGroupIds.add("javax.portlet");//NOI18N
                privilegedGroupIds.add("javax.resource");//NOI18N
                privilegedGroupIds.add("javax.security");//NOI18N
                privilegedGroupIds.add("javax.servlet");//NOI18N
                privilegedGroupIds.add("javax.sql");//NOI18N
                privilegedGroupIds.add("javax.transaction");//NOI18N
                privilegedGroupIds.add("javax.xml");//NOI18N
            }
            //TODO add some more heuristics
            NbMavenProject mavenproject = project.getLookup().lookup(NbMavenProject.class);
            List<Dependency> deps = mavenproject.getMavenProject().getDependencies();
            for (Dependency d : deps) {
                privilegedGroupIds.add(d.getGroupId());
            }
        }

        public int compare(String s1, String s2) {
            String[] split1 = s1.split(":");
            String[] split2 = s2.split(":");
            boolean b1 = privilegedGroupIds.contains(split1[0].trim());
            boolean b2 = privilegedGroupIds.contains(split2[0].trim());
            if (b1 && !b2) {
                return -1;
            }
            if (!b1 && b2) {
                return 1;
            }
            return s1.compareTo(s2);
        }
        
    }
            
}
