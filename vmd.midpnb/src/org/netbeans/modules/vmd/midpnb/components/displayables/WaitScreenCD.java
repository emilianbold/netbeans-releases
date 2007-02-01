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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.eventhandler.PropertyEditorEventHandler;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.openide.util.NbBundle;

/**
 * @author Karol Harezlak
 */

public final class WaitScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.WaitScreen"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/wait_screen16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/wait_screen64.png"; // NOI18N
    
    private static final String PROP_TASK = "task"; //NOI18N
    private static final String PROP_SUCCESS_ACTION = "successAction"; //NOI18N
    private static final String PROP_FAILURE_ACTION = "failureAction"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(AbstractScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_TASK, SimpleCancellableTaskCD.TYPEID, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_SUCCESS_ACTION, EventSourceCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_FAILURE_ACTION, EventSourceCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP_2)
        );
    }
    
     private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES) 
                   .addProperty("Task", PropertyEditorResourcesComboBox.creater(SimpleCancellableTaskCD.TYPEID, NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NONE")), PROP_TASK)
                   .addProperty("Success Action", PropertyEditorEventHandler.createInstance(), PROP_SUCCESS_ACTION)
                   .addProperty("Failure Action", PropertyEditorEventHandler.createInstance(), PROP_FAILURE_ACTION);  
    }
    
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            createPropertiesPresenter()
        );
    } 
    
}
