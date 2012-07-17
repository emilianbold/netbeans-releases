/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstanceNode;
import org.openide.nodes.Node;
import static org.openide.util.NbBundle.getMessage;

/**
 * GUI action to update GlassFish cloud user account properties.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountActionProperties extends GlassFishAccountAction {

    /**
     * Perform the action based on the currently activated nodes.
     * <p/>
     * This action will always be triggered from
     * {@link GlassFishAccountInstanceNode} so we can access it directly.
     * <p/>
     * @param activatedNodes Current activated nodes. It should always be
     *                       GlassFish user account GUI node.
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        GlassFishAccountInstance instance = activatedNodes[0].getLookup()
                .lookup(GlassFishAccountInstance.class);
        CommonServerUIs.showCloudCustomizer(instance.getServerInstance());
    }

    /**
     * Test whether the action should be enabled based on the currently
     * activated nodes.
     * <p/>
     * @param activatedNodes Current activated nodes, may be empty but not
     *                       <code>null</code>.
     * @return <code>true</code> to be enabled or <code>false</code>
     *         to be disabled.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes.length > 0 && activatedNodes[0].getLookup()
                .lookup(GlassFishAccountInstance.class) != null;
    }

    /**
     * Get a human presentable name of the action.
     * <p/>
     * This may be presented as an item in a menu. Value is retrieved from
     * properties bundle.
     * <p/>
     * @return human presentable name of the action.
     */
    @Override
    public String getName() {
        return getMessage(GlassFishAccountActionProperties.class,
                Bundle.USER_ACTION_PROPERTIES_NAME);
    }

}
