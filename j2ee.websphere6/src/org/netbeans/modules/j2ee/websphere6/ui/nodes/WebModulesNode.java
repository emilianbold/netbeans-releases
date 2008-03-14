/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.websphere6.ui.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.actions.RefreshAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Petr Hejl
 */
public class WebModulesNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(WebModulesNode.class.getName());

    private WebModulesNode(WebModuleChildFactory factory) {
        super(Children.create(factory, true));
        setDisplayName(NbBundle.getMessage(WebModulesNode.class, "LBL_WebApps"));
        getCookieSet().add(new RefreshEnterpriseModuleChildren(factory));
    }

    public static WebModulesNode newInstance(Lookup lookup) {
        WebModuleChildFactory factory = new WebModuleChildFactory(lookup);
        return new WebModulesNode(factory);
    }

    @Override
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_FOLDER);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        return new SystemAction[] {
               SystemAction.get(RefreshAction.class)
           };
    }

    private static class WebModuleChildFactory extends ChildFactory<WebModule> {

        private final Lookup lookup;

        public WebModuleChildFactory(Lookup lookup) {
            this.lookup = lookup;
        }

        public void refresh() {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(WebModule key) {
            return new WSDefaultModuleNode(ModuleType.WAR, key);
        }

        @Override
        protected boolean createKeys(List<WebModule> toPopulate) {
            DeploymentManager manager = lookup.lookup(DeploymentManager.class);
            Target target = lookup.lookup(Target.class);

            if (manager == null || target == null) {
                return true;
            }

            try {
                List<WebModule> toAdd = new ArrayList<WebModule>();

                Set<String> ids = new HashSet<String>();
                for (TargetModuleID module : manager.getRunningModules(ModuleType.WAR, new Target[] {target})) {
                    toAdd.add(new WebModule(module, true));
                    ids.add(module.getModuleID());
                }

                // it doesn't matter if we call available or non running - WS returns all
                for (TargetModuleID module : manager.getAvailableModules(ModuleType.WAR, new Target[] {target})) {
                    if (!ids.contains(module.getModuleID())) {
                        toAdd.add(new WebModule(module, false));
                    }
                }

                Collections.sort(toAdd, WSDefaultModule.MODULE_COMPARATOR);
                toPopulate.addAll(toAdd);
            } catch (TargetException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            return true;
        }
    }

    private static class WebModule extends WSDefaultModule {

        private static final String PREFIX = "WebSphere:";

        private static final String NAME_ATTRIBUTE = "name";

        private static final String NAME_SUFFIX_1 = "_war";

        private static final String NAME_SUFFIX_2 = ".war";

        private String realName;

        public WebModule(TargetModuleID moduleID, boolean running) {
            super(moduleID, running);
        }

        public synchronized String getRealName() {
            if (realName == null) {
                TargetModuleID[] children = getModuleID().getChildTargetModuleID();
                if (children != null && children.length == 1) {
                    realName = constructName(children[0].getModuleID(), false);
                }
                realName = constructName(getModuleID().getModuleID(), true);
            }
            return realName;
        }

        protected String constructName(String id, boolean suffix) {
            String name = id;
            if (name.startsWith(PREFIX)) {
                name = name.substring(PREFIX.length());
                String[] parts = name.split(",");
                for (String part : parts) {
                    String[] pair = part.split("=");
                    if (pair.length == 2 && pair[0].trim().equals(NAME_ATTRIBUTE)) {
                        name = pair[1].trim();
                        if (!suffix) {
                            if (name.endsWith(NAME_SUFFIX_1)) {
                                return name.substring(0, name.length() - NAME_SUFFIX_1.length());
                            } else if (name.endsWith(NAME_SUFFIX_2)) {
                                return name.substring(0, name.length() - NAME_SUFFIX_2.length());
                            }
                        }
                        return name;
                    }
                }
            }

            return id;
        }
    }

    private static class RefreshEnterpriseModuleChildren implements RefreshCookie {

        private final WebModuleChildFactory factory;

        RefreshEnterpriseModuleChildren (WebModuleChildFactory factory){
            this.factory = factory;
        }

        public void refresh() {
            factory.refresh();
        }
    }
}
