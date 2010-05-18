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
