/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.docker.ui.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerIntegration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public final class DockerNode extends AbstractNode {

    private static final RequestProcessor REFRESH_PROCESSOR =
            new RequestProcessor("Docker node update/refresh", 5);

    private static final String DOCKER_ICON = "org/netbeans/modules/docker/ui/resources/docker_root.png"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(DockerNode.class.getName());

    private static DockerNode node;

    private DockerNode(ChildFactory factory, String displayName, String shortDesc, String iconBase) {
        super(Children.create(factory, true));

        setName(""); // NOI18N
        setDisplayName(displayName);
        setShortDescription(shortDesc);
        setIconBaseWithExtension(iconBase);
    }

    @ServicesTabNodeRegistration(
        name = "docker",
        displayName = "org.netbeans.modules.docker.ui.node.Bundle#Docker_Root_Node_Name",
        shortDescription = "org.netbeans.modules.docker.ui.node.Bundle#Docker_Root_Node_Short_Description",
        iconResource = "org/netbeans/modules/docker/ui/resources/docker_root.png",
        position = 500
    )
    public static synchronized DockerNode getInstance() {
        if (node == null) {
            ChildFactory factory = new ChildFactory(DockerIntegration.getDefault());
            factory.init();

            node = new DockerNode(factory,
                    NbBundle.getMessage(DockerNode.class, "Docker_Root_Node_Name"),
                    NbBundle.getMessage(DockerNode.class, "Docker_Root_Node_Short_Description"),
                    DOCKER_ICON);
        }
        return node;
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Docker/Actions").toArray(new Action[0]); // NOI18N
    }

    private static class ChildFactory extends org.openide.nodes.ChildFactory<DockerInstance>
            implements ChangeListener {

        private static final Comparator<DockerInstance> COMPARATOR = new InstanceComparator();

        private final DockerIntegration registry;

        public ChildFactory(DockerIntegration registry) {
            super();
            this.registry = registry;
        }

        public void init() {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    synchronized (ChildFactory.this) {
                        registry.addChangeListener(
                            WeakListeners.create(ChangeListener.class, ChildFactory.this, registry));
                        updateState(new ChangeEvent(registry));
                    }
                }
            });
        }

        public void stateChanged(final ChangeEvent e) {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    updateState(e);
                }
            });
        }

        private synchronized void updateState(final ChangeEvent e) {
            refresh();
        }

        protected final void refresh() {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(DockerInstance key) {
            return new DockerInstanceNode(new CachedDockerInstance(key));
        }

        @Override
        protected boolean createKeys(List<DockerInstance> toPopulate) {
            List<DockerInstance> fresh = new ArrayList<>(registry.getInstances());
            Collections.sort(fresh, COMPARATOR);

            toPopulate.addAll(fresh);
            return true;
        }

    } // end of ChildFactory

    private static class InstanceComparator implements Comparator<DockerInstance>, Serializable {

        public int compare(DockerInstance o1, DockerInstance o2) {
            boolean firstNull = false;
            boolean secondNull = false;

            if (o1.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o1);
                firstNull = true;
            }
            if (o2.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o2);
                secondNull = true;
            }

            if (firstNull && secondNull) {
                return 0;
            } else if (firstNull && !secondNull) {
                return -1;
            } else if (!firstNull && secondNull) {
                return 1;
            }

            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }
}
