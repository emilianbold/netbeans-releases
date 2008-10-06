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

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midp.screen.display.StringItemDisplayPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.codegen.MidpDatabindingCodeSupport;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetAbstractCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */

public class StringItemCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.StringItem"); // NOI18N

    public static final String PROP_TEXT = "text"; // NOI18N
    public static final String PROP_FONT = "font"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
         return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(ItemCD.PROP_APPEARANCE_MODE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN), false, true, MidpVersionable.MIDP_2)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        presenters.addAll(MidpDatabindingCodeSupport.createDatabindingPresenters(PROP_TEXT
                                                                                 ,"getText()"
                                                                                 ,TYPEID
                                                                                 ,MidpDatabindingCodeSupport.FeatureType.StringItem_FEATURE_TEXT));
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Text"), // NOI18N
                    PropertyEditorString.createInstanceWithDatabinding(NbBundle.getMessage(StringItemCD.class, "LBL_StringItem_Text")), PROP_TEXT) // NOI18N
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance"), // NOI18N
                    PropertyEditorComboBox.createInstance(ImageItemCD.getAppearanceValues(), TYPEID,
                        NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance_RB_LABEL"), // NOI18N
                        NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance_UCLABEL")), ItemCD.PROP_APPEARANCE_MODE) // NOI18N
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Font"), PropertyEditorResource.createFontPropertyEditor(), PROP_FONT); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_TEXT, PROP_FONT))
                .addParameters (ItemCode.createAppearanceModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, PROP_TEXT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(ItemCD.PROP_LABEL, PROP_TEXT, ItemCode.PARAM_APPEARANCE_MODE))
                .addSetters(MidpSetter.createSetter("setText", MidpVersionable.MIDP).addParameters(PROP_TEXT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setFont", MidpVersionable.MIDP_2).addParameters(PROP_FONT)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FONT),
            // screen
            new StringItemDisplayPresenter(),
            // accept
            new MidpAcceptProducerKindPresenter().addType(FontCD.TYPEID, PROP_FONT),
            new MidpAcceptTrensferableKindPresenter().addType(FontCD.TYPEID, PROP_FONT),
            DatabindingItemAcceptPresenter.create(PROP_TEXT, ItemCD.PROP_LABEL)
       );   
    }
    
}
