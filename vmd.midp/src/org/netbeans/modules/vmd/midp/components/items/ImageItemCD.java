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
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageFileAcceptPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midp.screen.display.ImageItemDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.injector.ImageItemInjectorPresenter;
import org.openide.util.NbBundle;

import java.util.*;
import org.netbeans.modules.vmd.midp.codegen.MidpDatabindingCodeSupport;

/**
 *
 * @author Karol Harezlak
 */
public class ImageItemCD extends ComponentDescriptor {
    
    private static Map<String, PropertyValue> appearanceValues;
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.ImageItem"); // NOI18N
    
    public static final String PROP_ALT_TEXT = "altText"; // NOI18N
    public static final String PROP_IMAGE = ImageCD.PROP_IMAGE; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_ALT_TEXT, MidpTypes.createStringValue (NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Missing_Image"))); // NOI18N
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor (PROP_ALT_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(ItemCD.PROP_APPEARANCE_MODE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN), false, true, MidpVersionable.MIDP_2)
        );
    }
    
    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        presenters.addAll(MidpDatabindingCodeSupport.createDatabindingPresenters(PROP_IMAGE
                                                                                 ,"getImage()", //NOI18N
                                                                                 TYPEID,
                                                                                 MidpDatabindingCodeSupport.FeatureType.ImageItem_FEATURE_IMAGE));
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Alternate_Text"), // NOI18N
                    PropertyEditorString.createInstance(NbBundle.getMessage(ImageItemCD.class,
                        "LBL_ImageItem_Alternate_Text")), PROP_ALT_TEXT) // NOI18N
                .addProperty(NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Appearance"), // NOI18N
                    PropertyEditorComboBox.createInstance(getAppearanceValues(), TYPEID,
                        NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Appearance_RB_LABEL"), // NOI18N
                        NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Appearance_UCLABEL")), ItemCD.PROP_APPEARANCE_MODE) // NOI18N
                .addProperty(NbBundle.getMessage(ImageItemCD.class, "DISP_ImageItem_Image"), PropertyEditorResource.createImagePropertyEditorWithDatabinding(), PROP_IMAGE); // NOI18N
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_ALT_TEXT, PROP_IMAGE))
                .addParameters (ItemCode.createAppearanceModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, PROP_IMAGE, ItemCD.PROP_LAYOUT, PROP_ALT_TEXT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(ItemCD.PROP_LABEL, PROP_IMAGE, ItemCD.PROP_LAYOUT, PROP_ALT_TEXT, ItemCode.PARAM_APPEARANCE_MODE))
                .addSetters(MidpSetter.createSetter("setAltText", MidpVersionable.MIDP).addParameters(PROP_ALT_TEXT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setImage", MidpVersionable.MIDP).addParameters(PROP_IMAGE)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_IMAGE),
                //accept
                new ImageFileAcceptPresenter(ImageItemCD.PROP_IMAGE, ImageCD.TYPEID, "jpg", "gif", "png"), //NOI18N
                new MidpAcceptProducerKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE),
                new MidpAcceptTrensferableKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE),
                DatabindingItemAcceptPresenter.create(PROP_IMAGE, ItemCD.PROP_LABEL),
                // screen
                new ImageItemDisplayPresenter(),
                new ImageItemInjectorPresenter ()
        );
    }
    
    public static Map<String, PropertyValue> getAppearanceValues() {
        if (appearanceValues == null) {
            appearanceValues = new TreeMap<String, PropertyValue>();
            appearanceValues.put("PLAIN", MidpTypes.createIntegerValue(ItemCD.VALUE_PLAIN)); // NOI18N
            appearanceValues.put("HYPERLINK", MidpTypes.createIntegerValue(ItemCD.VALUE_HYPERLINK)); // NOI18N
            appearanceValues.put("BUTTON", MidpTypes.createIntegerValue(ItemCD.VALUE_BUTTON)); // NOI18N
        }
        return appearanceValues;
    }

}
