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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;

/**
 * @author David Kaspar
 */
public class DisplayableAccept {
    
    static class DisplayableCommandsAcceptPresenter extends AcceptPresenter {
        
        public DisplayableCommandsAcceptPresenter() {
            super(Kind.COMPONENT_PRODUCER);
        }
        
        @Override
        public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
            if (getComponent().getComponentDescriptor().getPropertyDescriptor(DisplayableCD.PROP_COMMANDS).isReadOnly())
                return false;
            DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
            return registry.isInHierarchy(CommandCD.TYPEID, producer.getMainComponentTypeID ());
        }
        
        @Override
        public final ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
            DesignComponent displayable = getComponent();
            DesignDocument document = displayable.getDocument();
            
            DesignComponent command = producer.createComponent(document).getMainComponent();
            MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).addComponent(command);
            
            DesignComponent source = document.createComponent(CommandEventSourceCD.TYPEID);
            MidpDocumentSupport.addEventSource(displayable, DisplayableCD.PROP_COMMANDS, source);
            
            source.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
            source.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(command));
            
            return new ComponentProducer.Result (source);
        }
        
    }
    
    //    static class DisplayableCommandsEventHandlerAcceptPresenter extends AcceptPresenter {
    //
    //        public boolean isAcceptable (ComponentProducer producer) {
    //            if (getComponent ().getComponentDescriptor ().getPropertyDescriptor (DisplayableCD.PROP_COMMANDS).isReadOnly ())
    //                return false;
    //            DescriptorRegistry registry = getComponent ().getDocument ().getDescriptorRegistry ();
    //            return registry.isInHierarchy (EventHandlerCD.TYPEID, producer.getMainComponentTypeID ());
    //        }
    //
    //        public final void accept (ComponentProducer producer) {
    //            DesignComponent displayable = getComponent ();
    //            DesignDocument document = displayable.getDocument ();
    //
    //            DesignComponent handler = producer.createComponent (document).getMainComponent ();
    //            if (handler == null)
    //                return;
    //
    //            DesignComponent command = createBackCommand (document);
    //            MidpDocumentSupport.getCategoryComponent (document, CommandsCategoryCD.TYPEID).addComponent (command);
    //
    //            DesignComponent source = document.createComponent (CommandEventSourceCD.TYPEID);
    //            MidpDocumentSupport.addEventSource (displayable, DisplayableCD.PROP_COMMANDS, source);
    //
    //            source.writeProperty (CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference (displayable));
    //            source.writeProperty (CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference (command));
    //
    //            MidpDocumentSupport.updateEventHandlerWithNew (source, handler);
    //        }
    //
    //        private DesignComponent createBackCommand (DesignDocument document) {
    //            List<ComponentProducer> producers = document.getDescriptorRegistry ().getComponentProducers ();
    //            for (ComponentProducer producer : producers) {
    //                if (CommandProducer.PRODUCER_ID_BACK_COMMAND.equals (producer.getProducerID ()))
    //                    return producer.createComponent (document).getMainComponent ();
    //            }
    //            return null;
    //        }
    //
    //    }
    
}
