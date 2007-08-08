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
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.listeners.ItemStateListenerCD;
import org.netbeans.modules.vmd.midp.flow.FlowItemCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.screen.FormResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midp.screen.display.FormDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ScreenMoveArrayAcceptPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author David Kaspar
 */

public final class FormCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Form"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/form_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/form_32.png"; // NOI18N
    
    public static final String PROP_ITEMS = "items"; //NOI18N
    public static final String PROP_ITEM_STATE_LISTENER = "itemStateListener"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ScreenCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_ITEMS, ItemCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(ItemCD.TYPEID), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_ITEM_STATE_LISTENER, ItemStateListenerCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP)
                );
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_ITEMS, PROP_ITEM_STATE_LISTENER))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE, PROP_ITEMS))
                .addSetters(MidpSetter.createSetter("setItemStateListener", MidpVersionable.MIDP).addParameters(PROP_ITEM_STATE_LISTENER)) //NOI18N
                .addSetters(MidpSetter.createSetter("insert", MidpVersionable.MIDP).setArrayParameter(PROP_ITEMS).addParameters(PROP_ITEMS, Parameter.PARAM_INDEX)) //NOI18N
                .addSetters(MidpSetter.createSetter("set", MidpVersionable.MIDP).setArrayParameter(PROP_ITEMS).addParameters(PROP_ITEMS, Parameter.PARAM_INDEX)); //NOI18N
    }
    
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, ScreenResourceCategoriesPresenter.class);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // accept
                FormAcceptPresenterSupport.createImageAcceptPresenter(),
                FormAcceptPresenterSupport.createFileAcceptPresenter("png", "jpg", "gif"), //NOI18N
                new ScreenMoveArrayAcceptPresenter(FormCD.PROP_ITEMS, ItemCD.TYPEID),
                new AcceptTypePresenter(ItemCD.TYPEID) {
                    @Override
                    protected void notifyCreated(DesignComponent component) {
                        super.notifyCreated(component);
                        MidpArraySupport.append(getComponent(), PROP_ITEMS, component);
                    }
                },
                // action
                AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, ItemCD.TYPEID),
                // inspector
                MidpInspectorSupport.createComponentElementsCategory(NbBundle.getMessage(FormCD.class, "DISP_InspectorCategory_Items"), createOrderingArrayController() , ItemCD.TYPEID), // NOI18N
                // code
                createSetterPresenter(),
                // flow
                new FlowItemCommandPinOrderPresenter(),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_ITEM_STATE_LISTENER),
                // screen
                new FormDisplayPresenter(),
                new FormResourceCategoriesPresenter()
                );
    }
    
    private List<InspectorOrderingController> createOrderingArrayController() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_ITEMS, 0, ItemCD.TYPEID));
    }

}
