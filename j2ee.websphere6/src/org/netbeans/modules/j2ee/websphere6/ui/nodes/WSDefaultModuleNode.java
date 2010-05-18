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
import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WSDefaultModuleNode extends AbstractNode {

    private final ModuleType moduleType;

    private final WSDefaultModule module;

    public WSDefaultModuleNode(ModuleType moduleType, WSDefaultModule module) {
        super(Children.LEAF);
        this.moduleType = moduleType;
        this.module = module;
        setDisplayName(decorateConsoleName(module.getConsoleName()));
        setShortDescription(module.getRealName());
    }

    @Override
    public Image getIcon(int type) {
        if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_ARCHIVE);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_ARCHIVE);
        } else if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
        } else {
            throw new IllegalArgumentException("Unsupported module type"); // NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{};
    }

    private String decorateConsoleName(String consoleName) {
        String name = consoleName;
        if (!module.isRunning()) {
            name += " [" // NOI18N
                    + NbBundle.getMessage(WSDefaultModuleNode.class, "LBL_Stopped")
                    + "]"; // NOI18N
        }
        return name;
    }
}
