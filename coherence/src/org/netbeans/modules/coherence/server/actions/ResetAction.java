/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.actions;

import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.CoherenceServer;
import org.netbeans.modules.coherence.server.CoherenceServerProperty;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Actions which invokes reseting server properties to default values.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResetAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {

            // confirmation for the action
            DialogDescriptor.Confirmation confirmation = new Confirmation(
                    NbBundle.getMessage(ResetAction.class, "MSG_ResetProperties"), //NOI18N
                    NbBundle.getMessage(ResetAction.class, "TITLE_ResetProperties")); //NOI18N
            if (DialogDisplayer.getDefault().notify(confirmation) != DialogDescriptor.YES_OPTION) {
                return;
            }

            for (Node node : activatedNodes) {
                CoherenceServer coherenceServer = node.getLookup().lookup(CoherenceServer.class);
                if (coherenceServer != null) {
                    resetProperties(coherenceServer.getInstanceProperties());
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            for (Node node : activatedNodes) {
                CoherenceServer coherenceServer = node.getLookup().lookup(CoherenceServer.class);
                if (coherenceServer != null) {
                    return !coherenceServer.isRunning();
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ResetAction.class, "ACTION_ServerReset"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Performs reset all Coherence server properties to defaults.
     *
     * @param instanceProperties properties for reseting
     */
    private static void resetProperties(InstanceProperties instanceProperties) {
        for (CoherenceServerProperty property : CoherenceProperties.SERVER_PROPERTIES) {
            instanceProperties.removeKey(property.getPropertyName());
        }
    }
}
