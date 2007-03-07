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
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.properties.common.TextFieldBC;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.RootCode;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.listeners.CommandListenerCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowDisplayableCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.DisplayableResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;

import java.util.*;

/**
 * @author David Kaspar
 */

public final class DisplayableCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Displayable"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/displayable_16.png"; // NOI18N

    public static final String PROP_COMMANDS = "commands" ; // NOI18N
    public static final String PROP_TITLE = "title"; // NOI18N
    public static final String PROP_TICKER = "ticker";  // NOI18N
    public static final String PROP_COMMAND_LISTENER = "commandListener"; // NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (ClassCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize (DesignComponent component) {
        String instanceName = MidpTypes.getString (component.readProperty (ClassCD.PROP_INSTANCE_NAME));
        component.writeProperty (PROP_TITLE, MidpTypes.createStringValue (instanceName));

        DesignDocument document = component.getDocument ();
        DesignComponent listener = MidpDocumentSupport.getCommandListener (document);
        component.writeProperty (PROP_COMMAND_LISTENER, PropertyValue.createComponentReference (listener));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor(PROP_COMMANDS, CommandEventSourceCD.TYPEID.getArrayType (), PropertyValue.createEmptyArray (CommandEventSourceCD.TYPEID), true, false, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_TITLE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_TICKER, TickerCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_COMMAND_LISTENER, CommandListenerCD.TYPEID, PropertyValue.createNull (), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory (PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty ("Title", PropertyEditorString.createInstance(), new TextFieldBC(), PROP_TITLE)
                    .addProperty ("Ticker", PropertyEditorResourcesComboBox.createTickerPropertyEditor(), PROP_TICKER);
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (MidpParameter.create (PROP_TITLE, PROP_TICKER))
                .addParameters (DisplayableCode.createCommandParameter ())
                .addParameters (DisplayableCode.createCommandListenerParameter ())
                .addSetters (MidpSetter.createSetter ("setTitle", MidpVersionable.MIDP).addParameters (PROP_TITLE))
                .addSetters (MidpSetter.createSetter ("setTicker", MidpVersionable.MIDP).addParameters (PROP_TICKER))
                .addSetters (MidpSetter.createSetter ("addCommand", MidpVersionable.MIDP).setArrayParameter (DisplayableCode.PARAM_COMMAND).addParameters (DisplayableCode.PARAM_COMMAND))
                .addSetters (MidpSetter.createSetter ("setCommandListener", MidpVersionable.MIDP).addParameters (PROP_COMMAND_LISTENER));
    }

    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        // actions
        MidpActionsSupport.addNewActionPresenter(presenters, CommandCD.TYPEID);
        MidpActionsSupport.addUnusedCommandsAddActionForDisplayable(presenters);
        
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            SwitchDisplayableEventHandlerCD.createSwitchDisplayableEventHandlerCreatorPresenter (),
            // properties
            createPropertiesPresenter (),
            // inspector
            InspectorFolderPresenter.create(true),
            MidpInspectorSupport.createComponentCommandsCategory(createOrderingArrayController(), CommandCD.TYPEID),
            // accept
            new DisplayableAccept.DisplayableCommandsAcceptPresenter (),
//            new DisplayableAccept.DisplayableCommandsEventHandlerAcceptPresenter (),
            // flow
            new FlowInfoNodePresenter (),
            new FlowDisplayableCommandPinOrderPresenter (),
            // code
            createSetterPresenter (),
            new RootCode.CodeComponentDependencyPresenter () {
                protected void collectRequiredComponents (Collection<DesignComponent> requiredComponents) {
                    PropertyValue propertyValue = getComponent ().readProperty (PROP_COMMANDS);
                    ArrayList<DesignComponent> commandEventSources = new ArrayList<DesignComponent> ();
                    Debug.collectAllComponentReferences (propertyValue, commandEventSources);
                    for (DesignComponent component : commandEventSources)
                        RootCode.collectRequiredComponents (component, requiredComponents);
                }
            },
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_TICKER),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_COMMAND_LISTENER),
            // screen
            new DisplayableDisplayPresenter(),
            new DisplayableResourceCategoriesPresenter()
        );
    }
    
    private List<InspectorOrderingController> createOrderingArrayController() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_COMMANDS, 0, CommandEventSourceCD.TYPEID));
    }
}
