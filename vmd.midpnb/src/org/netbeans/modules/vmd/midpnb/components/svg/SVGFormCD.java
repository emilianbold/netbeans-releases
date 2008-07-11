/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.util.ArrayList;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGCheckBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComboBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGLabelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGRadioButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGSpinnerCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGTextFieldCD;

/**
 * This class represents Component Descriptor for SVG Form component.
 * 
 * @author Andrew Korostelev
 */
public class SVGFormCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGForm"); // NOI18N
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_form_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_form_32.png"; // NOI18N
    

    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(SVGPlayerCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {

        return null;
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        //DocumentSupport.removePresentersOfClass(presenters, CodeSetterPresenter.class);
        super.gatherPresenters(presenters);
    }

    private Presenter createSetterPresenter() {
        return new CodeSetterPresenter().addParameters(MidpCustomCodePresenterSupport.createDisplayParameter()).addParameters(MidpParameter.create(SVGPlayerCD.PROP_SVG_IMAGE, SVGPlayerCD.PROP_START_ANIM_IMMEDIATELY, SVGPlayerCD.PROP_TIME_INCREMENT, SVGPlayerCD.PROP_RESET_ANIMATION_WHEN_STOPPED)).addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(SVGPlayerCD.PROP_SVG_IMAGE, MidpCustomCodePresenterSupport.PARAM_DISPLAY)).addSetters(MidpSetter.createSetter("setTimeIncrement", MidpVersionable.MIDP_2).addParameters(SVGPlayerCD.PROP_TIME_INCREMENT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setStartAnimationImmediately", MidpVersionable.MIDP_2).addParameters(SVGPlayerCD.PROP_START_ANIM_IMMEDIATELY)) //NOI18N
                .addSetters(MidpSetter.createSetter("setResetAnimationWhenStopped", MidpVersionable.MIDP_2).addParameters(SVGPlayerCD.PROP_RESET_ANIMATION_WHEN_STOPPED)); //NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                //code
                MidpCodePresenterSupport.createAddImportPresenter("org.netbeans.microedition.svg.*"),
                createSetterPresenter(),
                new SVGFormPresenterCodeClassInitHeaderFooterPresenter());
    }

    private class SVGFormPresenterCodeClassInitHeaderFooterPresenter extends CodeClassInitHeaderFooterPresenter {

        @Override
        public void generateClassInitializationHeader(MultiGuardedSection section) {
            //Do nothing
        }

        @Override
        public void generateClassInitializationFooter(MultiGuardedSection section) {
            Collection<DesignComponent> components = getComponent().getComponents();
            for (DesignComponent component : components) {
                if (component.getType() == SVGButtonCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGCheckBoxCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGComboBoxCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGLabelCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGListCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGSpinnerCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGListCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGRadioButtonCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                } else if (component.getType() == SVGTextFieldCD.TYPEID) {
                    generateSVGFormAddComponentCode(section, getComponent(), component);
                }
            }
        }
    }

    private static void generateSVGFormAddComponentCode(MultiGuardedSection section, DesignComponent svgForm, DesignComponent componentToAdd) {
        section.getWriter().write(CodeReferencePresenter.generateDirectAccessCode(svgForm));
        section.getWriter().write(".add(" + CodeReferencePresenter.generateDirectAccessCode(componentToAdd) + ");\n"); //NOI18N

    }
}
