/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.QueryRequest;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author  mkleint
 */
public class FindResultsPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Observer {

    private BeanTreeView btv;
    private ExplorerManager manager;
    private ActionListener close;
    private DialogDescriptor dd;
    
    private QueryRequest queryRequest;
    private ResultsRootNode resultsRootNode;
    
    private static final RequestProcessor queryRP = new RequestProcessor(FindResultsPanel.class.getName(), 10);

    private FindResultsPanel() {
        initComponents();
        btv = new BeanTreeView();
        btv.setRootVisible(false);
        manager = new ExplorerManager();
        queryRequest = null;
        resultsRootNode = new ResultsRootNode();
        manager.setRootContext(resultsRootNode);
        add(btv, BorderLayout.CENTER);
    }

    FindResultsPanel(ActionListener actionListener, DialogDescriptor d) {
        this();
        close = actionListener;
        dd = d;
    }

    void find(final List<QueryField> fields) {

        if (null != queryRequest) {
            queryRequest.deleteObserver(this);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resultsRootNode.setOneChild(getSearchingNode());
            }
        });

        queryRequest = new QueryRequest(fields, RepositoryPreferences.getInstance().getRepositoryInfos(), this);
        
        queryRP.post(new Runnable() {

            public void run() {
                try {
                    RepositoryQueries.find(queryRequest);
                } catch (BooleanQuery.TooManyClauses exc) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            resultsRootNode.setOneChild(getTooGeneralNode());
                        }
                    });
                } catch (final OutOfMemoryError oome) {
                    // running into OOME may still happen in Lucene despite the fact that
                    // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                    // (see #190265)
                    // in the bad circumstances theoretically any thread may encounter OOME
                    // but most probably this thread will be it
                    // trying to indicate the condition to the user here
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            resultsRootNode.setOneChild(getTooGeneralNode());
                        }
                    });
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnModify = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnModify, org.openide.util.NbBundle.getMessage(FindResultsPanel.class, "FindResultsPanel.btnModify.text")); // NOI18N
        btnModify.setFocusable(false);
        btnModify.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnModify.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnModify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModifyActionPerformed(evt);
            }
        });
        jToolBar1.add(btnModify);

        org.openide.awt.Mnemonics.setLocalizedText(btnClose, org.openide.util.NbBundle.getMessage(FindResultsPanel.class, "FindResultsPanel.btnClose.text")); // NOI18N
        btnClose.setFocusable(false);
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        jToolBar1.add(btnClose);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
    if (null != queryRequest) {
        queryRequest.deleteObserver(this);
    }
    if (close != null) {
        close.actionPerformed(evt);
    }
}//GEN-LAST:event_btnCloseActionPerformed

private void btnModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyActionPerformed
    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (ret == DialogDescriptor.OK_OPTION) {
        find(((FindInRepoPanel) dd.getMessage()).getQuery());
    }
}//GEN-LAST:event_btnModifyActionPerformed

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (null == o || !(o instanceof QueryRequest)) {
            return;
        }

        List<NBVersionInfo> infos = ((QueryRequest) o).getResults();

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
        Collections.sort(keyList);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateResultNodes(keyList, map);
            }
        });
    }

    private void updateResultNodes(List<String> keyList, Map<String, List<NBVersionInfo>> map) {

            if (keyList.size() > 0) { // some results available
                
                Map<String, Node> currentNodes = new HashMap<String, Node>();
                for (Node nd : resultsRootNode.getChildren().getNodes()) {
                    currentNodes.put(nd.getName(), nd);
                }
                List<Node> newNodes = new ArrayList<Node>(keyList.size());

                // still searching?
                if (null!=queryRequest && !queryRequest.isFinished())
                    newNodes.add(getSearchingNode());
                
                for (String key : keyList) {
                    Node nd;
                    nd = currentNodes.get(key);
                    if (null != nd) {
                        ((ArtifactNode)nd).setVersionInfos(map.get(key));
                    } else {
                        nd = new ArtifactNode(key, map.get(key));
                    }
                    newNodes.add(nd);
                }
                
                resultsRootNode.setNewChildren(newNodes);
            } else if (null!=queryRequest && !queryRequest.isFinished()) { // still searching, no results yet
                resultsRootNode.setOneChild(getSearchingNode());
            } else { // finished searching with no results
                resultsRootNode.setOneChild(getNoResultsNode());
            }
    }

    private static class ArtifactNode extends AbstractNode {

        private List<NBVersionInfo> versionInfos;
        private ArtifactNodeChildren myChildren;

        public ArtifactNode(String name, List<NBVersionInfo> list) {
            super(new ArtifactNodeChildren(list));
            myChildren = (ArtifactNodeChildren)getChildren();
            this.versionInfos=list;
            setName(name);
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int arg0) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/repository/ArtifactBadge.png", true); //NOI18N

            return badge;
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return getIcon(arg0);
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[0];
        }

        public List<NBVersionInfo> getVersionInfos() {
            return new ArrayList<NBVersionInfo>(versionInfos);
        }

        public void setVersionInfos(List<NBVersionInfo> infos) {
            versionInfos = infos;
            myChildren.setNewKeys(infos);
        }

        static class ArtifactNodeChildren extends Children.Keys<NBVersionInfo> {

            private List<NBVersionInfo> keys;

            public ArtifactNodeChildren(List<NBVersionInfo> keys) {
                this.keys = keys;
            }

            @Override
            protected Node[] createNodes(NBVersionInfo info) {
                RepositoryInfo rinf = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
                return new Node[]{new VersionNode(rinf, info, info.isJavadocExists(),
                            info.isSourcesExists(), true)
                        };
            }

            @Override
            protected void addNotify() {
                setKeys(keys);
            }

            protected void setNewKeys(List<NBVersionInfo> keys) {
                this.keys = keys;
                setKeys(keys);
            }
        }
    }

    private static Node noResultsNode, searchingNode, tooGeneralNode;

    private static Node getNoResultsNode() {
        if (noResultsNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/empty.png"); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(FindResultsPanel.class, "LBL_Node_Empty")); //NOI18N

            noResultsNode = nd;
        }

        return new FilterNode (noResultsNode, Children.LEAF);
    }

    private static Node getSearchingNode() {
        if (searchingNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/wait.gif"); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Searching"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(FindResultsPanel.class, "LBL_Node_Searching")); //NOI18N

            searchingNode = nd;
        }

        return new FilterNode (searchingNode, Children.LEAF);
    }

    private static Node getTooGeneralNode() {
        if (tooGeneralNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/empty.png"); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Too General"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(FindResultsPanel.class, "LBL_Node_TooGeneral")); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode (tooGeneralNode, Children.LEAF);
    }

    private class ResultsRootNode extends AbstractNode {

        private ResultsRootChildren resultsChildren;

        public ResultsRootNode() {
            this(new InstanceContent());
        }

        private ResultsRootNode(InstanceContent content) {
            super (new ResultsRootChildren(), new AbstractLookup(content));
            content.add(this);
            this.resultsChildren = (ResultsRootChildren) getChildren();
        }

        public void setOneChild(Node n) {
            List<Node> ch = new ArrayList<Node>(1);
            ch.add(n);
            setNewChildren(ch);
        }
        
        public void setNewChildren(List<Node> ch) {
            resultsChildren.setNewChildren (ch);
        }
    }
    
    private class ResultsRootChildren extends Children.Keys<Node> {
        
        List<Node> myNodes;

        public ResultsRootChildren() {
            myNodes = Collections.EMPTY_LIST;
        }

        private void setNewChildren(List<Node> ch) {
            myNodes = ch;
            refreshList();
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        private void refreshList() {
            List<Node> keys = new ArrayList();
            for (Node node : myNodes) {
                keys.add(node);
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[] { key };
        }

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnModify;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
