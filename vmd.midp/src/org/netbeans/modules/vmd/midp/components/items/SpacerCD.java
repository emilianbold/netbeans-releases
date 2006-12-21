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

import java.util.ArrayList;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;

import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorArrayInteger;

/**
 *
 * @author Karol Harezlak
 */

public class SpacerCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Spacer"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_16.png"; // NOI18N
    
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
                new PropertyDescriptor(PROP_MIN_HEIGHT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (1), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_MIN_WIDTH, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (16), false, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
//            .removeProperty(ItemCD.PROP_DEFAULT_COMMAND);
                  .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Minmum size", new PropertyEditorArrayInteger(), PROP_MIN_WIDTH, PROP_MIN_HEIGHT);
  }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_MIN_WIDTH, PROP_MIN_HEIGHT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(PROP_MIN_WIDTH, PROP_MIN_HEIGHT))
                .addSetters(MidpSetter.createSetter("setSetMinimumSize", MidpVersionable.MIDP_2).addParameters(PROP_MIN_WIDTH, PROP_MIN_HEIGHT));
    }
    
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        for (Presenter presenter : presenters.toArray(new Presenter[presenters.size()])) {
            if (presenter instanceof InspectorFolderPresenter) {
                presenters.remove(presenter);
            }
            if(presenter instanceof ActionsPresenter) {
                for (Action action : ((ActionsPresenter) presenter).getActions()) {
                    presenters.remove(presenter);
                }
            }
        }
        MidpActionsSupport.addCommonActionsPresenters(presenters, true, true, true, true, true);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                InspectorFolderPresenter.create(true)
                
        );
    }

    

}
