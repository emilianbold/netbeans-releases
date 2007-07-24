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
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class CanvasCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Canvas"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/canvas_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/canvas_32.png"; // NOI18N

    public static final Integer VALUE_DOWN = 6;
    public static final Integer VALUE_FIRE = 8;
    public static final Integer VALUE_GAME_A = 9;
    public static final Integer VALUE_GAME_B = 10;
    public static final Integer VALUE_GAME_C = 11;
    public static final Integer VALUE_GAME_D = 12;

    public static final Integer VALUE_KEY_NUM0 = 48;
    public static final Integer VALUE_KEY_NUM1 = 49;
    public static final Integer VALUE_KEY_NUM2 = 50;
    public static final Integer VALUE_KEY_NUM3 = 51;
    public static final Integer VALUE_KEY_NUM4 = 52;
    public static final Integer VALUE_KEY_NUM5 = 53;
    public static final Integer VALUE_KEY_NUM6 = 54;
    public static final Integer VALUE_KEY_NUM7 = 55;
    public static final Integer VALUE_KEY_NUM8 = 56;
    public static final Integer VALUE_KEY_NUM9 = 57;
    public static final Integer VALUE_KEY_POUND = 35;
    public static final Integer VALUE_KEY_STAR = 42;
    public static final Integer VALUE_LEFT = 2;
    public static final Integer VALUE_RIGHT = 5;
    public static final Integer VALUE_UP = 1;
    
    public static final String PROP_IS_FULL_SCREEN = "isFullScreen"; //NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (DisplayableCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
                //MIDP 2.0
                new PropertyDescriptor(PROP_IS_FULL_SCREEN, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, MidpVersionable.MIDP_2)
                );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Full Screen", "Controls whether the Canvas is in full-screen mode or in normal mode.", PropertyEditorBooleanUC.createInstance(), PROP_IS_FULL_SCREEN);
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_IS_FULL_SCREEN))
                .addSetters(MidpSetter.createSetter("setFullScreenMode", MidpVersionable.MIDP_2).addParameters(PROP_IS_FULL_SCREEN));
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter()
        );
    }

}
