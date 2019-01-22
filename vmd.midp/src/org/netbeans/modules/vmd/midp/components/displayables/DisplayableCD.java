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
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.screen.actions.EditDependencyPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.RootCode;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.listeners.CommandListenerCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowDisplayableCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.DisplayableResourceCategoriesPresenter;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.injector.TickerInjectorPresenter;
import org.openide.util.NbBundle;

import java.util.*;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResourceLazyInit;


/**
 * 
 * 
 */
public final class DisplayableCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Displayable"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/displayable_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/displayable_32.png"; // NOI18N

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

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_TITLE, component.readProperty (ClassCD.PROP_INSTANCE_NAME));

        DesignDocument document = component.getDocument ();
        DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
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
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty (NbBundle.getMessage(DisplayableCD.class, "DISP_Displayable_Title"), // NOI18N
                        PropertyEditorString.createTextFieldInstance(NbBundle.getMessage(DisplayableCD.class, "LBL_Displayable_Title")), PROP_TITLE) // NOI18N
                    .addProperty (NbBundle.getMessage(DisplayableCD.class, "DISP_Displayable_Ticker"), PropertyEditorResourceLazyInit.createTickerPropertyEditor(), PROP_TICKER); // NOI18N
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (MidpParameter.create (PROP_TITLE, PROP_TICKER))
                .addParameters (DisplayableCode.createCommandParameter ())
                .addParameters (DisplayableCode.createCommandListenerParameter ())
                .addSetters (MidpSetter.createSetter ("setTitle", MidpVersionable.MIDP).addParameters (PROP_TITLE)) // NOI18N
                .addSetters (MidpSetter.createSetter ("setTicker", MidpVersionable.MIDP).addParameters (PROP_TICKER)) // NOI18N
                .addSetters (MidpSetter.createSetter ("addCommand", MidpVersionable.MIDP).setArrayParameter (DisplayableCode.PARAM_COMMAND).addParameters (DisplayableCode.PARAM_COMMAND)) // NOI18N
                .addSetters (MidpSetter.createSetter ("setCommandListener", MidpVersionable.MIDP).addParameters (PROP_COMMAND_LISTENER)); // NOI18N
    }

    @Override
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
            new InspectorFolderComponentPresenter(true),
            MidpInspectorSupport.createComponentCommandsCategory(createOrderingArrayController(), CommandCD.TYPEID),
            InspectorPositionPresenter.create(new DisplayablePC()),
            // action
            EditDependencyPresenter.createEditablePresenter(),    
            // accept
            new DisplayableAccept.DisplayableCommandsAcceptPresenter (),
            new MidpAcceptProducerKindPresenter().addType(TickerCD.TYPEID, PROP_TICKER),
            new MidpAcceptTrensferableKindPresenter().addType(TickerCD.TYPEID, PROP_TICKER),
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
            new DisplayableResourceCategoriesPresenter(),
            new TickerInjectorPresenter ()
        );
    }
    
    private List<InspectorOrderingController> createOrderingArrayController() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_COMMANDS, 0, CommandEventSourceCD.TYPEID));
    }
}
