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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.ControllersCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.RootCD;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.general.AbstractEventHandlerCreatorPresenter;

import java.util.*;

/**
 * @author David Kaspar
 */
public final class MidpDocumentSupport {
    
    public static final String PROJECT_TYPE_MIDP = "vmd-midp"; // NOI18N

    public static final Comparator<DesignComponent> COMPONENT_DISPLAY_NAME_COMPARATOR = new Comparator<DesignComponent>() {
        public int compare(DesignComponent component1, DesignComponent component2) {
            String name1 = (String) component1.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
            String name2 = (String) component2.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
            if (name1 == null)
                return -1;
            else if (name2 == null)
                return 1;
            
            return name1.compareTo(name2);
        }
    };
    
    public static DesignComponent getCategoryComponent(DesignDocument document, TypeID categoryType) {
        List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(document.getRootComponent(), categoryType);
        if (list.size () == 0  &&  document.getTransactionManager ().isWriteAccess ()) {
            DesignComponent categoryComponent = document.createComponent (categoryType);
            document.getRootComponent ().addComponent (categoryComponent);
            return categoryComponent;
        }
        assert list.size() == 1;
        return list.get(0);
    }
    
    public static DesignComponent getSingletonCommand (final DesignDocument document, final TypeID typeID) {
        final DesignComponent categoryComponent = getCategoryComponent(document, CommandsCategoryCD.TYPEID);
        
        assert categoryComponent != null;
        List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(categoryComponent, typeID);
        if (list.size() == 1)
            return list.get(0);
        else if (list.size() == 0) {
            DesignComponent newComponent = document.createComponent (typeID);
            categoryComponent.addComponent(newComponent);
            return newComponent;
        }
        
        throw new IllegalStateException("Component "+ typeID + " should be singelton per document"); //NOI18N;
    }
    
    public static DesignComponent attachCommandToDisplayable(DesignComponent displayable, DesignComponent command) {
        DesignComponent source = displayable.getDocument().createComponent(CommandEventSourceCD.TYPEID);
        MidpDocumentSupport.addEventSource(displayable, DisplayableCD.PROP_COMMANDS, source);
        
        source.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
        source.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(command));
        
        return source;
    }
    
    public static DesignComponent attachCommandToItem(DesignComponent item, DesignComponent command) {
        DesignComponent itemCommandEventSource = command.getDocument().createComponent(ItemCommandEventSourceCD.TYPEID);
        itemCommandEventSource.writeProperty(ItemCommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(command));
        itemCommandEventSource.writeProperty(ItemCommandEventSourceCD.PROP_ITEM, PropertyValue.createComponentReference(item));
        MidpArraySupport.append(item, ItemCD.PROP_COMMANDS, itemCommandEventSource);
        item.addComponent(itemCommandEventSource);
        return itemCommandEventSource;
    }
    
    public static void addEventSource(DesignComponent component, String propertyName, DesignComponent eventSource) {
        component.addComponent(eventSource);
        PropertyValue sources = component.readProperty(propertyName);
        sources = PropertyValueSupport.addArrayValue(sources, PropertyValue.createComponentReference(eventSource));
        component.writeProperty(propertyName, sources);
    }
    
    
//    // TODO - PropertyValue.equals is not implemented -> it is not working!
//    public static void removeEventSource (DesignComponent component, String propertyName, DesignComponent eventSource) {
//        PropertyValue sources = component.readProperty (propertyName);
//        PropertyValueSupport.removeArrayValue (sources, PropertyValue.createComponentReference (eventSource));
//        component.writeProperty (propertyName, sources);
//        component.removeComponent (eventSource);
//    }
    
    public static boolean isCreatableEventHandlerTo(DesignComponent targetComponent) {
        return targetComponent == null || targetComponent.getPresenter (AbstractEventHandlerCreatorPresenter.class) != null;
    }
    
    public static DesignComponent updateEventHandlerFromTarget(DesignComponent eventSource, DesignComponent targetComponent) {
        assert targetComponent == null  ||  ! targetComponent.getDocument().getDescriptorRegistry().isInHierarchy(EventHandlerCD.TYPEID, targetComponent.getType());
        DesignComponent oldEventHandler = eventSource.readProperty(EventSourceCD.PROP_EVENT_HANDLER).getComponent();
        AbstractEventHandlerCreatorPresenter targetPresenter = targetComponent != null ? targetComponent.getPresenter(AbstractEventHandlerCreatorPresenter.class) : null;
        DesignComponent newEventHandler = targetPresenter != null ? targetPresenter.createReuseEventHandler(eventSource, oldEventHandler, targetComponent) : null;
        return updateEventHandlerWithNew(eventSource, newEventHandler);
    }
    
    public static DesignComponent updateEventHandlerWithNew(DesignComponent eventSource, DesignComponent newEventHandler) {
        //        System.out.println(eventSource + " " + newEventHandler);
        assert newEventHandler == null  ||  newEventHandler.getDocument().getDescriptorRegistry().isInHierarchy(EventHandlerCD.TYPEID, newEventHandler.getType());
        DesignComponent oldEventHandler = eventSource.readProperty(EventSourceCD.PROP_EVENT_HANDLER).getComponent();
        if (oldEventHandler == newEventHandler)
            return oldEventHandler;
        if (oldEventHandler != null) {
            oldEventHandler.writeProperty(EventHandlerCD.PROP_EVENT_SOURCE, PropertyValue.createNull());
            oldEventHandler.removeFromParentComponent();
        }
        if (newEventHandler != null) {
            eventSource.addComponent(newEventHandler);
            eventSource.writeProperty(EventSourceCD.PROP_EVENT_HANDLER, PropertyValue.createComponentReference(newEventHandler));
            newEventHandler.writeProperty(EventHandlerCD.PROP_EVENT_SOURCE, PropertyValue.createComponentReference(eventSource));
        } else {
            eventSource.writeProperty(EventSourceCD.PROP_EVENT_HANDLER, PropertyValue.createNull());
        }
        return newEventHandler;
    }
    
    public static void updateEventHandlerWithAlert(DesignComponent eventHandler, DesignComponent alert) {
        if (alert  != null) {
            eventHandler.writeProperty(SwitchDisplayableEventHandlerCD.PROP_ALERT, PropertyValue.createComponentReference(alert));
        } else {
            eventHandler.writeProperty(SwitchDisplayableEventHandlerCD.PROP_ALERT, PropertyValue.createNull());
        }
    }
    
    public static void updateSwitchDisplayableEventHandler(DesignComponent eventHandler, DesignComponent alert, DesignComponent displayable) {
        eventHandler.writeProperty(SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
        updateEventHandlerWithAlert(eventHandler, alert);
    }
    
    public static String createDisplayNameFromTypeID(TypeID type) {
        String str = type.getString();
        int i = str.lastIndexOf('.');
        return i >= 0 ? str.substring(i + 1) : str;
    }
    
    public static Collection<DesignComponent> getAvailableCommandsForComponent(DesignComponent component) {
        Collection<DesignComponent> componentsUnderCommandCategory = getCategoryComponent(component.getDocument(), CommandsCategoryCD.TYPEID).getComponents();
        DescriptorRegistry registry = component.getDocument().getDescriptorRegistry();
        List<DesignComponent> unusedCommands = null;
        Set<DesignComponent> usedCommands = null;
        
        for (PropertyValue propertyValue : component.readProperty(DisplayableCD.PROP_COMMANDS).getArray()) {
            DesignComponent currentComponent = propertyValue.getComponent();
            if (currentComponent == null)
                continue;
            if (usedCommands == null)
                usedCommands = new HashSet<DesignComponent>();
            usedCommands.add(currentComponent.readProperty(CommandEventSourceCD.PROP_COMMAND).getComponent());
        }
        for (DesignComponent componentChild : componentsUnderCommandCategory) {
            if (usedCommands != null && usedCommands.contains(componentChild))
                continue;
            if (registry.isInHierarchy(CommandCD.TYPEID, componentChild.getType()) && (Boolean) componentChild.readProperty(CommandCD.PROP_ORDINARY).getPrimitiveValue()) {
                if (unusedCommands == null)
                    unusedCommands = new ArrayList<DesignComponent>();
                unusedCommands.add(componentChild);
            }
        }
        if (unusedCommands != null)
            Collections.sort(unusedCommands, COMPONENT_DISPLAY_NAME_COMPARATOR);
        
        return unusedCommands;
    }

    public static DesignComponent getCommandListener(DesignDocument document, TypeID listenerType) {
        DesignComponent controllersCategory = getCategoryComponent(document, ControllersCategoryCD.TYPEID);
        List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(controllersCategory, listenerType);
        if (list.isEmpty()) {
            DesignComponent commandListener = document.createComponent(listenerType);
            controllersCategory.addComponent(commandListener);
            return commandListener;
        }
        assert list.size() == 1;
        return list.get(0);
    }
    
    public static int getMidpVersion(DesignDocument document) {
        PropertyValue propertyValue = document.getRootComponent().readProperty(RootCD.PROP_VERSION);
        return propertyValue.getKind() == PropertyValue.Kind.VALUE  &&  RootCD.VALUE_MIDP_2_0.equals(MidpTypes.getString(propertyValue)) ? 2 : 1; // NOI18N
    }

}
