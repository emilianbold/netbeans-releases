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

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Child factory for children of the Resources nodes of an ME project.
 *
 * @author Tim Boudreau
 */
public class ResourcesChildren extends ChildFactory<VisualClassPathItem> {

    private final J2MEProject project;
    static final String ARCHIVE_ICON =
            "org/netbeans/modules/mobility/project/ui/resources/libraries.gif"; //NOI18N
    private final ProjectConfiguration config;

    ResourcesChildren(J2MEProject project) {
        //for case where we are the current configuration's children
        this (project, null);
    }

    ResourcesChildren(J2MEProject project, ProjectConfiguration config) {
        this.project = project;
        this.config = config;
    }

    public void update() {
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<VisualClassPathItem> toPopulate) {
        ProjectConfiguration myConfig = config == null ?
            project.getConfigurationHelper().getActiveConfiguration() : config;
        ArrayList<VisualClassPathItem> libs = null;

        J2MEProjectProperties j2meProperties = new J2MEProjectProperties(project,
                project.getLookup().lookup(AntProjectHelper.class),
                project.getLookup().lookup(ReferenceHelper.class),
                project.getConfigurationHelper());

        if (Thread.interrupted()) {
            return true;
        }
        if (myConfig.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName())) {
            libs = (ArrayList<VisualClassPathItem>) j2meProperties.get(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        } else {
            libs = (ArrayList<VisualClassPathItem>) j2meProperties.get(J2MEProjectProperties.CONFIG_PREFIX +
                    myConfig.getDisplayName() + "." + DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }

        if (libs == null) /* Using resources of default configuration */ {
            libs = (ArrayList<VisualClassPathItem>) j2meProperties.get(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        }
        if (Thread.interrupted()) {
            return true;
        }
        if (libs != null) {
            toPopulate.addAll(libs);
        }
        return true;
    }

    @Override
    protected Node[] createNodesForKey(VisualClassPathItem item) {
        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        String raw = item.getRawText();
        String xPath = helper.getStandardPropertyEvaluator().evaluate(raw);
        String[] paths = xPath.split(File.pathSeparator);
        List <Node> result = new LinkedList<Node>();
        FileObject projectDir = project.getProjectDirectory();
        if (!projectDir.isValid()) {
            return new Node[0];
        }
        File root = FileUtil.toFile(projectDir);
        boolean isLibrary = item.getType() == VisualClassPathItem.TYPE_LIBRARY;
        boolean multi = paths.length > 1;
        for (String path : paths) {
            File f = FileUtil.normalizeFile(isLibrary ? new File (path) : new File(root, path));
            Node n = new OneResourceNode (project, config, item, f, multi);
            result.add (n);
        }
        Node[] nodes = (Node[]) result.toArray(new Node[result.size()]);
        return nodes;
    }

    public void stateChanged(ChangeEvent e) {
        refresh(false);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh(false);
    }
}
