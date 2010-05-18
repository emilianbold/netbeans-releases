/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.util.Collection;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetEngine;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaCollisionCollector;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Node action to show all connections connecting a service engine
 * service unit widget.
 * 
 * @author jqian
 */
public class ShowServiceUnitConnectionsAction extends NodeAction {

    @Override
    protected boolean asynchronous() {
        // This has to be done in the AWT thread!
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length > 0 && activatedNodes[0] instanceof ServiceUnitNode) {
            ServiceUnitNode suNode = (ServiceUnitNode) activatedNodes[0];
            CasaDataObject casaDO = suNode.getDataObject();
            CasaWrapperModel casaModel = suNode.getModel();
            CasaServiceEngineServiceUnit casaSESU =
                    (CasaServiceEngineServiceUnit) suNode.getData();

            final CasaModelGraphScene scene =
                    casaDO.getEditorSupport().getScene();

            Collection<CasaComponent> connections = scene.getEdges();

            for (CasaConnection connection : casaModel.getConnections(casaSESU, false)) {
                if (!connections.contains(connection)) {
                    ConnectionWidget connectionWidget = (ConnectionWidget) scene.addEdge(connection);
                    CasaEndpointRef casaConsumes = casaModel.getCasaEndpointRef(connection, true);
                    CasaEndpointRef casaProvides = casaModel.getCasaEndpointRef(connection, false);
                    scene.setEdgeSource(connection, casaConsumes);
                    scene.setEdgeTarget(connection, casaProvides);
                }
            }

            CasaNodeWidgetEngine widget = (CasaNodeWidgetEngine) scene.findWidget(casaSESU);
            widget.setConnectionHidden(false);

            scene.setOrthogonalRouter(
                    RouterFactory.createOrthogonalSearchRouter(new CasaCollisionCollector(
                    scene.getBindingRegion(),
                    scene.getEngineRegion(),
                    scene.getExternalRegion(),
                    scene.getConnectionLayer()))); // FIXME: a temp hack
            scene.updateEdgeRouting(null);
            scene.validate();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean ret = false;

        if (activatedNodes.length > 0 && activatedNodes[0] instanceof ServiceUnitNode) {
            ServiceUnitNode suNode = (ServiceUnitNode) activatedNodes[0];
            CasaDataObject casaDO = suNode.getDataObject();
            CasaModelGraphScene scene = casaDO.getEditorSupport().getScene();
            CasaServiceEngineServiceUnit casaSESU =
                    (CasaServiceEngineServiceUnit) suNode.getData();
            CasaNodeWidgetEngine widget = (CasaNodeWidgetEngine) scene.findWidget(casaSESU);
            ret = widget.isConnectionHidden();
        }

        return ret;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LoadWSDLPortsAction.class, "LBL_ShowServiceUnitConnectionsAction_Name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
