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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midpnb.components.sources;

import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.RenameAction;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.*;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.components.items.ItemSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.SecondaryGoToSourcePresenter;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midpnb.flow.FlowSVGMenuElementEventSourcePinPresenter;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuElementEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGMenuElementEventSource"); // NOI18N

    public static final String PROP_STRING = "string"; // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_element_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_element_32.png"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGMenuElementEventSourceCD.class, "DISP_SVGMenuElementEventSource_String"), // NOI18N
                    PropertyEditorString.createInstance(NbBundle.getMessage(SVGMenuElementEventSourceCD.class,
                        "LBL_SVGMenuElementEventSource_String")), PROP_STRING); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InspectorPositionPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, ActionsPresenter.class);

        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, false, true, false);
        MidpActionsSupport.addMoveActionPresenter(presenters, SVGMenuCD.PROP_ELEMENTS);
        presenters.addAll(ActionsSupport.createByParent(DeleteAction.class, RenameAction.class));

        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (ItemSupport.createSVGMenuElementInfoResolver ()),
            // properties
            createPropertiesPresenter (),
            // inspector
//            InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter(),
            new DeletePresenter () {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent menu = component.getParentComponent();
                    ArraySupport.remove (menu, SVGMenuCD.PROP_ELEMENTS, component);
                }
            },
            // flow
            new FlowSVGMenuElementEventSourcePinPresenter(),
            // general
             new GoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    DesignComponent svgMenu = getComponent().getParentComponent();
                    return MultiGuardedSection.matches (section, svgMenu.getComponentID () + "-action", getComponent ().getComponentID () + "-postAction"); // NOI18N
                }
            },
            new SecondaryGoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    DesignComponent parentComponent = getComponent().getParentComponent();
                    if (parentComponent == null)
                        return false;
                    boolean lazyInit = MidpTypes.getBoolean (parentComponent.readProperty (ClassCD.PROP_LAZY_INIT));
                    return MultiGuardedSection.matches(section, lazyInit ? parentComponent.getComponentID() + "-getter" : parentComponent.getDocument ().getRootComponent ().getComponentID () + "-initialize", 1); // NOI18N
                }
            }
        );
    }

}
