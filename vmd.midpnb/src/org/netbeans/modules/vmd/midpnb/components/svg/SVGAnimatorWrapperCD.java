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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorFloat;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.openide.util.NbBundle;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.common.AbstractAcceptPresenter;

/**
 *
 * @author Karol Harezlak
 */
public class SVGAnimatorWrapperCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGAnimatorWrapper"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_animator_wrapper_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_animator_wrapper_64.png"; // NOI18N

    public static final String PROP_SVG_IMAGE = "svgImage"; //NOI18N
    public static final String PROP_START_ANIM_IMMEDEATELY = "startAnimationImmideately"; //NOI18N
    public static final String PROP_TIME_INCREMENT = "animationTimeIncrement"; //NOI18N
    public static final String PROP_RESET_ANIMATION_WHEN_STOPPED = "resetAnimationWhenStopped"; //NOI18N

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.FOREVER;
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_START_ANIM_IMMEDEATELY, MidpTypes.createBooleanValue (true));
        component.writeProperty (PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.createBooleanValue (true));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_SVG_IMAGE, SVGImageCD.TYPEID, PropertyValue.createNull(), false, true, Versionable.FOREVER),
            new PropertyDescriptor(PROP_START_ANIM_IMMEDEATELY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, Versionable.FOREVER),
            new PropertyDescriptor(PROP_TIME_INCREMENT, MidpTypes.TYPEID_FLOAT, MidpTypes.createFloatValue(0.1f), false, true, Versionable.FOREVER),
            new PropertyDescriptor(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("SVG Image", PropertyEditorResourcesComboBox.create(SVGImageCD.TYPEID, NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE")), PROP_SVG_IMAGE)
                .addProperty("Start Animation Immediately", PropertyEditorBooleanUC.createInstance(), PROP_START_ANIM_IMMEDEATELY)
                .addProperty("Animation Time Increment", PropertyEditorFloat.createInstance(), PROP_TIME_INCREMENT)
                .addProperty("Reset Animation When Stopped", PropertyEditorBooleanUC.createInstance(), PROP_RESET_ANIMATION_WHEN_STOPPED);
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters (MidpParameter.create (PROP_SVG_IMAGE, PROP_START_ANIM_IMMEDEATELY, PROP_TIME_INCREMENT, PROP_RESET_ANIMATION_WHEN_STOPPED))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("setTimeIncrement", MidpVersionable.MIDP_2).addParameters (PROP_TIME_INCREMENT))
            .addSetters (MidpSetter.createSetter ("setStartAnimationImmediately", MidpVersionable.MIDP_2).addParameters (PROP_START_ANIM_IMMEDEATELY))
            .addSetters (MidpSetter.createSetter ("setResetAnimationWhenStopped", MidpVersionable.MIDP_2).addParameters (PROP_RESET_ANIMATION_WHEN_STOPPED));
    }

    private static class AcceptSVGFilesPresenter extends AbstractAcceptPresenter {
        public AcceptSVGFilesPresenter() {
            super(Kind.TRANSFERABLE);
        }

        public boolean isAcceptable (Transferable transferable) {
            DataFlavor[] df = transferable.getTransferDataFlavors();
            for (DataFlavor dataFlavor : df) {
                try {
                    java.lang.Object obj = transferable.getTransferData(dataFlavor);
                } catch (Exception ex) {
                }
            }
            return true;
        }

        public ComponentProducer.Result accept (Transferable transferable) {
            return new ComponentProducer.Result ((DesignComponent) null);
        }
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // accept
            new AcceptSVGFilesPresenter(),
            // code
            createSetterPresenter (),
            MidpCustomCodePresenterSupport.createAddImportPresenter ()
        );
    }
    
}
