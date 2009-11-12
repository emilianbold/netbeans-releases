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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
final class ProjectRootNodeChildren extends ChildFactory.Detachable<ChildKind> implements LookupListener, ChangeListener, Runnable {

    private final J2MEProject project;
    private volatile Lookup.Result<NodeFactory> res;
    static final String FOREIGN_NODES_PATH =
            "Projects/org-netbeans-modules-mobility-project/Nodes"; //NOI18N
    private Set<NodeList> lists = new HashSet<NodeList>();
    private final Object lock = new Object();
    static final Logger LOGGER = Logger.getLogger(ProjectRootNodeChildren.class.getName());

    ProjectRootNodeChildren(J2MEProject project) {
        this.project = project;
    }

    @Override
    protected void addNotify() {
        cycle = false;
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "J2ME Project Root Node Children for " + //NOI18N
                    projectString() + " addNotify()"); //NOI18N
        }
        Lookup.Result result = Lookups.forPath(FOREIGN_NODES_PATH).lookupResult(NodeFactory.class);
        synchronized (lock) {
            res = result;
            res.addLookupListener(this);
        }
    }

    @Override
    protected void removeNotify() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "J2ME Project Root Node Children for " + //NOI18N
                    projectString() + " removeNotify()"); //NOI18N
        }
        Set<NodeList> s;
        synchronized (lock) {
            assert res != null : "removeNotify called twice or w/o addNotify()"; //NOI18N
            res.removeLookupListener(this);
            res = null;
            s = new HashSet<NodeList>(lists);
            lists.clear();
        }
        for (NodeList l : s) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "J2ME Project Root Node Children for " + //NOI18N
                        projectString() + " detach " + //NOI18N
                        "listener from " + l); //NOI18N
            }
            l.removeChangeListener(this);
            l.removeNotify();
        }
      }

    volatile boolean cycle = false;
    protected boolean createKeys(List<ChildKind> toPopulate) {
        if (cycle) {
            //Issue #175202 refresh doesn't actually change key list,
            //so nodes are not updated.  So instead, we force the keys
            //to empty, and then refill them
            RequestProcessor.getDefault().post(this, 150);
            cycle = false;
            return true;
        }
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
        Lookup.Result<NodeFactory> lkpResult;
        synchronized (lock) {
            lkpResult = res;
        }
        if (lkpResult == null) {
            //removeNotify called while background thread still fetching nodes,
            //just exit, result won't be shown anyway
            return new Node[0];
        }
        List<Node> nodes = new LinkedList<Node>();
        Set<NodeList> found = new HashSet<NodeList>();
        final boolean log = LOGGER.isLoggable(Level.FINEST);

        for (NodeFactory f : lkpResult.allInstances()) {
            NodeList list = f.createNodes(project);
            list.addNotify();
            list.addChangeListener(this);
            for (Object key : list.keys()) {
                Node foreignNode = list.node(key);
                nodes.add(foreignNode);
                if (log) {
                    LOGGER.log(Level.FINEST, list + " provides foreign node " + //NOI18N
                            foreignNode + " for " + //NOI18N
                            projectString());
                }
            }
        }
        synchronized (lock) {
            lists.clear();
            lists.addAll (found);
        }
        Node[] result = nodes.toArray(new Node[nodes.size()]);
        if (LOGGER.isLoggable(Level.FINE) && nodes.size() > 0) {
            LOGGER.log(Level.FINE, "Foreign nodes created for J2ME Project " + //NOI18N
                    projectString() + ": " + //NOI18N
                    nodes);
        } else if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "No foreign nodes for J2ME Project " + //NOI18N
                    projectString()); //NOI18N
        }
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
        cycle = true;
        refresh(false);
    }

    public void stateChanged(ChangeEvent e) {
        cycle = true;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Received state change from " + e.getSource() //NOI18N
                    + " refereshing child nodes"); //NOI18N
        }
        refresh (true);
    }

    private String projectString() {
        //Project will be null for unit tests
        return project == null ? "null" : //NOI18N
            project.getProjectDirectory().getPath();
    }

    public void run() {
        cycle = false;
        refresh(true);
    }
}
