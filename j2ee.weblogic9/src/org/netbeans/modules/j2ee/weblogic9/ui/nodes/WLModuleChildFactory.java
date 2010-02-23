/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class WLModuleChildFactory
        extends org.openide.nodes.ChildFactory<WLModuleNode> implements RefreshModulesCookie {

    private static final Logger LOGGER = Logger.getLogger(WLModuleChildFactory.class.getName());

    private final Lookup lookup;

    private final ModuleType moduleType;

    public WLModuleChildFactory(Lookup lookup, ModuleType moduleType) {
        this.lookup = lookup;
        this.moduleType = moduleType;
    }

    public final void refresh() {
        refresh(false);
    }

    @Override
    protected Node createNodeForKey(WLModuleNode key) {
        return key;
    }

    @Override
    protected boolean createKeys(List<WLModuleNode> toPopulate) {
        WLDeploymentManager dm = lookup.lookup(WLDeploymentManager.class);
        try {
            TargetModuleID[] modules = dm.getAvailableModules(moduleType, dm.getTargets());
            TargetModuleID[] stopped = dm.getNonRunningModules(moduleType, dm.getTargets());
            Set<String> stoppedByName = new HashSet<String>();
            if (stopped != null) {
                for (TargetModuleID module : stopped) {
                    stoppedByName.add(module.getModuleID());
                }
            }

            if (modules != null) {
                for (TargetModuleID module : modules) {
                    toPopulate.add(new WLModuleNode(module, lookup, moduleType,
                            stoppedByName.contains(module.getModuleID())));
                }
            }
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (TargetException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        // perhaps we should return false on exception, however it would most likely fail again
        return true;
    }
}
