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
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorInteger;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.eventhandler.PropertyEditorEventHandler;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.openide.util.NbBundle;

/**
 * @author Karol Harezlak
 */

public final class SVGWaitScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGWaitScreen"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_wait_screen_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_wait_screen_64.png"; // NOI18N
    
    private static final String PROP_TASK = "task"; //NOI18N
    private static final String PROP_SUCCESS_ACTION = "successAction"; //NOI18N
    private static final String PROP_FAILURE_ACTION = "failureAction"; //NOI18N
    private static final String PROP_SVG_IMAGE = "svgImage"; //NOI18N
    private static final String PROP_START_ANIM_IMMEDEATELY = "startAnimationImmideately"; //NOI18N
    private static final String PROP_ANIM_TIME_INCREMENT = "animationTimeIncrement"; //NOI18N
    private static final String PROP_RESET_ANIMATION_WHEN_STOPPED = "resetAnimationWhenStopped"; //NOI18N
    private static final String PROP_FULL_SCREEN = "fullScreen"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_TASK, SimpleCancellableTaskCD.TYPEID, PropertyValue.createNull(), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_SUCCESS_ACTION, EventSourceCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER),
                new PropertyDescriptor(PROP_FAILURE_ACTION, EventSourceCD.TYPEID, PropertyValue.createNull(), false, false, Versionable.FOREVER),
                new PropertyDescriptor(PROP_SVG_IMAGE, SVGImageCD.TYPEID, PropertyValue.createNull(), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_START_ANIM_IMMEDEATELY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_ANIM_TIME_INCREMENT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(0 /*0.1f*/), false, true, Versionable.FOREVER), // TODO
                new PropertyDescriptor(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_FULL_SCREEN, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, Versionable.FOREVER)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("SVG Image", PropertyEditorResourcesComboBox.creater(SVGImageCD.TYPEID, NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE")), PROP_SVG_IMAGE)
                    .addProperty("Task", PropertyEditorResourcesComboBox.creater(SimpleCancellableTaskCD.TYPEID, NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NONE")), PROP_TASK)
                    .addProperty("Success Action", PropertyEditorEventHandler.createInstance(), PROP_SUCCESS_ACTION)
                    .addProperty("Failure Action", PropertyEditorEventHandler.createInstance(), PROP_FAILURE_ACTION)
                    .addProperty("Start Animation Immideately", PropertyEditorBooleanUC.createInstance(), PROP_START_ANIM_IMMEDEATELY)
                    .addProperty("Animation Time Increment", PropertyEditorInteger.createInstance(), PROP_ANIM_TIME_INCREMENT)
                    .addProperty("Reset Animation When Stopped", PropertyEditorBooleanUC.createInstance(), PROP_RESET_ANIMATION_WHEN_STOPPED)
                    .addProperty("Full Screen", PropertyEditorBooleanUC.createInstance(), PROP_FULL_SCREEN);
    }
    
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                createPropertiesPresenter()
                );
    }
    
}
