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

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorGaugeMaxValue;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.screen.display.GaugeDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderCategoryPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenter;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.GaugePC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorDefaultCommand;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorInstanceName;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorLayout;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorPreferredSize;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;

/**
 *
 * 
 */

public class GaugeCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Gauge"); // NOI18N
    
    public static final int VALUE_INDEFINITE = -1;
    
    public static final int VALUE_CONTINUOUS_IDLE = 0;
    public static final int VALUE_INCREMENTAL_IDLE = 1;
    public static final int VALUE_CONTINUOUS_RUNNING = 2;
    public static final int VALUE_INCREMENTAL_UPDATING = 3;
    
    public static final String PROP_MAX_VALUE = "maxValue"; // NOI18N
    public static final String PROP_VALUE = "value"; // NOI18N
    public static final String PROP_INTERACTIVE = "interactive"; // NOI18N
    public static final String PROP_USED_BY_ALERT = "usedByAlert"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_VALUE, MidpTypes.createIntegerValue (50));
        component.writeProperty (PROP_MAX_VALUE, MidpTypes.createIntegerValue (100));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor (PROP_USED_BY_ALERT, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, Versionable.FOREVER), // HINT - used for modelling Alert.indicator
                new PropertyDescriptor (PROP_INTERACTIVE, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor (PROP_VALUE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor (PROP_MAX_VALUE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP)
        );
    }
    
    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, PropertiesPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, InspectorFolderCategoryPresenter.class);
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                    .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                        .addProperty(NbBundle.getMessage(GaugeCD.class, "DISP_Gauge_Is_Interactive"), // NOI18N
                            PropertyEditorBooleanUC.createInstance(AlertCD.TYPEID,
                                NbBundle.getMessage(GaugeCD.class, "DISP_RB_Label")), PROP_INTERACTIVE) // NOI18N
                        .addProperty(NbBundle.getMessage(GaugeCD.class, "DISP_Gauge_Maximum_Value"), // NOI18N
                            PropertyEditorGaugeMaxValue.createInstance(), PROP_MAX_VALUE)
                        .addProperty(NbBundle.getMessage(GaugeCD.class, "DISP_Gauge_Value"), // NOI18N
                            PropertyEditorNumber.createIntegerInstance(false, NbBundle.getMessage(GaugeCD.class, "LBL_Gauge_Value")), PROP_VALUE ) // NOI18N
                        .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Label"), // NOI18N
                            PropertyEditorString.createInstance(NbBundle.getMessage(ItemCD.class, "LBL_Item_Label"), // NOI18N
                                AlertCD.TYPEID), ItemCD.PROP_LABEL)
                        .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Default_Command"), // NOI18N
                            PropertyEditorDefaultCommand.createInstance(AlertCD.TYPEID), ItemCD.PROP_DEFAULT_COMMAND) // NOI18N
                        .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Layout"), // NOI18N
                            PropertyEditorLayout.createInstance(AlertCD.TYPEID), ItemCD.PROP_LAYOUT)
                        .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Width"), // NOI18N
                            PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(ItemCD.class, "LBL_Item_Preferred_Width"), // NOI18N
                                NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Width"), AlertCD.TYPEID), ItemCD.PROP_PREFERRED_WIDTH) // NOI18N
                        .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Height"), // NOI18N
                            PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(ItemCD.class, "LBL_Item_Preferred_Height"), // NOI18N
                                NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Height"), AlertCD.TYPEID), ItemCD.PROP_PREFERRED_HEIGHT) // NOI18N
                     .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                        .addProperty(NbBundle.getMessage(ClassCD.class, "DISP_Class_Instance_Name"), // NOI18N
                            PropertyEditorInstanceName.createInstance(ClassCD.TYPEID), ClassCD.PROP_INSTANCE_NAME)
                        .addProperty(NbBundle.getMessage(ClassCD.class, "DISP_Class_Is_Lazy_Initialized"), // NOI18N
                            PropertyEditorBooleanUC.createInstance(), ClassCD.PROP_LAZY_INIT); // NOI18N
                        
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_INTERACTIVE))
                .addParameters(new GaugeValueParameter (), new GaugeMaxValueParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, PROP_INTERACTIVE, GaugeMaxValueParameter.PARAM_MAX_VALUE, GaugeValueParameter.PARAM_VALUE))
                .addSetters(MidpSetter.createSetter("setValue", MidpVersionable.MIDP).addParameters(GaugeValueParameter.PARAM_VALUE)) // NOI18N
                .addSetters(MidpSetter.createSetter("setMaxValue", MidpVersionable.MIDP).addParameters(GaugeMaxValueParameter.PARAM_MAX_VALUE)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                // delete
                new DeletePresenter () {
                    protected void delete () {
                        DesignComponent component = getComponent ();
                        if (MidpTypes.getBoolean (component.readProperty (PROP_USED_BY_ALERT))) {
                            component.getParentComponent ().writeProperty (AlertCD.PROP_INDICATOR, PropertyValue.createNull ());
                            component.writeProperty (PROP_USED_BY_ALERT, MidpTypes.createBooleanValue (false));
                        }
                    }
                },
                // screen
                new GaugeDisplayPresenter(),
                //inspector
                InspectorPositionPresenter.create(new GaugePC()),
                MidpInspectorSupport.createSpecialComponentCommandCategory(createOrderingArrayController(), AlertCD.TYPEID, CommandCD.TYPEID)
        );
    }
    
    private List<InspectorOrderingController> createOrderingArrayController() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(ItemCD.PROP_COMMANDS, 0, ItemCommandEventSourceCD.TYPEID));
    }

    private static class GaugeValueParameter extends MidpParameter {

        private static final String PARAM_VALUE = "value"; // NOI18N

        protected GaugeValueParameter () {
            super (PARAM_VALUE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_MAX_VALUE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                if (value == VALUE_INDEFINITE) {
                    propertyValue = component.readProperty (PROP_VALUE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        value = MidpTypes.getInteger (propertyValue);
                        switch (value) {
                            case VALUE_CONTINUOUS_IDLE: section.getWriter ().write ("Gauge.CONTINUOUS_IDLE"); return; // NOI18N
                            case VALUE_INCREMENTAL_IDLE: section.getWriter ().write ("Gauge.INCREMENTAL_IDLE"); return; // NOI18N
                            case VALUE_CONTINUOUS_RUNNING: section.getWriter ().write ("Gauge.CONTINUOUS_RUNNING"); return; // NOI18N
                            case VALUE_INCREMENTAL_UPDATING: section.getWriter ().write ("Gauge.INCREMENTAL_UPDATING"); return; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    }
                }
            }
            super.generateParameterCode (component, section, index);
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return true;
        }

    }

    private static class GaugeMaxValueParameter extends MidpParameter {

        private static final String PARAM_MAX_VALUE = "maxValue"; // NOI18N

        public GaugeMaxValueParameter () {
            super (PARAM_MAX_VALUE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_MAX_VALUE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                if (value == VALUE_INDEFINITE) {
                    section.getWriter ().write ("Gauge.INDEFINITE"); // NOI18N
                    return;
                }
            }
            super.generateParameterCode (component, section, index);
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return true;
        }

    }
    
}
