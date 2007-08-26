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
 *
 */

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenFailureCommandCD;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenSuccessCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenFailureCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenSuccessCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class WaitScreenProducer extends MidpComponentProducer {
   
    public WaitScreenProducer() {
       super(WaitScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(WaitScreenProducer.class, "DISP_Wait_Screen"), NbBundle.getMessage(WaitScreenProducer.class, "TTIP_Wait_Screen"), WaitScreenCD.ICON_PATH, WaitScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent waitScreen) {
        return produceWaitScreen (document, waitScreen, true);
    }

    public static Result produceWaitScreen (DesignDocument document, DesignComponent waitScreen, boolean createTask) {
        DesignComponent successCommand = MidpDocumentSupport.getSingletonCommand (document, WaitScreenSuccessCommandCD.TYPEID);
        DesignComponent failureCommand = MidpDocumentSupport.getSingletonCommand (document, WaitScreenFailureCommandCD.TYPEID);

        DesignComponent successEventSource = document.createComponent(WaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent failureEventSource = document.createComponent(WaitScreenFailureCommandEventSourceCD.TYPEID);

        successEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        successEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(successCommand));

        failureEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        failureEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(failureCommand));

        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, successEventSource);
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, failureEventSource);

        if (createTask) {
            DesignComponent task = document.createComponent (SimpleCancellableTaskCD.TYPEID);
            MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (task);
            waitScreen.writeProperty (WaitScreenCD.PROP_TASK, PropertyValue.createComponentReference (task));

            return new Result (waitScreen, successCommand, failureCommand, successEventSource, failureEventSource, task);
        } else
            return new Result (waitScreen, successCommand, failureCommand, successEventSource, failureEventSource);
    }

    @Override
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
