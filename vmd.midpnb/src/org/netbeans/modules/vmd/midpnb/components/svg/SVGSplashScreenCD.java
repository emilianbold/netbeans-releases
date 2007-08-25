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

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.NbBundle;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class SVGSplashScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGSplashScreen"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_splash_screen_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_splash_screen_32.png"; // NOI18N

    public static final String PROP_TIMEOUT = "timeout"; //NOI18N
    public static final String PROP_ALLOW_TIMEOUT_INTERRUPT = "allowTimeoutInterrupt"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    @Override
    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), SVGPlayerCD.MIDP_NB_SVG_LIBRARY);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SVGAnimatorWrapperCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TIMEOUT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(5000), false, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_ALLOW_TIMEOUT_INTERRUPT, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGSplashScreenCD.class, "DISP_SVGSplashScreen_Timeout"), PropertyEditorNumber.createIntegerInstance(), PROP_TIMEOUT) // NOI18N
                .addProperty(NbBundle.getMessage(SVGSplashScreenCD.class, "DISP_SVGSplashScreen_AllowTimeoutInterrupt"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGSplashScreenCD.class, "LBL_SVGSplashScreen_AllowTimeoutInterrupt")), PROP_ALLOW_TIMEOUT_INTERRUPT); // NOI18N
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createSVGTimeoutParameter ())
            .addParameters (MidpParameter.create (PROP_ALLOW_TIMEOUT_INTERRUPT))
            .addParameters (MidpCustomCodePresenterSupport.createSVGSplashScreenCommandParameter ())
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (SVGPlayerCD.PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("setTimeout", MidpVersionable.MIDP_2).addParameters (MidpCustomCodePresenterSupport.PARAM_SVG_TIMEOUT)) // NOI18N
            .addSetters (MidpSetter.createSetter ("setAllowTimeoutInterrupt", MidpVersionable.MIDP_2).addParameters (PROP_ALLOW_TIMEOUT_INTERRUPT)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter (),
            // code
            createSetterPresenter ()
        );
    }

}
