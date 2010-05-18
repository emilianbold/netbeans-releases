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
package org.netbeans.modules.vmd.midp.components.sources;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenterForwarder;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.SecondaryGoToSourcePresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.listeners.ItemCommandListenerCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowItemCommandPinOrderPresenter;
import org.netbeans.modules.vmd.midp.screen.ItemCommandSRItemPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class ItemCommandEventSourceCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "#ItemCommandEventSource"); // NOI18N
    
    // HINT this is redundant property, could be resolved from parent
    public static final String PROP_ITEM = "item"; // NOI18N
    public static final String PROP_COMMAND = "command"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(EventSourceCD.TYPEID, TYPEID, true, false);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(ItemCommandEventSourceCD.PROP_ITEM, ItemCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP),
            new PropertyDescriptor(ItemCommandEventSourceCD.PROP_COMMAND, CommandCD.TYPEID, PropertyValue.createNull(), false, false, MidpVersionable.MIDP)
        );
    }
    
    public static DesignComponent getItemComponent(DesignComponent itemCommandEventSourceComponent) {
        return itemCommandEventSourceComponent.readProperty(ItemCommandEventSourceCD.PROP_ITEM).getComponent();
    }
    
    public static DesignComponent getFormComponent(DesignComponent itemCommandEventSourceComponent) {
        DesignComponent itemComponent = getItemComponent(itemCommandEventSourceComponent);
        return itemComponent != null ? itemComponent.getParentComponent() : null;
    }
    
    public static DesignComponent getCommandComponent(DesignComponent commandEventSourceComponent) {
        return commandEventSourceComponent.readProperty(ItemCommandEventSourceCD.PROP_COMMAND).getComponent();
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        EventSourceSupport.addActionsPresentres(presenters);
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // info
            InfoPresenter.create(EventSourceSupport.createItemCommandEventSourceInfoResolver()),
            // general
            GoToSourcePresenter.createForwarder (PROP_COMMAND),
            new SecondaryGoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    DesignComponent listener = MidpDocumentSupport.getCommandListener (getComponent ().getDocument (), ItemCommandListenerCD.TYPEID);
                    return MultiGuardedSection.matches (section, listener.getComponentID () + "-itemCommandAction", getComponent ().getComponentID () + "-postAction"); // NOI18N
                }
            },
            //properties
            PropertiesPresenterForwarder.createByReference(PROP_COMMAND),
            //screen
            new ItemCommandSRItemPresenter(),
            // flow
            new FlowEventSourcePinPresenter() {
            protected DesignComponent getComponentForAttachingPin() {
                return getFormComponent(getComponent());
            }
            
            protected String getDisplayName() {
                DesignComponent command = getCommandComponent(getComponent());
                InfoPresenter presenter = command.getPresenter(InfoPresenter.class);
                return presenter.getDisplayName(InfoPresenter.NameType.PRIMARY);
                
            }
            
            protected String getOrder() {
                return FlowItemCommandPinOrderPresenter.CATEGORY_ID;
            }
            
            @Override
            protected boolean canRename() {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                return command != null;
            }
            
            @Override
            protected String getRenameName() {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                return (String) command.readProperty(CommandCD.PROP_LABEL).getPrimitiveValue ();
            }
            
            @Override
            protected void setRenameName(String name) {
                DesignComponent command = ItemCommandEventSourceCD.getCommandComponent(getComponent());
                command.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue(name));
            }
            
            @Override
            protected DesignEventFilter getEventFilter() {
                return super.getEventFilter().addDescentFilter(getComponent(), ItemCommandEventSourceCD.PROP_COMMAND);
            }
        },
            DeleteDependencyPresenter.createDependentOnPropertyPresenter(PROP_ITEM),
            DeleteDependencyPresenter.createDependentOnPropertyPresenter(PROP_COMMAND),
            new DeletePresenter() {
            protected void delete() {
                DesignComponent component = getComponent();
                DesignComponent item = component.readProperty(PROP_ITEM).getComponent();
                ArraySupport.remove(item, ItemCD.PROP_COMMANDS, component);
            }
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        );
    }
    
    
}
