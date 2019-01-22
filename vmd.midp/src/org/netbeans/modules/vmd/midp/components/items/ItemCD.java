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

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.actions.EditDependencyPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.FormCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.RootCode;
import org.netbeans.modules.vmd.midp.components.listeners.ItemCommandListenerCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.screen.display.ScreenMoveArrayAcceptPresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.*;
import org.netbeans.modules.vmd.midp.screen.DisplayableResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ItemDisplayPresenter;

import java.util.*;
import org.netbeans.modules.vmd.midp.codegen.MidpDatabindingCodeSupport;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.openide.util.NbBundle;


/**
 *
 * 
 */

public class ItemCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Item"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_32.png"; // NOI18N

    public static final int VALUE_PLAIN = 0;
    public static final int VALUE_HYPERLINK = 1;
    public static final int VALUE_BUTTON = 2;

    public static final int VALUE_LAYOUT_DEFAULT = 0;
    public static final int VALUE_LAYOUT_LEFT = 1;
    public static final int VALUE_LAYOUT_RIGHT = 2;
    public static final int VALUE_LAYOUT_CENTER = 3;
    public static final int VALUE_LAYOUT_TOP = 0x10;
    public static final int VALUE_LAYOUT_BOTTOM = 0x20;
    public static final int VALUE_LAYOUT_VCENTER = 0x30;
    public static final int VALUE_LAYOUT_NEWLINE_BEFORE = 0x100;
    public static final int VALUE_LAYOUT_NEWLINE_AFTER = 0x200;
    public static final int VALUE_LAYOUT_SHRINK = 0x400;
    public static final int VALUE_LAYOUT_VSHRINK = 0x800;
    public static final int VALUE_LAYOUT_EXPAND = 0x1000;
    public static final int VALUE_LAYOUT_VEXPAND = 0x2000;
    public static final int VALUE_LAYOUT_2 = 0x4000;

    public static final String PROP_LABEL = "label"; // NOI18N
    public static final String PROP_LAYOUT = "layout"; // NOI18N
    public static final String PROP_PREFERRED_HEIGHT = "preferredHeight"; // NOI18N
    public static final String PROP_PREFERRED_WIDTH = "preferredWidth"; // NOI18N
    public static final String PROP_COMMANDS = "commands"; // NOI18N
    public static final String PROP_DEFAULT_COMMAND = "defaultCommand"; //NOI18N
    public static final String PROP_ITEM_COMMAND_LISTENER = "itemCommandListener"; //NOI18N
    public static final String PROP_APPEARANCE_MODE = "appearanceMode"; // NOI18N
    public static final String PROP_OLD_ITEM_COMMAND_LISTENER = "itemCommandlistener"; //NOI18N
    public static final PropertyValue UNLOCKED_VALUE = MidpTypes.createIntegerValue(-1);
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_LABEL, component.readProperty (ClassCD.PROP_INSTANCE_NAME));

        DesignComponent listener = MidpDocumentSupport.getCommandListener (component.getDocument (), ItemCommandListenerCD.TYPEID);
        component.writeProperty (PROP_ITEM_COMMAND_LISTENER, PropertyValue.createComponentReference (listener));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_LABEL, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_LAYOUT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (VALUE_LAYOUT_DEFAULT), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_PREFERRED_HEIGHT, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (-1), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_PREFERRED_WIDTH, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (-1), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_COMMANDS, ItemCommandEventSourceCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(ItemCommandEventSourceCD.TYPEID), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_DEFAULT_COMMAND, ItemCommandEventSourceCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_ITEM_COMMAND_LISTENER, ItemCommandListenerCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)  
        );
    }

    private static Presenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Label"), // NOI18N
                        PropertyEditorString.createInstanceWithDatabinding(NbBundle.getMessage(ItemCD.class, "LBL_Item_Label")), PROP_LABEL) // NOI18N
                    .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Default_Command"), // NOI18N
                        PropertyEditorDefaultCommand.createInstance(), PROP_DEFAULT_COMMAND)
                    .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Layout"), PropertyEditorLayout.createInstance(), PROP_LAYOUT) // NOI18N
                    .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Width"), // NOI18N
                        PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(ItemCD.class, "LBL_Item_Preferred_Width"), // NOI18N
                            NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Width")), ItemCD.PROP_PREFERRED_WIDTH) // NOI18N
                    .addProperty(NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Height"), // NOI18N
                        PropertyEditorPreferredSize.createInstance(NbBundle.getMessage(ItemCD.class, "LBL_Item_Preferred_Height"), // NOI18N
                            NbBundle.getMessage(ItemCD.class, "DISP_Item_Preferred_Height")), ItemCD.PROP_PREFERRED_HEIGHT); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_LABEL, PROP_PREFERRED_WIDTH, PROP_PREFERRED_HEIGHT))
                .addParameters (ItemCode.createCommandParameter ())
                .addParameters (ItemCode.createItemCommandListenerParameter ())
                .addParameters (ItemCode.createItemLayoutParameter ())
                .addParameters (ItemCode.createDefaultCommandParameter ())
                .addSetters(MidpSetter.createSetter("addCommand", MidpVersionable.MIDP_2).setArrayParameter(ItemCode.PARAM_COMMAND).addParameters(ItemCode.PARAM_COMMAND)) // NOI18N
                .addSetters(MidpSetterItemCommandListener.createSetter("setItemCommandListener", MidpVersionable.MIDP_2).addParameters(ItemCode.PARAM_ITEM_COMMAND_LISTENER)) // NOI18N
                .addSetters(MidpSetter.createSetter("setDefaultCommand", MidpVersionable.MIDP_2).addParameters(ItemCode.PARAM_DEFAULT_COMMAND)) // NOI18N
                .addSetters(MidpSetter.createSetter("setLabel", MidpVersionable.MIDP).addParameters(PROP_LABEL)) // NOI18N
                .addSetters(MidpSetter.createSetter("setLayout", MidpVersionable.MIDP_2).addParameters(ItemCode.PARAM_LAYOUT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setPreferredSize", MidpVersionable.MIDP_2).addParameters(PROP_PREFERRED_WIDTH, PROP_PREFERRED_HEIGHT)); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        for (Presenter presenter : presenters.toArray(new Presenter[presenters.size()])) {
            if (presenter instanceof ActionsPresenter)
                presenters.remove(presenter);
        }      
        MidpActionsSupport.addCommonActionsPresentersParentEditAction(presenters, true, true, true, true, true);
        MidpActionsSupport.addNewActionPresenter(presenters, CommandCD.TYPEID);
        MidpActionsSupport.addUnusedCommandsAddActionForItem(presenters);
        MidpActionsSupport.addMoveActionPresenter(presenters, FormCD.PROP_ITEMS);
        
        presenters.addAll(MidpDatabindingCodeSupport.createDatabindingPresenters(PROP_LABEL, 
                                                                                 "getLabel()",
                                                                                 TYPEID,
                                                                                 MidpDatabindingCodeSupport.FeatureType.Item_FEATURE_LABEL));
        
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // inspector
                new InspectorFolderComponentPresenter(true),
                InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),
                MidpInspectorSupport.createComponentCommandsCategory(createOrderingArrayController(), CommandCD.TYPEID),
                // accept
                new AcceptItemCommandPresenter (),
                new ScreenMoveArrayAcceptPresenter(FormCD.PROP_ITEMS, ItemCD.TYPEID),
                // action
                EditDependencyPresenter.createEditablePresenter(),
                // code
                createSetterPresenter(),
                new RootCode.CodeComponentDependencyPresenter() {
                    protected void collectRequiredComponents (Collection<DesignComponent> requiredComponents) {
                        PropertyValue propertyValue = getComponent ().readProperty (PROP_COMMANDS);
                        ArrayList<DesignComponent> itemCommandEventSources = new ArrayList<DesignComponent> ();
                        Debug.collectAllComponentReferences (propertyValue, itemCommandEventSources);
                        for (DesignComponent component : itemCommandEventSources)
                            RootCode.collectRequiredComponents (component, requiredComponents);
                    }
                },
                // delete
                DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
                DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_ITEM_COMMAND_LISTENER),
                DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_DEFAULT_COMMAND),
                new DeletePresenter() {
                    protected void delete () {
                        DesignComponent component = getComponent ();
                        DesignComponent parent = component.getParentComponent ();
                        //TOOD this is should be check of Gauge attached to the ALert it is too generic
                        //This typeID check is becaouse of Gauge which could be also attached to the AlertCD
                        if (parent.getType() == FormCD.TYPEID )
                            ArraySupport.remove (parent, FormCD.PROP_ITEMS, component);
                        
                    }
                },
                new DeletePresenter() {
                    protected void delete() {
                        getComponent().getDocument().deleteComponents(MidpDatabindingSupport.getAllRelatedConnectors(getComponent()));
                    }
                },
                // screen
                new ItemDisplayPresenter (),
                new DisplayableResourceCategoriesPresenter()
        
        );
    }
    
    private List<InspectorOrderingController> createOrderingArrayController() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_COMMANDS, 0, ItemCommandEventSourceCD.TYPEID));
    }
    
    private static class AcceptItemCommandPresenter extends AcceptPresenter {
        
        public AcceptItemCommandPresenter() {
            super(Kind.COMPONENT_PRODUCER);
        }

        @Override
        public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
            DescriptorRegistry registry = getComponent ().getDocument ().getDescriptorRegistry ();
            return registry.isInHierarchy (CommandCD.TYPEID, producer.getMainComponentTypeID ());
        }

        @Override
        public final ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
            DesignComponent item = getComponent ();
            DesignDocument document = item.getDocument ();

            DesignComponent command = producer.createComponent (document).getMainComponent ();
            MidpDocumentSupport.getCategoryComponent (document, CommandsCategoryCD.TYPEID).addComponent (command);

            DesignComponent source = document.createComponent (ItemCommandEventSourceCD.TYPEID);
            MidpDocumentSupport.addEventSource (item, ItemCD.PROP_COMMANDS, source);

            source.writeProperty (ItemCommandEventSourceCD.PROP_ITEM, PropertyValue.createComponentReference (item));
            source.writeProperty (ItemCommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference (command));
            
            return new ComponentProducer.Result (item);
        }

    }
    
    

}
