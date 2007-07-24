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

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SMSComposerSendCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SMSComposerSendCommandEventSourceCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerProducer extends MidpComponentProducer {
    
    public SMSComposerProducer() {
        super(SMSComposerCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(SMSComposerProducer.class, "DISP_SMS_Composer"), NbBundle.getMessage(SMSComposerProducer.class, "TTIP_SMS_Composer"), SMSComposerCD.ICON_PATH, SMSComposerCD.ICON_LARGE_PATH)); // NOI18N
    }
    
    public Result postInitialize (DesignDocument document, DesignComponent smsComposer) {
        DesignComponent sendCommand = MidpDocumentSupport.getSingletonCommand(document, SMSComposerSendCommandCD.TYPEID);
        DesignComponent smsEventSource = document.createComponent(SMSComposerSendCommandEventSourceCD.TYPEID);
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(smsComposer));
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(sendCommand));
        MidpDocumentSupport.addEventSource(smsComposer, DisplayableCD.PROP_COMMANDS, smsEventSource);
        smsComposer.writeProperty(SMSComposerCD.PROP_BGK_COLOR, MidpTypes.createIntegerValue(0x00));
        smsComposer.writeProperty(SMSComposerCD.PROP_FRG_COLOR, MidpTypes.createIntegerValue(0xCCCCCC));
        return new Result(smsComposer, sendCommand, smsEventSource);
    }
    
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
