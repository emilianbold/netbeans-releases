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
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.categories.DisplayablesCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.displayables.FormCD;
import org.netbeans.modules.vmd.midp.components.displayables.TextBoxCD;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class ConverterDisplayables {

    // Created: NO, Adds: YES
    private static void convertDisplayable (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent displayable) {
        Converter.convertClass (item, displayable);
        DesignDocument document = displayable.getDocument ();
        MidpDocumentSupport.getCategoryComponent (document, DisplayablesCategoryCD.TYPEID).addComponent (displayable);

        ConverterUtil.convertStringWithUserCode (displayable, DisplayableCD.PROP_TITLE, item.getPropertyValue ("title")); // NOI18N

        ConverterItem tickerItem = Converter.convertConverterItem (id2item, item.getPropertyValue ("ticker"), document); // NOI18N
        if (tickerItem != null) {
            DesignComponent ticker = tickerItem.getRelatedComponent ();
            if (ticker != null)
                displayable.writeProperty (DisplayableCD.PROP_TICKER, PropertyValue.createComponentReference (ticker));
        }

        ArrayList<String> commandsList = item.getContainerPropertyValue ("commands"); // NOI18N
        if (commandsList != null)
            for (String commandValue : commandsList) {
                ConverterItem commandActionItem = id2item.get (commandValue);
                if (commandActionItem == null)
                    continue;
                ConverterActions.convertCommandAction (id2item, commandActionItem, document);
                DesignComponent eventSource = commandActionItem.getRelatedComponent ();
                MidpDocumentSupport.addEventSource(displayable, DisplayableCD.PROP_COMMANDS, eventSource);
                eventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
            }
    }

    // Created: NO, Adds: NO
    private static void convertScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent screen) {
        convertDisplayable (id2item, item, screen);
    }

    // Created: YES, Adds: NO
    static void convertForm (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent form = document.createComponent (FormCD.TYPEID);
        convertScreen (id2item, item, form);

        ArrayList<String> itemsList = item.getContainerPropertyValue ("items"); // NOI18N
        if (itemsList != null)
            for (String itemValue : itemsList) {
                ConverterItem itemItem = Converter.convertConverterItem (id2item, itemValue, document);
                if (itemItem != null) {
                    DesignComponent itemComponent = itemItem.getRelatedComponent ();
                    if (itemComponent != null)
                        ArraySupport.append (form, FormCD.PROP_ITEMS, itemComponent);
                }
            }
    }

    // Created: YES, Adds: NO
    public static void convertTextBox (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent textbox = document.createComponent (TextBoxCD.TYPEID);
        convertScreen (id2item, item, textbox);

        ConverterUtil.convertStringWithUserCode (textbox, TextBoxCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
        ConverterUtil.convertInteger (textbox, TextBoxCD.PROP_MAX_SIZE, item.getPropertyValue ("maxSize")); // NOI18N
        ConverterUtil.convertString (textbox, TextBoxCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("initialInputMode")); // NOI18N

        // TODO - constraints
    }

}
