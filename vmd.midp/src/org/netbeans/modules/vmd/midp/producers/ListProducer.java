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
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.commands.ListSelectCommandCD;
import org.netbeans.modules.vmd.midp.components.sources.ListSelectCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.handlers.ListEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class ListProducer extends MidpComponentProducer {

    public ListProducer () {
        super (ListCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(ListProducer.class, "DISP_List"), NbBundle.getMessage(ListProducer.class, "TTIP_List"), ListCD.ICON_PATH, ListCD.ICON_LARGE_PATH)); // NOI18N
    }

    public Result postInitialize (DesignDocument document, DesignComponent list) {
        DesignComponent listSelectCommand = MidpDocumentSupport.getSingletonCommand (document, ListSelectCommandCD.TYPEID);

        DesignComponent listSelectCommandEventSource = document.createComponent (ListSelectCommandEventSourceCD.TYPEID);
        listSelectCommandEventSource.writeProperty (CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference (list));
        listSelectCommandEventSource.writeProperty (CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference (listSelectCommand));
        MidpDocumentSupport.addEventSource (list, DisplayableCD.PROP_COMMANDS, listSelectCommandEventSource);

        DesignComponent listEventHandler = document.createComponent (ListEventHandlerCD.TYPEID);
        MidpDocumentSupport.updateEventHandlerWithNew (listSelectCommandEventSource, listEventHandler);
        list.writeProperty(ListCD.PROP_SELECT_COMMAND, PropertyValue.createComponentReference (listSelectCommandEventSource));
        
        return new Result (list, listSelectCommandEventSource, listEventHandler);
    }

}
