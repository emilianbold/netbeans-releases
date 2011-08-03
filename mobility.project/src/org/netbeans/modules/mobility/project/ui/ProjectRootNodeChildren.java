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
package org.netbeans.modules.mobility.project.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
final class ProjectRootNodeChildren extends ChildFactory.Detachable<Object> implements LookupListener, ChangeListener, Runnable {
    private final Map<Object, NodeList> nodeListForKey = Collections.synchronizedMap(new HashMap<Object, NodeList>());
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
            // #200543
            if (res != null) {
                res.removeLookupListener(this);
            } else {
                LOGGER.log(Level.SEVERE, "removeNotify called twice or w/o addNotify()!"); //NOI18N
            }
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
        nodeListForKey.clear();
      }

    volatile boolean cycle = false;
    protected boolean createKeys(List<Object> toPopulate) {
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
        boolean addForeignNodes;
        if (provider == null) {
            toPopulate.addAll(Arrays.asList(ChildKind.values()));
            addForeignNodes = true;
        } else {
            toPopulate.addAll(provider.getKeys());
            addForeignNodes = toPopulate.contains(ChildKind.Foreign);
        }
        if (addForeignNodes) {
            addForeignNodeKeys(toPopulate);
        }
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {
        if (key instanceof ChildKind) {
            switch ((ChildKind) key) {
                case Configurations:
                    return new Node[]{createConfigurationsNode()};
                case Resources:
                    return new Node[]{createResourcesNode()};
                case Sources:
                    return createSourcesNodes();
                case Foreign :
                    return new Node[0];
                default:
                    throw new AssertionError();
            }
        } else {
            NodeList nl = nodeListForKey.get(key);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Foreign nodes created for J2ME Project " + //NOI18N
                        projectString() + ": " + //NOI18N
                        key);
            }
            if (nl != null) {
                return new Node[] { nl.node(key) };
            } else {
                //removeNotify was called on another thread while we
                //were iterating, and our keys-to-lists map was cleared
                return new Node[0];
            }
        }
    }

    void addForeignNodeKeys (List<Object> toPopulate) {
        Lookup.Result<NodeFactory> lkpResult;
        synchronized (lock) {
            lkpResult = res;
        }
        if (lkpResult == null) {
            //removeNotify called while background thread still fetching nodes,
            //just exit, result won't be shown anyway
            return;
        }
        nodeListForKey.clear();
        Set<NodeList> found = new HashSet<NodeList>();
        synchronized(lock) {
            found.addAll(lists);
        }
        Set<NodeList> toRemove = new HashSet<NodeList>(lists);
        for (NodeFactory f : lkpResult.allInstances()) {
            NodeList nl = f.createNodes(project);
            if (!found.contains(nl)) {
                found.add(nl);
                nl.addNotify();
                nl.addChangeListener(this);
                toRemove.remove(nl);
            }
            for (Object o : nl.keys()) {
                nodeListForKey.put (o, nl);
                toPopulate.add(o);
            }
        }
        synchronized(lock) {
            for (NodeList nl : toRemove) {
                nl.removeChangeListener(this);
                nl.removeNotify();
            }
            lists.removeAll(toRemove);
            lists.addAll(found);
        }
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
