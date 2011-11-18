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

import java.awt.Image;
import java.io.IOException;
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
import org.netbeans.modules.maven.indexer.api.QueryRequest;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import static org.netbeans.modules.maven.repository.Bundle.*;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  mkleint
 */
public class FindResultsNode extends AbstractNode {

    private final QueryRequest queryRequest;
    
    private static final RequestProcessor queryRP = new RequestProcessor(FindResultsNode.class.getName(), 10);
    FindResultsNode(QueryRequest queryRequest) {
        super(Children.create(new FindResultsChildren(queryRequest), true));
        this.queryRequest = queryRequest;
        setDisplayName(queryRequest.getQueryFields().get(0).getValue());
        setIconBaseWithExtension("org/netbeans/modules/maven/repository/FindInRepo.png");
    }

    @Override public boolean canDestroy() {
        return true;
    }

    @Override public void destroy() throws IOException {
        M2RepositoryBrowser.remove(queryRequest);
    }

    @Override public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(DeleteAction.class)};
    }

    // XXX clumsy, use a real key instead (NBGroupInfo?) and replace no results/too general nodes with status line notifications
    private static class FindResultsChildren extends ChildFactory.Detachable<Node> implements Observer {

        private final QueryRequest queryRequest;
        private List<Node> nodes;

        FindResultsChildren(QueryRequest queryRequest) {
            this.queryRequest = queryRequest;
        }

        @Override protected Node createNodeForKey(Node key) {
            return key;
        }

        @Override protected void addNotify() {
            queryRequest.addObserver(this);
        }

        @Override protected void removeNotify() {
            queryRequest.deleteObserver(this);
        }

        @Override protected boolean createKeys(List<Node> toPopulate) {
            if (nodes != null) {
                toPopulate.addAll(nodes);
            } else {
        queryRP.post(new Runnable() {

            public void run() {
                try {
                    RepositoryQueries.find(queryRequest);
                } catch (BooleanQuery.TooManyClauses exc) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            nodes = Collections.singletonList(getTooGeneralNode());
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
                            nodes = Collections.singletonList(getTooGeneralNode());
                        }
                    });
                }
            }
        });
    }
            return true; // XXX queryRequest.isFinished() unsuitable here
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
                if (nodes != null) {
                for (Node nd : nodes) {
                    currentNodes.put(nd.getName(), nd);
                }
                }
                List<Node> newNodes = new ArrayList<Node>(keyList.size());

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
                
                nodes = newNodes;
                refresh(false);
            } else if (null!=queryRequest && !queryRequest.isFinished()) { // still searching, no results yet
                nodes = Collections.emptyList();
            } else { // finished searching with no results
                nodes = Collections.singletonList(getNoResultsNode());
            }
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

    private static Node noResultsNode, tooGeneralNode;

    @Messages("LBL_Node_Empty=No matching items")
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

            nd.setDisplayName(LBL_Node_Empty()); //NOI18N

            noResultsNode = nd;
        }

        return new FilterNode (noResultsNode, Children.LEAF);
    }

    @Messages("LBL_Node_TooGeneral=Too general query")
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

            nd.setDisplayName(LBL_Node_TooGeneral()); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode (tooGeneralNode, Children.LEAF);
    }

    
}
