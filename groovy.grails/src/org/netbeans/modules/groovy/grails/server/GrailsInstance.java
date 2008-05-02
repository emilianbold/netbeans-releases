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

package org.netbeans.modules.groovy.grails.server;

import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsInstance implements ServerInstanceImplementation {

    private final GrailsChildFactory childFactory;

    private GrailsInstance(GrailsInstanceProvider provider) {
        this.childFactory = new GrailsChildFactory(provider);
    }

    public static final GrailsInstance forProvider(GrailsInstanceProvider provider) {
        return new GrailsInstance(provider);
    }

    public Node getBasicNode() {
        return new GrailsNode(Children.LEAF);
    }

    public Node getFullNode() {
        return new GrailsNode(Children.create(childFactory, false));
    }

    public JComponent getCustomizer() {
        return null;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(GrailsInstance.class, "GrailsInstance.displayName", "");
    }

    public String getServerDisplayName() {
        return NbBundle.getMessage(GrailsInstance.class, "GrailsInstance.serverDisplayName");
    }

    public void refresh() {
        childFactory.refresh();
    }

    public boolean isRemovable() {
        return false;
    }

    public void remove() {
        // noop
    }

    private static class GrailsNode extends AbstractNode {

        public GrailsNode(Children children) {
            super(children);
            // FIXME get version from runtime
            setDisplayName(NbBundle.getMessage(
                    GrailsInstance.class, "GrailsInstance.displayName", "N/A"));
            setIconBaseWithExtension(
                    "org/netbeans/modules/groovy/grails/resources/GrailsIcon.png"); // NOI18N
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {};
        }
    }

    private static class GrailsRunningNode extends AbstractNode {

        public GrailsRunningNode(Project project) {
            super(Children.LEAF);
            GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
            ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
            setDisplayName(NbBundle.getMessage(
                    GrailsInstance.class, "GrailsInstance.appDisplayName", info.getDisplayName(), config.getPort()));
            setIconBaseWithExtension(
                    "org/netbeans/modules/groovy/grails/resources/GrailsIcon.png"); // NOI18N
        }

        // FIXME put stop action here
        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {};
        }
    }

    private static class GrailsChildFactory extends ChildFactory<Project> {

        private final GrailsInstanceProvider provider;

        public GrailsChildFactory(GrailsInstanceProvider provider) {
            this.provider = provider;
        }

        public void refresh() {
            super.refresh(false);
        }

        @Override
        protected Node createNodeForKey(Project key) {
            return new GrailsRunningNode(key);
        }

        @Override
        protected boolean createKeys(List<Project> toPopulate) {
            toPopulate.addAll(provider.getRunningProjects());
            return true;
        }
    }
}
