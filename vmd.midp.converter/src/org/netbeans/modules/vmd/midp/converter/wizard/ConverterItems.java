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

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.*;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ConverterItems {

    // Created: NO, Adds: NO
    private static void convertItem (HashMap<String, ConverterItem> id2item, ConverterItem itemItem, DesignComponent itemComponent) {
        Converter.convertClass (itemItem, itemComponent);
        DesignDocument document = itemComponent.getDocument ();

        ConverterUtil.convertStringWithUserCode (itemComponent, ItemCD.PROP_LABEL, itemItem.getPropertyValue ("label")); // NOI18N
        ConverterUtil.convertInteger (itemComponent, ItemCD.PROP_PREFERRED_WIDTH, itemItem.getPropertyValue ("preferredSizeX")); // NOI18N
        ConverterUtil.convertInteger (itemComponent, ItemCD.PROP_PREFERRED_HEIGHT, itemItem.getPropertyValue ("preferredSizeY")); // NOI18N
        ConverterUtil.convertInteger (itemComponent, ItemCD.PROP_LAYOUT, itemItem.getPropertyValue ("layout")); // NOI18N

        ArrayList<String> commandsList = itemItem.getContainerPropertyValue ("commands"); // NOI18N
        if (commandsList != null)
            for (String commandValue : commandsList) {
                ConverterItem commandActionItem = id2item.get (commandValue);
                if (commandActionItem == null)
                    continue;
                ConverterActions.convertCommandAction (id2item, commandActionItem, document);
                DesignComponent eventSource = commandActionItem.getRelatedComponent ();
                if (eventSource != null) {
                    MidpDocumentSupport.addEventSource(itemComponent, ItemCD.PROP_COMMANDS, eventSource);
                    eventSource.writeProperty(ItemCommandEventSourceCD.PROP_ITEM, PropertyValue.createComponentReference(itemComponent));
                } else
                    Debug.warning ("Unrecognized command", itemItem, commandValue); // NOI18N
            }

        if (itemItem.isPropertyValueSet ("defaultCommand")) { // NOI18N
            DesignComponent defaultCommand = Converter.convertConverterItemComponent (id2item, itemItem.getPropertyValue ("defaultCommand"), document); // NOI18N
            if (defaultCommand != null) {
                List<DesignComponent> commandEventSources = DocumentSupport.gatherSubComponentsOfType (itemComponent, ItemCommandEventSourceCD.TYPEID);
                for (DesignComponent commandEventSource : commandEventSources) {
                    DesignComponent foundCommand = commandEventSource.readProperty (ItemCommandEventSourceCD.PROP_COMMAND).getComponent ();
                    if (defaultCommand == foundCommand) {
                        itemComponent.writeProperty (ItemCD.PROP_DEFAULT_COMMAND, PropertyValue.createComponentReference (defaultCommand));
                        break;
                    }
                }
                Debug.warning ("defaultCommand not found for", itemItem, itemItem.getPropertyValue ("defaultCommand")); // NOI18N
            }
        }
    }

    // Created: YES, Adds: NO
    static void convertGauge (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent gauge = document.createComponent (GaugeCD.TYPEID);
        convertItem (id2item, item, gauge);

        ConverterUtil.convertBoolean (gauge, GaugeCD.PROP_INTERACTIVE, item.getPropertyValue ("interactive")); // NOI18N
        ConverterUtil.convertInteger (gauge, GaugeCD.PROP_VALUE, item.getPropertyValue ("value")); // NOI18N
        ConverterUtil.convertInteger (gauge, GaugeCD.PROP_MAX_VALUE, item.getPropertyValue ("maxValue")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertAlertIndicator (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertGauge (id2item, item, document);

        DesignComponent gauge = item.getRelatedComponent ();
        gauge.writeProperty (GaugeCD.PROP_USED_BY_ALERT, MidpTypes.createBooleanValue (true));
    }

    // Created: YES, Adds: NO
    static void convertSpacer (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent spacer = document.createComponent (SpacerCD.TYPEID);
        convertItem (id2item, item, spacer);

        ConverterUtil.convertInteger (spacer, SpacerCD.PROP_MIN_WIDTH, item.getPropertyValue ("minWidth")); // NOI18N
        ConverterUtil.convertInteger (spacer, SpacerCD.PROP_MIN_HEIGHT, item.getPropertyValue ("minHeight")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertStringItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent stringComponent = document.createComponent (StringItemCD.TYPEID);
        convertItem (id2item, item, stringComponent);

        ConverterUtil.convertStringWithUserCode (stringComponent, StringItemCD.PROP_TEXT, item.getPropertyValue ("text")); // NOI18N

        ConverterUtil.convertConverterItemComponent (stringComponent, StringItemCD.PROP_FONT, id2item, item.getPropertyValue ("font"));// NOI18N

        String apperanceMode = item.getPropertyValue ("appearanceMode");// NOI18N
        if ("PLAIN".equals (apperanceMode))
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN));
        else if ("BUTTON".equals (apperanceMode))
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_BUTTON));
        else if ("HYPERLINK".equals (apperanceMode))
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_HYPERLINK));
    }

    // Created: YES, Adds: NO
    static void convertImageItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent imageComponent = document.createComponent (ImageItemCD.TYPEID);
        convertItem (id2item, item, imageComponent);

        ConverterUtil.convertStringWithUserCode (imageComponent, ImageItemCD.PROP_ALT_TEXT, item.getPropertyValue ("altText")); // NOI18N

        ConverterUtil.convertConverterItemComponent (imageComponent, ImageItemCD.PROP_IMAGE, id2item, item.getPropertyValue ("image"));// NOI18N

        String apperanceMode = item.getPropertyValue ("appearanceMode");// NOI18N
        if ("PLAIN".equals (apperanceMode))
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN));
        else if ("BUTTON".equals (apperanceMode))
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_BUTTON));
        else if ("HYPERLINK".equals (apperanceMode))
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_HYPERLINK));
    }

    // Created: YES, Adds: NO
    static void convertTextField (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent textField = document.createComponent (TextFieldCD.TYPEID);
        convertItem (id2item, item, textField);

        ConverterUtil.convertInteger (textField, TextFieldCD.PROP_CONSTRAINTS, item.getPropertyValue ("constraints")); // NOI18N
        ConverterUtil.convertString (textField, TextFieldCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("initialInputMode")); // NOI18N
        ConverterUtil.convertInteger (textField, TextFieldCD.PROP_CONSTRAINTS, item.getPropertyValue ("maxSize")); // NOI18N
        ConverterUtil.convertStringWithUserCode (textField, TextFieldCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("string")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertDateField (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent dateField = document.createComponent (DateFieldCD.TYPEID);
        convertItem (id2item, item, dateField);

        String inputMode = item.getPropertyValue ("inputMode");// NOI18N
        if ("DATE".equals (inputMode))
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_DATE));
        else if ("DATE_TIME".equals (inputMode))
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_DATE_TIME));
        else if ("TIME".equals (inputMode))
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_TIME));
    }

    // Created: YES, Adds: NO
    public static void convertChoiceGroup (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent choiceGroup = document.createComponent (ChoiceGroupCD.TYPEID);
        convertItem (id2item, item, choiceGroup);

        String choiceTypeValue = item.getPropertyValue ("choiceType"); // NOI18N
        if ("EXCLUSIVE".equals (choiceTypeValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_CHOICE_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_EXCLUSIVE));
        else if ("IMPLICIT".equals (choiceTypeValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_CHOICE_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_IMPLICIT));
        else if ("MULTIPLE".equals (choiceTypeValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_CHOICE_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_MULTIPLE));
        else if ("POPUP".equals (choiceTypeValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_CHOICE_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_POPUP));

        String fitPolicyValue = item.getPropertyValue ("fitPolicy"); // NOI18N
        if ("TEXT_WRAP_DEFAULT".equals (fitPolicyValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_DEFAULT));
        else if ("TEXT_WRAP_OFF".equals (fitPolicyValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_OFF));
        else if ("TEXT_WRAP_ON".equals (fitPolicyValue)) // NOI18N
            choiceGroup.writeProperty (ChoiceGroupCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_ON));

        ArrayList<String> elementsList = item.getContainerPropertyValue ("elements"); // NOI18N
        if (elementsList != null)
            for (String elementValue : elementsList) {
                DesignComponent choiceElement = Converter.convertConverterItemComponent (id2item, elementValue, document);
                if (choiceElement == null) {
                    Debug.warning ("ChoiceElement not found", elementValue);
                    continue;
                }
                choiceGroup.addComponent (choiceElement);
                ArraySupport.append (choiceGroup, ChoiceGroupCD.PROP_ELEMENTS, choiceElement);
            }
    }

    // Created: NO, Adds: NO
    static void convertCustomItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent custom) {
        convertItem (id2item, item, custom);
    }

}
