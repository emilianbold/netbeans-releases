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
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListSelectCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;

import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class ConverterActions {

    // Created: YES, Adds: NO
    static void convertCommandAction (HashMap<String, ConverterItem> id2item, ConverterItem commandActionItem, DesignDocument document) {
        if ("CommandAction".equals (commandActionItem.getTypeID ())) { // NOI18N
            DesignComponent eventSource = document.createComponent (CommandEventSourceCD.TYPEID);
            Converter.convertObject (commandActionItem, eventSource);

            ConverterUtil.convertConverterItemComponent (eventSource, CommandEventSourceCD.PROP_COMMAND, id2item, commandActionItem.getPropertyValue ("actionSource"), document); // NOI18N

            convertCommandActionHandler (id2item, commandActionItem, eventSource);
        } else if ("ItemCommandAction".equals (commandActionItem.getTypeID ())) { // NOI18N
            DesignComponent eventSource = document.createComponent (ItemCommandEventSourceCD.TYPEID);
            Converter.convertObject (commandActionItem, eventSource);

            ConverterUtil.convertConverterItemComponent (eventSource, ItemCommandEventSourceCD.PROP_COMMAND, id2item, commandActionItem.getPropertyValue ("actionSource"), document); // NOI18N

            convertCommandActionHandler (id2item, commandActionItem, eventSource);
        } else if ("SelectCommandAction".equals (commandActionItem.getTypeID ())) { // NOI18N
            DesignComponent eventSource = document.createComponent (ListSelectCommandEventSourceCD.TYPEID);
            Converter.convertObject (commandActionItem, null);
            
            convertCommandActionHandler (id2item, commandActionItem, eventSource);
        }
        // TODO - other command actions
        // HINT - SelectCaseCommandAction is recognized by ConverterElements.convertListElement
    }

    // Created: YES, Adds: YES
    static void convertCommandActionHandler (HashMap<String, ConverterItem> id2item, ConverterItem commandActionItem, DesignComponent eventSource) {
        ConverterItem targetItem = Converter.convertConverterItem (id2item, commandActionItem.getPropertyValue ("targetDisplayable"), eventSource.getDocument ()); // NOI18N
        if (targetItem == null)
            return;
        DesignComponent targetComponent = targetItem.getRelatedComponent ();

        ConverterItem targetForwardItem = null;
        if (targetComponent != null  &&  targetComponent.getDocument ().getDescriptorRegistry ().isInHierarchy (AlertCD.TYPEID, targetComponent.getType ()))
                targetForwardItem = Converter.convertConverterItem (id2item, commandActionItem.getPropertyValue ("targetForwardDisplayable"), eventSource.getDocument ()); // NOI18N
        DesignComponent targetForwardComponent = targetForwardItem != null ? targetForwardItem.getRelatedComponent () : null;

        if (targetForwardComponent != null) {
            DesignComponent eventHandler = MidpDocumentSupport.updateEventHandlerFromTarget (eventSource, targetForwardComponent);
            MidpDocumentSupport.updateEventHandlerWithAlert (eventHandler, targetComponent);
        } else {
            MidpDocumentSupport.updateEventHandlerFromTarget (eventSource, targetComponent);
        }
    }

}
