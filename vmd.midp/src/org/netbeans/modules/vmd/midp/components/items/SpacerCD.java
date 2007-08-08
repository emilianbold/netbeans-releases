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
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorArrayInteger;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorLayout;
import org.netbeans.modules.vmd.midp.screen.display.SpacerDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Preferred_Size"), PropertyEditorArrayInteger.create(), ItemCD.PROP_PREFERRED_WIDTH, ItemCD.PROP_PREFERRED_HEIGHT) // NOI18N
                .addProperty(NbBundle.getMessage(SpacerCD.class, "DISP_Spacer_Minimum_size"), new PropertyEditorArrayInteger(), PROP_MIN_WIDTH, PROP_MIN_HEIGHT); // NOI18N
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
