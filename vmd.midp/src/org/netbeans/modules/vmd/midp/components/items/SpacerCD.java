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
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.FormCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorLayout;
import org.netbeans.modules.vmd.midp.screen.display.SpacerDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorPreferredSize;

/**
 *
 * @author Karol Harezlak
 */

public class SpacerCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Spacer"); // NOI18N
    
    public static final String PROP_MIN_WIDTH = "minWidth" ;  // NOI18N
    public static final String PROP_MIN_HEIGHT = "minHeight" ;  // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_MIN_HEIGHT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(1), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_MIN_WIDTH, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(16), false, true, MidpVersionable.MIDP_2)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Layout"), PropertyEditorLayout.createInstance(), ItemCD.PROP_LAYOUT) // NOI18N
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Preferred_Width"), // NOI18N
                    PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(SpacerCD.class, "LBL_Spacer_Preferred_Width"),  // NOI18N
                        NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Preferred_Width")), ItemCD.PROP_PREFERRED_WIDTH) // NOI18N
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Preferred_Height"), // NOI18N
                    PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(SpacerCD.class, "LBL_Spacer_Preferred_Height"), // NOI18N
                        NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Preferred_Height")), ItemCD.PROP_PREFERRED_HEIGHT) // NOI18N
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Minimum_Width"), // NOI18N
                    PropertyEditorNumber.createPositiveIntegerInstance(false, NbBundle.getMessage(SpacerCD.class, "LBL_Spacer_Minimum_Width")), PROP_MIN_WIDTH) // NOI18N
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Minimum_Height"), // NOI18N
                    PropertyEditorNumber.createPositiveIntegerInstance(false, NbBundle.getMessage(SpacerCD.class, "LBL_Spacer_Minimum_Height")), PROP_MIN_HEIGHT); // NOI18N
    }
   
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(ItemCD.PROP_LABEL, ItemCD.PROP_PREFERRED_WIDTH, ItemCD.PROP_PREFERRED_HEIGHT))
                .addParameters (ItemCode.createCommandParameter ())
                .addParameters (ItemCode.createItemCommandListenerParameter ())
                .addParameters (ItemCode.createItemLayoutParameter ())
                .addParameters (ItemCode.createDefaultCommandParameter ())
                .addSetters(MidpSetter.createSetter("setItemCommandListener", MidpVersionable.MIDP_2).addParameters(ItemCode.PARAM_ITEM_COMMAND_LISTENER)) // NOI18N
                .addSetters(MidpSetter.createSetter("setLayout", MidpVersionable.MIDP_2).addParameters(ItemCode.PARAM_LAYOUT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setPreferredSize", MidpVersionable.MIDP_2).addParameters(ItemCD.PROP_PREFERRED_WIDTH, ItemCD.PROP_PREFERRED_HEIGHT)) // NOI18N
                .addParameters(MidpParameter.create(PROP_MIN_WIDTH, PROP_MIN_HEIGHT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(PROP_MIN_WIDTH, PROP_MIN_HEIGHT))
                .addSetters(MidpSetter.createSetter("setSetMinimumSize", MidpVersionable.MIDP_2).addParameters(PROP_MIN_WIDTH, PROP_MIN_HEIGHT)); // NOI18N
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        Presenter[] pa = presenters.toArray(new Presenter[presenters.size()]);
        for (Presenter presenter : pa) {
            if (presenter instanceof PropertiesPresenter) {
                for (DesignPropertyDescriptor pd : ((PropertiesPresenter)presenter).getDesignPropertyDescriptors()) {
                    if (pd.getPropertyDisplayName().equalsIgnoreCase(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Label")))
                        presenters.remove(presenter);
                }
            }
        }
        DocumentSupport.removePresentersOfClass(presenters, CodeSetterPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, InspectorFolderPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, ActionsPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        MidpActionsSupport.addCommonActionsPresenters(presenters, true, true, true, true, true);
        MidpActionsSupport.addMoveActionPresenter(presenters, FormCD.PROP_ITEMS);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                new InspectorFolderComponentPresenter(true),
                // screen
                new SpacerDisplayPresenter()
                );
    }     
}
