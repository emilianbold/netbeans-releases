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
package org.netbeans.modules.mobility.project.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.actions.ContextAction;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

public abstract class NodeAction<T> extends ContextAction<Node> {

    protected FileObject defaultDir = null;

    protected NodeAction(String name) {
        super(Node.class);
        putValue(NAME, name);
    }

    private void perform(final T[] obj, final Node node, final J2MEProjectProperties j2meProperties) {
        final J2MEProject project = node.getLookup().lookup(J2MEProject.class);
        final ProjectConfiguration conf = node.getLookup().lookup(ProjectConfiguration.class);
        assert project != null;
        String propName;
        if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName())) {
            propName = DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        } else {
            propName = J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        }
        final List<VisualClassPathItem> list = (List) j2meProperties.get(propName);
        if (list == null) {
            throw new NullPointerException("Could not get a list of properties " + "for " + propName); //NO18N
        }
        List<VisualClassPathItem> newList = new ArrayList<VisualClassPathItem>(list);
        newList = addItems(obj, newList, node);
        j2meProperties.put(propName, newList);
    }

    private static void save(final HashMap<J2MEProject, J2MEProjectProperties> map) {
        // Store all properties after they are set for all nodes
        ProjectManager.mutex().writeAccess(new Runnable() {

            public void run() {
                for (final J2MEProject project : map.keySet()) {
                    final J2MEProjectProperties j2meProperties = map.get(project);
                    // Store the properties
                    j2meProperties.store();
                    // And save the project
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        });
    }

    public static void pasteAction(final HashSet<VisualClassPathItem> items, Node node) {
        final J2MEProject project = node.getLookup().lookup(J2MEProject.class);
        final ProjectConfiguration conf = node.getLookup().lookup(ProjectConfiguration.class);
        final HashMap<J2MEProject, J2MEProjectProperties> map = new HashMap<J2MEProject, J2MEProjectProperties>();
        assert project != null;
        ProjectManager.mutex().writeAccess(new Runnable() {

            public void run() {
                final J2MEProjectProperties j2meProperties = new J2MEProjectProperties(project, project.getLookup().lookup(AntProjectHelper.class), project.getLookup().lookup(ReferenceHelper.class), project.getConfigurationHelper());
                String propName;
                if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName())) {
                    propName = DefaultPropertiesDescriptor.LIBS_CLASSPATH;
                } else {
                    propName = J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.LIBS_CLASSPATH; //NO18N
                }
                final List<VisualClassPathItem> list = (List) j2meProperties.get(propName);
                final HashSet<VisualClassPathItem> set = new HashSet<VisualClassPathItem>(list);
                set.addAll(items);
                list.clear();
                list.addAll(set);
                j2meProperties.put(propName, list);
                map.put(project, j2meProperties);
                save(map);
            }
        });
    }

    protected synchronized void performAction(Node[] activatedNodes) {
        J2MEProject proj = activatedNodes[0].getLookup().lookup(J2MEProject.class);
        //Check if all items are from the same project
        for (Node node : activatedNodes) {
            final J2MEProject project = node.getLookup().lookup(J2MEProject.class);
            if (proj != project) {
                proj = null;
                break;
            }
        }
        if (proj != null) {
            defaultDir = proj.getProjectDirectory();
        } else {
            defaultDir = null;
        }
        final T[] obj = getItems();
        if (obj != null) {
            final HashMap<J2MEProject, J2MEProjectProperties> map = new HashMap<J2MEProject, J2MEProjectProperties>();
            for (Node node : activatedNodes) {
                final J2MEProject project = node.getLookup().lookup(J2MEProject.class);
                J2MEProjectProperties j2meProperties = map.get(project);
                if (j2meProperties == null) {
                    j2meProperties = new J2MEProjectProperties(project, project.getLookup().lookup(AntProjectHelper.class), project.getLookup().lookup(ReferenceHelper.class), project.getConfigurationHelper());
                    map.put(project, j2meProperties);
                }
                perform(obj, node, j2meProperties);
            }
            save(map);
        }
    }

    public final void actionPerformed(Collection<? extends Node> nodes) {
        Node[] n = nodes.toArray(new Node[nodes.size()]);
        performAction(n);
    }

    protected abstract List<VisualClassPathItem> addItems(T[] obj, List<VisualClassPathItem> list, Node node);

    protected abstract T[] getItems();
}
