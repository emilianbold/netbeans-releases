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

package org.netbeans.modules.vmd.midpnb.components.svg;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuItemEventSourceCD;
import org.netbeans.modules.vmd.midpnb.flow.FlowSVGMenuItemPinOrderPresenter;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorSVGMenuSelectCommand;

/**
 *
 * @author Karol Harezlak
 */
public class SVGMenuCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGMenu"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_16.png"; // NOI18N
    
    public static final String PROP_ELEMENTS = "elements"; // NOI18N
    public static final String PROP_SELECT_COMMAND = "selectCommand";  // NOI18N
    public static final String PROP_INDEX_BASED_SWITCH = "indexBasedSwitch";  // NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SVGAnimatorWrapperCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_ELEMENTS, SVGMenuItemEventSourceCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(SVGMenuItemEventSourceCD.TYPEID), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_SELECT_COMMAND, CommandEventSourceCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_INDEX_BASED_SWITCH, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
//            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
//                .addProperty("Select Command", PropertyEditorSVGMenuSelectCommand.createInstanceMenuSelect(), PROP_SELECT_COMMAND) //NOI18N
            .addPropertiesCategory(PropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty("Index Based Switch", Boolean.class, PROP_INDEX_BASED_SWITCH); //NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // accept
                new AcceptTypePresenter(SVGMenuItemEventSourceCD.TYPEID) {
                    protected void notifyCreated (DesignComponent component) {
                        super.notifyCreated (component);
                        ArraySupport.append (getComponent (), SVGMenuCD.PROP_ELEMENTS, component);
                        if (component.isDefaultValue(ListElementEventSourceCD.PROP_STRING)) {
                            PropertyValue value = getComponent ().readProperty(SVGMenuCD.PROP_ELEMENTS);
                            List<PropertyValue> list = value.getArray ();
                            component.writeProperty (SVGMenuItemEventSourceCD.PROP_STRING, MidpTypes.createStringValue ("SVG Menu Item " + list.size()));
                        }
                    }
                },
                // properties
                createPropertiesPresenter(),
                // flow
                new FlowSVGMenuItemPinOrderPresenter()
        );
    }
    
}
