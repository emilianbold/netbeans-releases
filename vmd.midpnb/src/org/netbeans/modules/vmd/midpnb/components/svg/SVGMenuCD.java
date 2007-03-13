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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.netbeans.modules.vmd.midpnb.flow.FlowSVGMenuElementPinOrderPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;

/**
 *
 * @author Karol Harezlak
 */
public class SVGMenuCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGMenu"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_16.png"; // NOI18N

    public static final String PROP_ELEMENTS = "elements"; // NOI18N
    public static final String PROP_INDEX_BASED_SWITCH = "indexBasedSwitch";  // NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), SVGAnimatorWrapperCD.MIDP_NB_SVG_LIBRARY);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SVGAnimatorWrapperCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_ELEMENTS, SVGMenuElementEventSourceCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(SVGMenuElementEventSourceCD.TYPEID), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_INDEX_BASED_SWITCH, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(PropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty("Index Based Switch", Boolean.class, PROP_INDEX_BASED_SWITCH); //NOI18N
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createSVGMenuElementParameter ())
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (SVGAnimatorWrapperCD.PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("addMenuElement", MidpVersionable.MIDP_2).setArrayParameter (MidpCustomCodePresenterSupport.PARAM_SVG_MENU_ELEMENT).addParameters (MidpCustomCodePresenterSupport.PARAM_SVG_MENU_ELEMENT));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // accept
                new AcceptTypePresenter(SVGMenuElementEventSourceCD.TYPEID) {
                    protected void notifyCreated (DesignComponent component) {
                        super.notifyCreated (component);
                        ArraySupport.append (getComponent (), SVGMenuCD.PROP_ELEMENTS, component);
                        if (component.isDefaultValue(SVGMenuElementEventSourceCD.PROP_STRING)) {
                            List<PropertyValue> list = getComponent ().readProperty(SVGMenuCD.PROP_ELEMENTS).getArray ();
                            component.writeProperty (SVGMenuElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue ("SVG Menu Item " + list.size()));
                        }
                    }
                },
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter (),
                MidpCustomCodePresenterSupport.createSVGMenuCodePresenter (),
                MidpCustomCodePresenterSupport.createSVGMenuEventHandlerCodeNamePresenter (),
                // flow
                new FlowSVGMenuElementPinOrderPresenter ()
        );
    }
    
}
