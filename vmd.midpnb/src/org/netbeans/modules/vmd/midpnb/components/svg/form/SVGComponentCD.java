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
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGComponentCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.svg.SVGComponent"); // NOI18N

    public static final String PROP_ID = "id"; // NOI18N
    public static final String PROP_FOCUSABLE="isFocusable";// NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (ClassCD.TYPEID, TYPEID, true, true);
        
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor(PROP_ID, MidpTypes.TYPEID_JAVA_LANG_STRING, 
                    PropertyValue.createNull(), false, false, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_FOCUSABLE, MidpTypes.TYPEID_BOOLEAN, 
                    MidpTypes.createBooleanValue (Boolean.TRUE), false, false, 
                    MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter().
                addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES).
                    addProperty(NbBundle.getMessage(SVGComponentCD.class, 
                            "DISP_SVGComponentIsFocusable"), 
                            PropertyEditorBooleanUC.createInstance(), PROP_FOCUSABLE); // NOI18N
            
    }
    
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ().
                addParameters(MidpParameter.create(PROP_FOCUSABLE)).
                addSetters(MidpSetter.createSetter("setFocusable", 
                        MidpVersionable.MIDP_2).addParameters(PROP_FOCUSABLE));
    }
    
    public static TypeID getEventType( TypeID type ){
        return PAIR_TYPES.get( type );
    }
    
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                // delete
                DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
                new DeletePresenter() {
                    @Override
                    protected void delete() {
                        DesignComponent svgForm = getComponent().getParentComponent();
                        if (svgForm == null) {
                            return;
                        }
                        ArraySupport.remove (svgForm, SVGFormCD.PROP_COMPONENTS, getComponent());
                    }

                });
    }
    
    static void addPairType( TypeID typeId , TypeID eventTypeId ){
        assert eventTypeId.getString().endsWith("EventSource") :
            "Check type of paired component descriptor. It ends with " + 
            eventTypeId.getString()+". Possibly it is not correct";// NOI18N
        PAIR_TYPES.put( typeId , eventTypeId );
    }
    
    private static Map<TypeID,TypeID> PAIR_TYPES = new HashMap<TypeID, TypeID>();

}
