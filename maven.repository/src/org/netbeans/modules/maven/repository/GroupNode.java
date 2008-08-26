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
package org.netbeans.modules.maven.repository;

import java.awt.Image;
import java.util.Collections;

import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBGroupInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class GroupNode extends AbstractNode {
    private RepositoryInfo info;
    /** Creates a new instance of GroupNode */
    public GroupNode(RepositoryInfo info,String id) {
        super(new GroupChildren(info,id));
        this.info=info;
        setName(id);
        setDisplayName(id);
    }

    public GroupNode( final RepositoryInfo info,final NBGroupInfo groupInfo) {
        super(new Children.Keys<NBArtifactInfo>() {

            @Override
            protected Node[] createNodes(NBArtifactInfo arg0) {
                return new Node[]{new ArtifactNode(info,arg0)};
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                setKeys(groupInfo.getArtifactInfos());
            }
        });
        setName(groupInfo.getName());
        setDisplayName(groupInfo.getName());
    }

    static class GroupChildren extends Children.Keys {

        private String id;
        private RepositoryInfo info;
        /** Creates a new instance of GroupListChildren */
        public GroupChildren(RepositoryInfo info,String group) {
            this.info = info;
            id = group;
        }

        protected Node[] createNodes(Object key) {
            if (GroupListChildren.LOADING == key) {
                return new Node[]{GroupListChildren.createLoadingNode()};
            }
            String artifactId = (String) key;
            return new Node[]{new ArtifactNode(info,id, artifactId)};
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(Collections.singletonList(GroupListChildren.LOADING));
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    setKeys(RepositoryQueries.getArtifacts(id, info));
                }
            });
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            setKeys(Collections.EMPTY_LIST);
        }
    }

    @Override
    public Image getIcon(int arg0) {
        return NodeUtils.getTreeFolderIcon(false);
    }

    @Override
    public Image getOpenedIcon(int arg0) {
        return NodeUtils.getTreeFolderIcon(true);
    }
}
