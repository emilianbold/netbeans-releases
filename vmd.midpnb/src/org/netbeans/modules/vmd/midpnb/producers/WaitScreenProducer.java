/*
 * WaitScreenProducer.java
 *
 * Created on February 2, 2007, 9:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.handlers.ListEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenFailureCommandCD;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenSuccessCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenFailureCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenSuccessCommandEventSourceCD;

/**
 *
 * @author Karol Harezlak
 */
public class WaitScreenProducer extends MidpComponentProducer {
   
    public WaitScreenProducer() {
       super(WaitScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "WaitScreen", "WaitScreen", WaitScreenCD.ICON_PATH, WaitScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    public Result createComponent(DesignDocument document) {
        DesignComponent waitScreen = document.createComponent(WaitScreenCD.TYPEID);
        DesignComponent successCommand = MidpDocumentSupport.getListSelectCommand(document, WaitScreenSuccessCommandCD.TYPEID);
        DesignComponent failureCommand = MidpDocumentSupport.getListSelectCommand(document, WaitScreenFailureCommandCD.TYPEID);
        
        DesignComponent waitScreenCommandEventSourceSuccess = document.createComponent(WaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent waitScreenCommandEventSourceFailure = document.createComponent(WaitScreenFailureCommandEventSourceCD.TYPEID);
        
        waitScreenCommandEventSourceSuccess.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        waitScreenCommandEventSourceSuccess.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(successCommand));
        
        waitScreenCommandEventSourceFailure.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        waitScreenCommandEventSourceFailure.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(failureCommand));
       
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, waitScreenCommandEventSourceSuccess);
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, waitScreenCommandEventSourceFailure);

        DesignComponent listEventHandlerSuccess = document.createComponent(ListEventHandlerCD.TYPEID);
        DesignComponent listEventHandlerFailure = document.createComponent(ListEventHandlerCD.TYPEID);
        
        MidpDocumentSupport.updateEventHandlerWithNew(waitScreenCommandEventSourceSuccess, listEventHandlerSuccess);
        MidpDocumentSupport.updateEventHandlerWithNew(waitScreenCommandEventSourceFailure, listEventHandlerFailure);

        waitScreen.writeProperty(WaitScreenCD.PROP_SUCCESS_ACTION, PropertyValue.createComponentReference(waitScreenCommandEventSourceSuccess));
        waitScreen.writeProperty(WaitScreenCD.PROP_FAILURE_ACTION, PropertyValue.createComponentReference(waitScreenCommandEventSourceFailure));
        
        return new Result(waitScreen, successCommand, failureCommand, waitScreenCommandEventSourceSuccess, waitScreenCommandEventSourceFailure, listEventHandlerSuccess, listEventHandlerFailure);
    }
    
}
