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
public class EjbModulesNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(EjbModulesNode.class.getName());

    private EjbModulesNode(EjbModuleChildFactory factory) {
        super(Children.create(factory, true));
        setDisplayName(NbBundle.getMessage(EjbModulesNode.class, "LBL_EjbModules"));
        getCookieSet().add(new RefreshEnterpriseModuleChildren(factory));
    }

    public static EjbModulesNode newInstance(Lookup lookup) {
        EjbModuleChildFactory factory = new EjbModuleChildFactory(lookup);
        return new EjbModulesNode(factory);
    }

    @Override
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.EJB_FOLDER);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return UISupport.getIcon(ServerIcon.EJB_OPENED_FOLDER);
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        return new SystemAction[] {
               SystemAction.get(RefreshAction.class)
           };
    }

    private static class EjbModuleChildFactory extends ChildFactory<WSDefaultModule> {

        private final Lookup lookup;

        public EjbModuleChildFactory(Lookup lookup) {
            this.lookup = lookup;
        }

        public void refresh() {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(WSDefaultModule key) {
            return new WSDefaultModuleNode(ModuleType.EJB, key);
        }

        @Override
        protected boolean createKeys(List<WSDefaultModule> toPopulate) {
            DeploymentManager manager = lookup.lookup(DeploymentManager.class);
            Target target = lookup.lookup(Target.class);

            if (manager == null || target == null) {
                return true;
            }

            try {
                List<WSDefaultModule> toAdd = new ArrayList<WSDefaultModule>();

                Set<String> ids = new HashSet<String>();
                for (TargetModuleID module : manager.getRunningModules(ModuleType.EJB, new Target[] {target})) {
                    toAdd.add(new WSDefaultModule(module, true));
                    ids.add(module.getModuleID());
                }

                // it doesn't matter if we call available or non running - WS returns all
                for (TargetModuleID module : manager.getAvailableModules(ModuleType.EJB, new Target[] {target})) {
                    if (!ids.contains(module.getModuleID())) {
                        toAdd.add(new WSDefaultModule(module, false));
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

    private static class RefreshEnterpriseModuleChildren implements RefreshCookie {

        private final EjbModuleChildFactory factory;

        RefreshEnterpriseModuleChildren (EjbModuleChildFactory factory){
            this.factory = factory;
        }

        public void refresh() {
            factory.refresh();
        }
    }
}
