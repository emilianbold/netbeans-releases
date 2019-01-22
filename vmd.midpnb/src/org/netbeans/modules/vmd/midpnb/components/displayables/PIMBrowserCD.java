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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.ScreenCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.util.*;

/**
 * 
 */

public final class PIMBrowserCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.pda.PIMBrowser"); // NOI18N
    
    public static final int VALUE_CONTACT_LIST = 1;
    public static final int VALUE_EVENT_LIST = 2;
    public static final int VALUE_TODO_LIST = 3;
    
    public static final int VALUE_READ_ONLY = 1;
    public static final int VALUE_WRITE_ONLY =  2;
    public static final int VALUE_READ_WRITE = 3;
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/PIM_browser_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/PIM_browser_32.png"; // NOI18N
    
    public static final String PROP_PIM_TYPE = "pimType"; //NOI18N

    private static Map listTypes;
    private static Map accessTypes;
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_PIM_TYPE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_CONTACT_LIST), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                   .addProperty(NbBundle.getMessage(PIMBrowserCD.class, "DISP_PIMBrowser_Type"), // NOI18N
                        PropertyEditorComboBox.createInstance(getListTypes(), TYPEID,
                            NbBundle.getMessage(PIMBrowserCD.class, "DISP_PIMBrowser_Type_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(PIMBrowserCD.class, "DISP_PIMBrowser_Type_UCLABEL")), PROP_PIM_TYPE); // NOI18N
    }
    
    private Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
        .addParameters(MidpCustomCodePresenterSupport.createDisplayParameter())
        .addParameters(MidpCustomCodePresenterSupport.createPIMListTypesParameter())
        .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(MidpCustomCodePresenterSupport.PARAM_DISPLAY, MidpCustomCodePresenterSupport.PARAM_PIM_LIST_TYPE));
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                //properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                MidpCodePresenterSupport.createAddImportPresenter("javax.microedition.pim.PIM"), //NOI18N
                // actions
                AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, CommandCD.TYPEID),
                //inspector
                InspectorPositionPresenter.create(new DisplayablePC()),
                //screen
                new DisplayableDisplayPresenter(ImageUtilities.loadImage(ICON_LARGE_PATH))
         );
    }
    
    @Override
    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_BASIC);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_PDA);
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, AddActionPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, DisplayableDisplayPresenter.class);
        MidpActionsSupport.addUnusedCommandsAddActionForDisplayable(presenters);
        super.gatherPresenters(presenters);
    }
    
    public static Map<String, PropertyValue> getListTypes() {
        if (listTypes == null || listTypes.isEmpty()) {
            listTypes = new TreeMap<String, PropertyValue>();
            listTypes.put("CONTACT_LIST", MidpTypes.createIntegerValue(VALUE_CONTACT_LIST)); // NOI18N
            listTypes.put("EVENT_LIST", MidpTypes.createIntegerValue(VALUE_EVENT_LIST));   // NOI18N
            listTypes.put("TODO_LIST", MidpTypes.createIntegerValue(VALUE_TODO_LIST));   // NOI18N
        }
        return listTypes;
    }
    
    public static Map<String, PropertyValue> getAccessTypes() {
        if (accessTypes == null || accessTypes.isEmpty()) {
            accessTypes = new TreeMap<String, PropertyValue>();
            accessTypes.put("READ_ONLY", MidpTypes.createIntegerValue(VALUE_READ_ONLY)); // NOI18N
            accessTypes.put("WRITE_ONLY", MidpTypes.createIntegerValue(VALUE_WRITE_ONLY));   // NOI18N
            accessTypes.put("READ_WRITE", MidpTypes.createIntegerValue(VALUE_READ_WRITE));   // NOI18N
        }
        return accessTypes;
    }
    
}
