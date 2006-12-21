/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.propertyeditors.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Karol Harezlak
 */
// TODO - ValidatorPresenter: timeout has to be -2 or positive number
public final class AlertCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Alert"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/alert_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/alert_64.png"; // NOI18N
    
    public static final PropertyValue FOREVER_VALUE = MidpTypes.createIntegerValue(-2);
    
    public static final String PROP_STRING  = "string"; // NOI18N
    public static final String PROP_TIMEOUT = "timeout"; // NOI18N
    public static final String PROP_IMAGE = ImageCD.PROP_IMAGE; // NOI18N
    public static final String PROP_ALERT_TYPE = "type"; // NOI18N
    public static final String PROP_INDICATOR = "indicator";  // NOI18N
    
    private static Map<String, PropertyValue> alertTypes;
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public void postInitialize(DesignComponent component) {
        component.writeProperty(PROP_TIMEOUT, FOREVER_VALUE);
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_TIMEOUT, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_ALERT_TYPE, MidpTypes.TYPEID_ALERT_TYPE, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_INDICATOR, GaugeCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Image", PropertyEditorResourcesComboBox.createImagePropertyEditor(), PROP_IMAGE)
                .addProperty("String", PropertyEditorString.createInstance(), PROP_STRING)
                .addProperty("Type", PropertyEditorComboBox.createInstance(getKindTypes(), TYPEID), PROP_ALERT_TYPE)
                .addProperty("Use Indicator", PropertyEditorIndicator.createInstance(), PROP_INDICATOR)
                .addProperty("Timeout", PropertyEditorTimeout.createInstance(), PROP_TIMEOUT);
    }
    
    // TODO override Displayable.addCommand, see DesignerMIDP document
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_IMAGE, PROP_STRING, PROP_ALERT_TYPE, PROP_INDICATOR))
                .addParameters(new AlertTimeoutParameter())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE, PROP_STRING, PROP_IMAGE, PROP_ALERT_TYPE))
                .addSetters(MidpSetter.createSetter("setImage", MidpVersionable.MIDP).addParameters(PROP_IMAGE))
                .addSetters(MidpSetter.createSetter("setIndicator", MidpVersionable.MIDP_2).addParameters(PROP_INDICATOR))
                .addSetters(MidpSetter.createSetter("setString", MidpVersionable.MIDP).addParameters(PROP_STRING))
                .addSetters(MidpSetter.createSetter("setTimeout", MidpVersionable.MIDP).addParameters(AlertTimeoutParameter.PARAM_TIMEOUT))
                .addSetters(MidpSetter.createSetter("setType", MidpVersionable.MIDP).addParameters(PROP_ALERT_TYPE));
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // inspector
                InspectorPositionPresenter.create(new DisplayablePC()),
                // code
                createSetterPresenter(),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_IMAGE)
                );
    }
    
    public static Map<String, PropertyValue> getKindTypes() {
        if (alertTypes == null) {
            alertTypes = new TreeMap<String, PropertyValue>();
            for (MidpTypes.AlertType type : MidpTypes.AlertType.values()) {
                alertTypes.put(type.toString(), MidpTypes.createAlertTypeValue(type));
            }
        }
        return alertTypes;
    }
    
    
    
    private static class AlertTimeoutParameter extends MidpParameter {
        
        public static final String PARAM_TIMEOUT = "timeout"; // NOI18N
        
        public AlertTimeoutParameter() {
            super(PARAM_TIMEOUT);
        }
        
        public void generateParameterCode(DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(PROP_TIMEOUT);
            if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                int timeout = MidpTypes.getInteger(propertyValue);
                if (timeout == -2) {
                    section.getWriter().write("Alert.FOREVER"); // NOI18N
                    return;
                }
            }
            super.generateParameterCode(component, section, index);
        }
        
        public boolean isRequiredToBeSet(DesignComponent component) {
            return true;
        }
        
    }
    
}
