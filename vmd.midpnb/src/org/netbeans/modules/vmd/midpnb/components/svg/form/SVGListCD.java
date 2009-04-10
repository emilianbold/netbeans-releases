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

package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.vmd.api.flow.FlowSupport;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorListModel;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGListDisplayPresenter;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class SVGListCD extends ComponentDescriptor{

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGList"); // NOI18N
    
    public static final String PROP_ELEMENTS = "listElements"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/mobility/svgcore/resources/palette/form/list_16.png"; // NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (SVGComponentCD.TYPEID, TYPEID, true, false);
    }
   

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
                new PropertyDescriptor(PROP_ELEMENTS, SVGListElementEventSourceCD.TYPEID.getArrayType(), PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGRadioButtonCD.class, "DISP_ListModel"), PropertyEditorListModel.createInstance( NbBundle.getMessage( SVGListCD.class, "LBL_ListModel"), NbBundle.getMessage( SVGListCD.class, "TXT_ListModel")), PROP_ELEMENTS); // NOI18N
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ActionsPresenter.class);
        MidpActionsSupport.addCommonClassActionsPresenters(presenters, false, true, true, false, true);
        super.gatherPresenters(presenters);
    }



    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                //code
                MidpCustomCodePresenterSupport.createSVGComponentCodePresenter(TYPEID),
                MidpCodePresenterSupport.createAddImportPresenter(),
                new SVGListModelFooter(),
                new SVGListElementCodeFooter(SVGListElementEventSourceCD.TYPEID),
                //inspector
                new SVGComponentInspectorFolderPresenter(),
                MidpInspectorSupport.createComponentElementsCategory(NbBundle.getMessage (ListCD.class, "DISP_InspectorCategory_Elements"), getInspectorOrderingControllers(), SVGListElementEventSourceCD.TYPEID), //NOI18N
                new SVGListDisplayPresenter(),
                new FlowListElementsUpdaterPresenter()
        );
    }

    private List<InspectorOrderingController> getInspectorOrderingControllers() {

        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_ELEMENTS, 0, SVGListElementEventSourceCD.TYPEID));
    }

    private class FlowListElementsUpdaterPresenter extends DynamicPresenter {

        @Override
        protected void notifyAttached(DesignComponent component) {
            
        }

        @Override
        protected void notifyDetached(DesignComponent component) {
            
        }

        @Override
        protected DesignEventFilter getEventFilter() {
            return new DesignEventFilter().addDescentFilter(getComponent(), PROP_ELEMENTS);
        }

        @Override
        protected void designChanged(DesignEvent event) {
            if (getComponent() == null || getComponent().getParentComponent() == null) {
                return;
            }
            final FlowNodeDescriptor nodeToUpdate = getComponent().getParentComponent().getPresenter(FlowInfoNodePresenter.class).getNodeDescriptor();
            final FlowScene fc = FlowSupport.getFlowSceneForDocument(getComponent().getDocument());
            fc.scheduleNodeDescriptorForOrdering (nodeToUpdate);
            if (SwingUtilities.isEventDispatchThread()) {
                fc.resolveOrderInNodeDescriptors();
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        fc.resolveOrderInNodeDescriptors();
                    }
                });
            }
        }

        @Override
        protected void presenterChanged(PresenterEvent event) {
            
        }

    }

}
