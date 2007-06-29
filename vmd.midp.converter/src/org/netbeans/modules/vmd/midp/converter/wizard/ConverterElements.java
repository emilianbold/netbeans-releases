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

import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;

import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class ConverterElements {

    public static void convertListElement (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent listElement = document.createComponent (ListElementEventSourceCD.TYPEID);
        Converter.convertObject (item, listElement);

        ConverterUtil.convertStringWithUserCode (listElement, ListElementEventSourceCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N

        DesignComponent image = Converter.convertConverterItemComponent (id2item, item.getPropertyValue ("image"), document); // NOI18N
        if (image != null)
            listElement.writeProperty (ListElementEventSourceCD.PROP_IMAGE, PropertyValue.createComponentReference (image));

        ConverterUtil.convertBoolean (listElement, ListElementEventSourceCD.PROP_SELECTED, item.getPropertyValue ("selected")); // NOI18N

        ConverterItem commandActionItem = id2item.get (item.getPropertyValue ("commandAction")); // NOI18N
        if (commandActionItem != null) {
            Converter.convertObject (commandActionItem, listElement);
            ConverterActions.convertCommandActionHandler (id2item, commandActionItem, listElement);
        }
    }

}
