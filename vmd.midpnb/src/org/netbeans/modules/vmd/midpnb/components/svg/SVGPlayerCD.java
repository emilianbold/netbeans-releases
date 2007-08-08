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
 *
 */
package org.netbeans.modules.vmd.midpnb.components.svg;

import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.codegen.SwitchDisplayableParameterPresenter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.general.SVGFileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.propertyeditors.SVGImageEditorElement;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGAnimatorWrapperDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class SVGPlayerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGPlayer"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_player_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_player_32.png"; // NOI18N

    public static final String PROP_SVG_IMAGE = "svgImage"; //NOI18N
    public static final String PROP_START_ANIM_IMMEDIATELY = "startAnimationImmediately"; //NOI18N
    public static final String PROP_TIME_INCREMENT = "animationTimeIncrement"; //NOI18N
    public static final String PROP_RESET_ANIMATION_WHEN_STOPPED = "resetAnimationWhenStopped"; //NOI18N

    public static final String PROP_OLD_START_ANIM_IMMEDIATELY = "startAnimationImmideately"; //NOI18N

    public static final String[] MIDP_NB_SVG_LIBRARY = {"nb_svg_midp_components"}; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.FOREVER;
    }

    @Override
    public void postInitialize(DesignComponent component) {
        component.writeProperty(PROP_START_ANIM_IMMEDIATELY, MidpTypes.createBooleanValue(true));
        component.writeProperty(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.createBooleanValue(true));
        MidpProjectSupport.addLibraryToProject(component.getDocument(), MIDP_NB_SVG_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_SVG_IMAGE, SVGImageCD.TYPEID, PropertyValue.createNull(), true, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_START_ANIM_IMMEDIATELY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_TIME_INCREMENT, MidpTypes.TYPEID_FLOAT, MidpTypes.createFloatValue(0.1f), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, Versionable.FOREVER)
                );
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_SVGImage"), //NOI18N
                    PropertyEditorResource.createInstance(new SVGImageEditorElement(),
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                        NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL")), PROP_SVG_IMAGE) //NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_StartAnimationImmediately"), PropertyEditorBooleanUC.createInstance(), PROP_START_ANIM_IMMEDIATELY) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_AnimationTimeIncrement"), PropertyEditorNumber.createFloatInstance(), PROP_TIME_INCREMENT) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_ResetAnimationWhenStopped"), PropertyEditorBooleanUC.createInstance(), PROP_RESET_ANIMATION_WHEN_STOPPED); // NOI18N
    }

    private Presenter createSetterPresenter() {
        return new CodeSetterPresenter ()
                .addParameters(MidpCustomCodePresenterSupport.createDisplayParameter())
                .addParameters(MidpParameter.create(PROP_SVG_IMAGE, PROP_START_ANIM_IMMEDIATELY, PROP_TIME_INCREMENT, PROP_RESET_ANIMATION_WHEN_STOPPED))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
                .addSetters(MidpSetter.createSetter("setTimeIncrement", MidpVersionable.MIDP_2).addParameters(PROP_TIME_INCREMENT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setStartAnimationImmediately", MidpVersionable.MIDP_2).addParameters(PROP_START_ANIM_IMMEDIATELY)) //NOI18N
                .addSetters(MidpSetter.createSetter("setResetAnimationWhenStopped", MidpVersionable.MIDP_2).addParameters(PROP_RESET_ANIMATION_WHEN_STOPPED)); //NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter (),
                new SVGFileAcceptPresenter (),
                //accept
                new MidpAcceptProducerKindPresenter ().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
                new MidpAcceptTrensferableKindPresenter().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
                // code
                createSetterPresenter(),
                MidpCodePresenterSupport.createAddImportPresenter(),
                new SwitchDisplayableParameterPresenter() {
                    public String generateSwitchDisplayableParameterCode() {
                        return CodeReferencePresenter.generateAccessCode(getComponent()) + ".getSvgCanvas ()"; // NOI18N
                    }
                },
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_SVG_IMAGE),
                // screen
                new SVGAnimatorWrapperDisplayPresenter ()
                );
    }

}
