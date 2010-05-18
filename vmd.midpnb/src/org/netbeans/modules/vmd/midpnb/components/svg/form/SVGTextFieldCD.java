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
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
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
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class SVGTextFieldCD extends ComponentDescriptor{

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGTextField"); // NOI18N
    
    public static final String DEFAULT_TEXT = "Some Text"; // NOI18N
    
    public static final String PROP_EDITABLE = "editable"; // NOI18N
    
    //public static final String PROP_CARET_VISIBLE= "caretVisible"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (SVGComponentCD.TYPEID, TYPEID, true, true);
    }
    
    static {
        SVGComponentCD.addPairType( TYPEID, SVGTextFieldEventSourceCD.TYPEID );
    }

    @Override
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor( SVGLabelCD.PROP_TEXT, 
                        MidpTypes.TYPEID_JAVA_LANG_STRING, 
                        MidpTypes.createStringValue(DEFAULT_TEXT), false, true, 
                        MidpVersionable.MIDP_2),
                new PropertyDescriptor( PROP_EDITABLE, 
                                MidpTypes.TYPEID_BOOLEAN, 
                                MidpTypes.createBooleanValue(true ), false, false,
                                MidpVersionable.MIDP_2)
                /*new PropertyDescriptor( PROP_CARET_VISIBLE, 
                                MidpTypes.TYPEID_BOOLEAN, 
                                MidpTypes.createBooleanValue(true ), false, false,
                                MidpVersionable.MIDP_2)  */              
                
                
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES).
                addProperty(NbBundle.getMessage(SVGTextFieldCD.class, 
                        "DISP_Text"), 
                        PropertyEditorString.createInstance(
                                NbBundle.getMessage(SVGTextFieldCD.class, 
                                "LBL_SVGTextField_Text")), SVGLabelCD.PROP_TEXT).
                 addProperty(NbBundle.getMessage(SVGTextFieldCD.class, 
                                "DISP_IsEditable"), 
                         PropertyEditorBooleanUC.createInstance(), PROP_EDITABLE)/*.
                 addProperty(NbBundle.getMessage(SVGTextFieldCD.class, 
                         "DISP_IsCaretVisible"), 
                         PropertyEditorBooleanUC.createInstance(), PROP_CARET_VISIBLE)*/; // NOI18N
                
    }
    
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ().
                addParameters(MidpParameter.create(SVGLabelCD.PROP_TEXT,
                        PROP_EDITABLE /*, PROP_CARET_VISIBLE */)).
                addSetters(MidpSetter.createSetter("setText", 
                        MidpVersionable.MIDP_2).addParameters(SVGLabelCD.
                                PROP_TEXT)).
                 addSetters(MidpSetter.createSetter("setEditable", 
                       MidpVersionable.MIDP_2).addParameters( PROP_EDITABLE))/*.
                 addSetters(MidpSetter.createSetter("setCaretVisible", 
                       MidpVersionable.MIDP_2).addParameters( PROP_CARET_VISIBLE))*/;
    }
    
    @Override
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                createSetterPresenter(),
                //code
                MidpCustomCodePresenterSupport.createSVGComponentCodePresenter(TYPEID),
                MidpCodePresenterSupport.createAddImportPresenter()
                //new SVGCodeFooter( SVGTextFieldEventSourceCD.TYPEID )
        );
    }
    
    
}
