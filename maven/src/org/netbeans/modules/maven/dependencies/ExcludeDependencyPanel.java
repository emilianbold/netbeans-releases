/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class ExcludeDependencyPanel extends javax.swing.JPanel {
    private MavenProject project;
    private DependencyNode rootnode;
    private Map<Artifact, TreeModel> modelCache;
    private Map<ChangeListener, CheckNode> change2Trans;
    private Map<ChangeListener, List<CheckNode>> change2Refs;
    private boolean isSingle = false;

    /** Creates new form ExcludeDependencyPanel */
    public ExcludeDependencyPanel(MavenProject prj, final Artifact single) {
        project = prj;
        modelCache = new HashMap<Artifact, TreeModel>();
        change2Trans = new HashMap<ChangeListener, CheckNode>();
        change2Refs = new HashMap<ChangeListener, List<CheckNode>>();
        initComponents();
        isSingle = single != null;
        if (isSingle) {
            trTrans.setVisible(false);
            jScrollPane1.setVisible(false);
            jLabel1.setVisible(false);
        }
//        ToolTipManager.sharedInstance().registerComponent(trRef);
        trRef.setCellRenderer(new CheckRenderer(false));
        trTrans.setCellRenderer(new CheckRenderer(true));
        CheckNodeListener l = new CheckNodeListener(false);
        trRef.addMouseListener(l);
        trRef.addKeyListener(l);
        trRef.setToggleClickCount(0);
        trRef.setRootVisible(false);
        trTrans.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
        trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                rootnode = DependencyTreeFactory.createDependencyTree(project, EmbedderFactory.getOnlineEmbedder(), "test");
                if (!isSingle) {
                    trTrans.setModel(new DefaultTreeModel(createTransitiveDependenciesList()));
                } else {
                    CheckNode nd = new CheckNode(single, null, null);
                    setReferenceTree(nd);
                }
            }            
        });

    }

    public Map<Artifact, List<DependencyNode>> getDependencyExcludes() {
        Map<Artifact, List<DependencyNode>> toRet = new HashMap<Artifact, List<DependencyNode>>();
        for (ChangeListener list : change2Trans.keySet()) {
            CheckNode trans = change2Trans.get(list);
            List<CheckNode> refs = change2Refs.get(list);
            List<DependencyNode> nds = new ArrayList<DependencyNode>();
            for (CheckNode ref : refs) {
                if (ref.isSelected()) {
                    nds.add((DependencyNode)ref.getUserObject());
                }
            }
            toRet.put((Artifact)trans.getUserObject(), nds);
        }
        return toRet;
    }

    private TreeNode createReferenceModel(Set<DependencyNode> nds, CheckNode trans) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        ChangeListener list = new Listener();
        List<CheckNode> s = new ArrayList<CheckNode>();
        Icon icn = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/DependencyIcon.png", true));
        change2Trans.put(list, trans);
        change2Refs.put(list, s);
        for (DependencyNode nd : nds) {
            String label = nd.getArtifact().getGroupId() + ":" + nd.getArtifact().getArtifactId();
            CheckNode child = new CheckNode(nd, label, icn);
            child.setSelected(isSingle);
            child.addChangeListener(list);
            s.add(child);
            root.add(child);
        }
        return root;
    }

    private TreeNode createTransitiveDependenciesList() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = project.getArtifacts();
        Icon icn = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/TransitiveDependencyIcon.png", true));
        for (Artifact a : artifacts) {
            if (a.getDependencyTrail().size() > 2) {
                String label = a.getGroupId() + ":" + a.getArtifactId();
                root.add(new CheckNode(a, label, icn));
            }
        }
        return root;
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
        trTrans = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        trRef = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        trTrans.setRootVisible(false);
        trTrans.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                trTransValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(trTrans);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        trRef.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        trRef.setRootVisible(false);
        jScrollPane2.setViewportView(trRef);

        jLabel1.setText(org.openide.util.NbBundle.getBundle(ExcludeDependencyPanel.class).getString("ExcludeDependencyPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getBundle(ExcludeDependencyPanel.class).getString("ExcludeDependencyPanel.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                    .add(jScrollPane1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setReferenceTree(CheckNode mtb) {
        Artifact art = (Artifact) mtb.getUserObject();
        if (modelCache.containsKey(art)) {
            trRef.setModel(modelCache.get(art));
        } else {
            if (rootnode == null) {
                trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            } else {
                DependencyExcludeNodeVisitor nv = new DependencyExcludeNodeVisitor(art.getGroupId(), art.getArtifactId(), art.getType());
                rootnode.accept(nv);
                Set<DependencyNode> nds = nv.getDirectDependencies();
                DefaultTreeModel dtm = new DefaultTreeModel(createReferenceModel(nds, mtb));
                trRef.setModel(dtm);
                modelCache.put(art, dtm);
            }
        }
    }

    private void trTransValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_trTransValueChanged
        TreeNode tn = (TreeNode) evt.getPath().getLastPathComponent();
        if (tn instanceof CheckNode) {
            setReferenceTree((CheckNode)tn);
        } else {
            trRef.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
        }

    }//GEN-LAST:event_trTransValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree trRef;
    private javax.swing.JTree trTrans;
    // End of variables declaration//GEN-END:variables


    private class Listener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            CheckNode trans = change2Trans.get(this);
            List<CheckNode> refs = change2Refs.get(this);
            boolean all = true;
            boolean some = false;
            for (CheckNode ref : refs) {
                if (!ref.isSelected()) {
                    all = false;
                }
                if (ref.isSelected()) {
                    some = true;
                }
            }
            if (all) {
                //competely gone.. -> strikethrough
                trans.strike();
            } else {
                trans.unstrike();
            }
            if (some) {
                trans.italic();
            } else {
                trans.unitalic();
            }
            if (trTrans.isVisible()) {
                trTrans.repaint();
            }
        }

    }

}
