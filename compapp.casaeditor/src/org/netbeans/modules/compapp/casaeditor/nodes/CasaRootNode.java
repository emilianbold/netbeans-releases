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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AutoLayoutAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddExternalServiceUnitAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.LoadWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddJBIModuleAction;
import org.netbeans.modules.compapp.casaeditor.properties.LookAndFeelProperty;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.casaeditor.properties.ShowHoveringHighlightProperty;
import org.netbeans.modules.compapp.casaeditor.properties.ShowToolTipProperty;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Josh Sandusky
 */
public class CasaRootNode extends CasaNode {

    private static final Image ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/CasaRootNode.png"); // NOI18N

    private static final String CHILD_ID_WSDL_ENDPOINTS  = "WSDLEndpoints";     // NOI18N
    private static final String CHILD_ID_SERVICE_ENGINES = "ServiceEngines";    // NOI18N
    private static final String CHILD_ID_CONNECTIONS     = "Connections";       // NOI18N

    private static final String[] CHILD_TYPES = {
        CHILD_ID_WSDL_ENDPOINTS,
        CHILD_ID_SERVICE_ENGINES,
        CHILD_ID_CONNECTIONS
    };


    public CasaRootNode(Object data, CasaNodeFactory factory) {
        super(data, new MyChildren(data, factory), factory);
    }

    @Override
    public String getName() {
        DataObject dataObject = getDataObject();
        if (dataObject != null) {
            return dataObject.getName();
        }
        return NbBundle.getMessage(getClass(), "LBL_CasaModel");        // NOI18N
    }

    @Override
    protected void setupPropertySheet(Sheet sheet) {
        final CasaWrapperModel model = (CasaWrapperModel) getData();
        if (model == null) {
            return;
        }

        Sheet.Set mainPropertySet =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.MAIN_SET);

        String propertyName = NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeel"); // NOI18N
        try {
            // global L&F properties
            Node.Property property = new LookAndFeelProperty(this);
            mainPropertySet.put(property);
        } catch (Exception e) {
            mainPropertySet.put(PropertyUtils.createErrorProperty(propertyName));
            ErrorManager.getDefault().notify(e);
        }

        // project-specific L&F properties
        Node.Property property = new ShowToolTipProperty(this);
        mainPropertySet.put(property);

        property = new ShowHoveringHighlightProperty(this);
        mainPropertySet.put(property);
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public boolean isValidSceneActionForLocation(Action action, Widget widget, Point sceneLocation) {
        if (action instanceof AddJBIModuleAction) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget engineRegion = scene.getEngineRegion();
            Rectangle engineRegionRect =
                    engineRegion.convertLocalToScene(new Rectangle(engineRegion.getBounds().width, engineRegion.getBounds().height));
            if (engineRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        else if ((action instanceof AddWSDLPortsAction)
              || (action instanceof LoadWSDLPortsAction)) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget bindingRegion = scene.getBindingRegion();
            Rectangle bindingRegionRect =
                    bindingRegion.convertLocalToScene(new Rectangle(bindingRegion.getBounds().width, bindingRegion.getBounds().height));
            if (bindingRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        else if (action instanceof AddExternalServiceUnitAction) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget externalRegion = scene.getExternalRegion();
            Rectangle externalRegionRect =
                    externalRegion.convertLocalToScene(new Rectangle(externalRegion.getBounds().width, externalRegion.getBounds().height));
            if (externalRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        return true;
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        final Project jbiProject = getModel().getJBIProject();
        actions.add(new AddJBIModuleAction(jbiProject, true));
        actions.add(new AddJBIModuleAction(jbiProject, false));
        actions.add(SystemAction.get(LoadWSDLPortsAction.class));
        actions.add(SystemAction.get(AddWSDLPortsAction.class));
        actions.add(SystemAction.get(AddExternalServiceUnitAction.class));
        actions.add(null);
        actions.add(new AutoLayoutAction(getDataObject()));
    }


    private static class MyChildren extends CasaNodeChildren<String> {
        private WeakReference mReference;

        public MyChildren(Object data, CasaNodeFactory factory) {
            super(data, factory);
            mReference = new WeakReference<Object>(data);
        }

        protected Node[] createNodes(String keyName) {
            if (mReference.get() != null) {
                try {
                    CasaWrapperModel model = (CasaWrapperModel) mReference.get();
                    if (keyName.equals(CHILD_ID_WSDL_ENDPOINTS)) {
                        return new Node[] {
                            mNodeFactory.createNode_portList(model.getCasaPorts()) };
                    } else if (keyName.equals(CHILD_ID_SERVICE_ENGINES)) {
                        return new Node[] {
                            mNodeFactory.createNode_suList(model.getServiceEngineServiceUnits()) };
                    } else if (keyName.equals(CHILD_ID_CONNECTIONS)) {
                        return new Node[] {
                            mNodeFactory.createNode_connectionList(model.getCasaConnectionList(false)) };
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            return null;
        }

        @Override
        public Object getChildKeys(Object data)  {
            return CHILD_TYPES;
        }
    }
}
