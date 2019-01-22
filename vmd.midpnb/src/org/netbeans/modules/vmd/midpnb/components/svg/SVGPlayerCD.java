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
import org.netbeans.modules.vmd.midpnb.components.SVGImageAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.general.SVGFileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.screen.display.SVGPlayerDisplayPresenter;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.midpnb.actions.EditSVGFileAction;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorResourceLazyInitFactory;
import org.openide.util.actions.SystemAction;

/**
 *
 * 
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
        MidpProjectSupport.addLibraryToProject(component.getDocument(), MIDP_NB_SVG_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_SVG_IMAGE, SVGImageCD.TYPEID, PropertyValue.createNull(), true, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_START_ANIM_IMMEDIATELY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_TIME_INCREMENT, MidpTypes.TYPEID_FLOAT, MidpTypes.createFloatValue(0.1f), false, true, Versionable.FOREVER),
                new PropertyDescriptor(PROP_RESET_ANIMATION_WHEN_STOPPED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, true, Versionable.FOREVER)
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
                    PropertyEditorResourceLazyInitFactory.createSVGImageEditorPropertyEditor(), PROP_SVG_IMAGE) //NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_StartAnimationImmediately"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_StartAnimationImmediately")), PROP_START_ANIM_IMMEDIATELY) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_AnimationTimeIncrement"), // NOI18N
                    PropertyEditorNumber.createFloatInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_AnimationTimeIncrement")), PROP_TIME_INCREMENT) // NOI18N
                .addProperty(NbBundle.getMessage(SVGPlayerCD.class, "DISP_SVGPlayer_ResetAnimationWhenStopped"), // NOI18N
                    PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SVGPlayerCD.class, "LBL_SVGPlayer_ResetAnimationWhenStopped")), PROP_RESET_ANIMATION_WHEN_STOPPED); // NOI18N
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
                //accept
                new SVGFileAcceptPresenter (),
                new MidpAcceptProducerKindPresenter ().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
                new SVGImageAcceptTrensferableKindPresenter().addType(SVGImageCD.TYPEID, PROP_SVG_IMAGE),
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
                new SVGPlayerDisplayPresenter (),
                //actions
                ActionsPresenter.create(20, SystemAction.get(EditSVGFileAction.class))
                );
    }

}
