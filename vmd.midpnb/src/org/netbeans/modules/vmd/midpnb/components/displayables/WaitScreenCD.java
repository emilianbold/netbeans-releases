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

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.components.resources.CancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.propertyeditors.MidpNbPropertiesCategories;
import org.netbeans.modules.vmd.midpnb.propertyeditors.TaskEditorElement;
import org.openide.util.NbBundle;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Karol Harezlak
 */
public final class WaitScreenCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.WaitScreen"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/wait_screen_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/wait_screen_32.png"; // NOI18N
    
    public static final String PROP_TASK = "task"; //NOI18N
     
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(AbstractInfoScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    @Override
    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject(component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_BASIC); //NOI18N
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_TASK, CancellableTaskCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(MidpNbPropertiesCategories.CATEGORY_TASK) // TODO
                   .addProperty(NbBundle.getMessage(WaitScreenCD.class, "DISP_WaitScreen_Task"), PropertyEditorResource.createInstance(new TaskEditorElement(), // NOI18N
                        NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), // NOI18N
                        NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NONE")), PROP_TASK); // NOI18N
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters (MidpParameter.create (PROP_TASK))
            .addParameters (MidpCustomCodePresenterSupport.createWaitScreenCommandParameter ())
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters (MidpSetter.createSetter ("setTask", MidpVersionable.MIDP_2).addParameters (PROP_TASK)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // accept
            new MidpAcceptTrensferableKindPresenter().addType(CancellableTaskCD.TYPEID, PROP_TASK),
            //properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_TASK),
            new MidpAcceptProducerKindPresenter().addType(CancellableTaskCD.TYPEID, PROP_TASK)
        );
    }

}
