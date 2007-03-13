/*
 * FileBrowserProducer.java
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
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SMSComposerSendCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SMSComposerSendCommandEventSourceCD;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerProducer extends MidpComponentProducer {
    
    public SMSComposerProducer() {
        super(SMSComposerCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "SMS Composer", "SMS Composer", SMSComposerCD.ICON_PATH, SMSComposerCD.ICON_LARGE_PATH)); // NOI18N
    }
    
    public Result createComponent(DesignDocument document) {
        DesignComponent smsComposer = document.createComponent(SMSComposerCD.TYPEID);
        DesignComponent sendCommand = MidpDocumentSupport.getSingletonCommand(document, SMSComposerSendCommandCD.TYPEID);
        DesignComponent smsEventSource = document.createComponent(SMSComposerSendCommandEventSourceCD.TYPEID);
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(smsComposer));
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(sendCommand));
        MidpDocumentSupport.addEventSource(smsComposer, DisplayableCD.PROP_COMMANDS, smsEventSource);
        
        return new Result(smsComposer, sendCommand, smsEventSource);
    }
    
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
