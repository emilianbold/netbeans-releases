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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.CallPointEventHandlerCD;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;

/**
 * @author David Kaspar
 */
public class CallPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#CallPoint"); // NOI18N

    public static final String PROP_ACCESS_CODE = "accessCode"; // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/call_point_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/call_point_32.png"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (PointCD.TYPEID, CallPointCD.TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_ACCESS_CODE, MidpTypes.createJavaCodeValue ("")); // NOI18N
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor (PROP_ACCESS_CODE, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    @Override
    public PaletteDescriptor getPaletteDescriptor () {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(CallPointCD.class, "DISP_CallPoint"), NbBundle.getMessage(CallPointCD.class, "TTIP_CallPoint"), ICON_PATH, LARGE_ICON_PATH); // NOI18N
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(CallPointCD.class, "DISP_CallPoint_Call_Code"), PropertyEditorJavaString.createInstance(TYPEID, MidpTypes.createJavaCodeValue("")), PROP_ACCESS_CODE); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ActionsPresenter.class);
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, false, false, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (PointSupport.createCallPointInfoResolver ()),
            // general
            CallPointEventHandlerCD.createCallPointEventHandlerCreatorPresenter (),
            // properties
            createPropertiesPresenter (),
            // flow
            new FlowInfoNodePresenter ()
        );
    }

}
