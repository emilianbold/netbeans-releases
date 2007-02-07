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
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGWaitScreenFailureCommandCD;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGWaitScreenSuccessCommandCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGWaitScreenFailureCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGWaitScreenSuccessCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGWaitScreenCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;

/**
 *
 * @author Anton Chechel
 */
public class SVGWaitScreenProducer extends MidpComponentProducer {
   
    public SVGWaitScreenProducer() {
       super(SVGWaitScreenCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG, "SVGWaitScreen", "SVGWaitScreen", SVGWaitScreenCD.ICON_PATH, SVGWaitScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    public Result createComponent(DesignDocument document) {
        DesignComponent waitScreen = document.createComponent(SVGWaitScreenCD.TYPEID);
        DesignComponent successCommand = MidpDocumentSupport.getSingletonCommand (document, SVGWaitScreenSuccessCommandCD.TYPEID);
        DesignComponent failureCommand = MidpDocumentSupport.getSingletonCommand (document, SVGWaitScreenFailureCommandCD.TYPEID);
        
        DesignComponent successEventSource = document.createComponent(SVGWaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent failureEventSource = document.createComponent(SVGWaitScreenFailureCommandEventSourceCD.TYPEID);
        
        successEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        successEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(successCommand));
        
        failureEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        failureEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(failureCommand));
       
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, successEventSource);
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, failureEventSource);
        
        return new Result(waitScreen, successCommand, failureCommand, successEventSource, failureEventSource);
    }
    
}
