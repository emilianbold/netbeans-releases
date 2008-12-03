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
package org.netbeans.modules.mobility.project.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.api.mobility.project.ChildKind;
import org.netbeans.api.mobility.project.ProjectChildKeyProvider;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
final class ProjectRootNodeChildren extends ChildFactory.Detachable<ChildKind> implements LookupListener, ChangeListener {

    private final J2MEProject project;
    private Lookup.Result<NodeFactory> res;
    private static final String FOREIGN_NODES_PATH =
            "Projects/org-netbeans-modules-mobility-project/Nodes"; //NOI18N
    private Set<NodeList> lists = new HashSet<NodeList>();
    private final Object lock = new Object();

    ProjectRootNodeChildren(J2MEProject project) {
        this.project = project;
    }

    @Override
    protected void addNotify() {
        res = Lookups.forPath(FOREIGN_NODES_PATH).lookupResult(NodeFactory.class);
        res.addLookupListener(this);
    }

    @Override
    protected void removeNotify() {
        res = null;
        Set<NodeList> s;
        synchronized (lock) {
            s = new HashSet<NodeList>(lists);
            lists.clear();
        }
        for (NodeList l : s) {
            l.removeChangeListener(this);
            l.removeNotify();
        }
      }
    
    protected boolean createKeys(List<ChildKind> toPopulate) {
        ProjectChildKeyProvider provider = Lookup.getDefault().lookup(
                ProjectChildKeyProvider.class);
        if (provider == null) {
            toPopulate.addAll(Arrays.asList(ChildKind.values()));
        } else {
            toPopulate.addAll(provider.getKeys());
        }
        return true;
    }

    @Override
    protected Node[] createNodesForKey(ChildKind key) {
        switch (key) {
            case Configurations:
                return new Node[]{createConfigurationsNode()};
            case Resources:
                return new Node[]{createResourcesNode()};
            case Sources:
                return createSourcesNodes();
            case Foreign :
                return createForeignNodes();
            default:
                throw new AssertionError();
        }
    }

    private Node[] createForeignNodes() {
        List<Node> nodes = new LinkedList<Node>();
        Set<NodeList> found = new HashSet<NodeList>();
        for (NodeFactory f : res.allInstances()) {
            NodeList list = f.createNodes(project);
            list.addNotify();
            list.addChangeListener(this);
            for (Object key : list.keys()) {
                nodes.add(list.node(key));
            }
        }
        synchronized (lock) {
            lists.clear();
            lists.addAll (found);
        }
        Node[] result = nodes.toArray(new Node[nodes.size()]);
        return result;
    }

    private Node createConfigurationsNode() {
        return new ConfigurationsNode (project);
    }

    private Node createResourcesNode() {
        return new ResourcesNode(project, null);
    }

    private Node[] createSourcesNodes() {
        Node[] result = new Node[0];
        final Sources src = ProjectUtils.getSources(project);
        if (src != null) {
            final SourceGroup sg[] = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            result = new Node[sg.length];
            int ix = 0;
            //in preparation for multiple source roots
            for (SourceGroup group : sg) {
                result[ix++] = PackageView.createPackageView(group);
            }
        }
        if (result.length == 0) {
            result = new Node[] { new AbstractNode(Children.LEAF) };
            result[0].setDisplayName(NbBundle.getMessage(ProjectRootNodeChildren.class,
                    "LBL_MissingSources")); //NOI18N
        }
        return result;
    }

    public void resultChanged(LookupEvent ev) {
        refresh(false);
    }

    public void stateChanged(ChangeEvent e) {
        refresh (true);
    }
}
