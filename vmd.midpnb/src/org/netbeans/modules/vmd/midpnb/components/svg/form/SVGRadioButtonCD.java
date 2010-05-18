/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorButtonGroup;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 * @author ads
 */
public class SVGRadioButtonCD extends ComponentDescriptor{

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGRadioButton"); // NOI18N
    
    public static final String PROP_SELECTED = "selected";       // NOI18N
    
    public static final String PROP_BUTTON_GROUP = "buttonGroup";// NOI18N
    
    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (SVGComponentCD.TYPEID, TYPEID, true, false);
    }
    
    static {
        SVGComponentCD.addPairType( TYPEID, SVGRadioButtonEventSourceCD.TYPEID );
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.model.ComponentDescriptor#postInitialize(org.netbeans.modules.vmd.api.model.DesignComponent)
     */
    @Override
    public void postInitialize( DesignComponent component ) {
        /*Collection<DesignComponent> components = 
            component.getParentComponent().getComponents();
        for (DesignComponent designComponent : components) {
            if ( !designComponent.getType().equals( SVGButtonGroupCD.TYPEID ) ){
                continue;
            }
            PropertyValue propValue =  designComponent.readProperty( 
                    SVGButtonGroupCD.PROP_BUTTONS );
            if ( propValue == null ){
                continue;
            }
            for (PropertyValue propertyValue : propValue.getArray()) {
                if ( propertyValue.getComponent() == component ){
                    component.writeProperty( PROP_BUTTON_GROUP, 
                            MidpTypes.createJavaCodeValue(
                                    CodeReferencePresenter.generateAccessCode( 
                                            designComponent)));
                }
            }
        }*/
    }

    
    @Override
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
                new PropertyDescriptor( SVGLabelCD.PROP_TEXT, 
                        MidpTypes.TYPEID_JAVA_LANG_STRING, 
                        MidpTypes.createStringValue( "" ), true, true,
                        MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_SELECTED, 
                        MidpTypes.TYPEID_BOOLEAN, 
                        MidpTypes.createBooleanValue (Boolean.FALSE), false, false, 
                        MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_BUTTON_GROUP, 
                                SVGButtonGroupCD.TYPEID, 
                                PropertyValue.createNull(), true, true, 
                                MidpVersionable.MIDP_2)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGRadioButtonCD.class, 
                        "DISP_Text"), 
                        PropertyEditorString.createInstance(
                                NbBundle.getMessage(SVGRadioButtonCD.class, 
                                "LBL_SVGRadioButton_Text")), SVGLabelCD.PROP_TEXT).
                 addProperty(NbBundle.getMessage(SVGRadioButtonCD.class, 
                                "DISP_IsSelected"), 
                         PropertyEditorBooleanUC.createInstance(), PROP_SELECTED).
                 addProperty(NbBundle.getMessage(SVGRadioButtonCD.class, 
                         "DISP_ButtonGroup"), 
                  PropertyEditorButtonGroup.createInstance(), PROP_BUTTON_GROUP); // NOI18N
                
    }
    
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ().
                addParameters(MidpParameter.create(SVGLabelCD.PROP_TEXT)).
                addSetters(MidpSetter.createSetter("setText",             // NOI18N
                        MidpVersionable.MIDP_2).addParameters(SVGLabelCD.
                                PROP_TEXT));
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                createSetterPresenter(),
                //code
                MidpCustomCodePresenterSupport.createSVGComponentCodePresenter(TYPEID),
                MidpCodePresenterSupport.createAddImportPresenter(),
               // new SVGCodeFooter( SVGRadioButtonEventSourceCD.TYPEID ),
                //delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter( 
                        SVGRadioButtonCD.PROP_BUTTON_GROUP )
        );
    }

}
