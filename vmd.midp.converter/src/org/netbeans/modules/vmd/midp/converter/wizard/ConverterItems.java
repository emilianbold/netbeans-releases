/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.vmd.midp.converter.wizard;

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
    static void convertItem (HashMap<String, ConverterItem> id2item, ConverterItem itemItem, DesignComponent itemComponent) {
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
                        itemComponent.writeProperty (ItemCD.PROP_DEFAULT_COMMAND, PropertyValue.createComponentReference (commandEventSource));
                        break;
                    }
                }
                Debug.warning ("defaultCommand not found for", itemItem, itemItem.getPropertyValue ("defaultCommand")); // NOI18N
            }
        }
    }

    // Created: YES, Adds: NO
    static void convertGauge (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertGaugeCore (id2item, item, document.createComponent (GaugeCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertGaugeCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent gauge) {
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
        convertSpacerCore (id2item, item, document.createComponent (SpacerCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertSpacerCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent spacer) {
        convertItem (id2item, item, spacer);

        ConverterUtil.convertInteger (spacer, SpacerCD.PROP_MIN_WIDTH, item.getPropertyValue ("minWidth")); // NOI18N
        ConverterUtil.convertInteger (spacer, SpacerCD.PROP_MIN_HEIGHT, item.getPropertyValue ("minHeight")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertStringItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertStringItemCore (id2item, item, document.createComponent (StringItemCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertStringItemCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent stringComponent) {
        convertItem (id2item, item, stringComponent);

        ConverterUtil.convertStringWithUserCode (stringComponent, StringItemCD.PROP_TEXT, item.getPropertyValue ("text")); // NOI18N

        ConverterUtil.convertConverterItemComponent (stringComponent, StringItemCD.PROP_FONT, id2item, item.getPropertyValue ("font"));// NOI18N

        String apperanceMode = item.getPropertyValue ("appearanceMode");// NOI18N
        if ("PLAIN".equals (apperanceMode)) // NOI18N
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN));
        else if ("BUTTON".equals (apperanceMode)) // NOI18N
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_BUTTON));
        else if ("HYPERLINK".equals (apperanceMode)) // NOI18N
            stringComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_HYPERLINK));
    }

    // Created: YES, Adds: NO
    static void convertImageItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertImageItemCore (id2item, item, document.createComponent (ImageItemCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertImageItemCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent imageComponent) {
        convertItem (id2item, item, imageComponent);

        ConverterUtil.convertStringWithUserCode (imageComponent, ImageItemCD.PROP_ALT_TEXT, item.getPropertyValue ("altText")); // NOI18N

        ConverterUtil.convertConverterItemComponent (imageComponent, ImageItemCD.PROP_IMAGE, id2item, item.getPropertyValue ("image"));// NOI18N

        String apperanceMode = item.getPropertyValue ("appearanceMode");// NOI18N
        if ("PLAIN".equals (apperanceMode)) // NOI18N
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN));
        else if ("BUTTON".equals (apperanceMode)) // NOI18N
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_BUTTON));
        else if ("HYPERLINK".equals (apperanceMode)) // NOI18N
            imageComponent.writeProperty (ItemCD.PROP_APPEARANCE_MODE, MidpTypes.createIntegerValue (ItemCD.VALUE_HYPERLINK));
    }

    // Created: YES, Adds: NO
    static void convertTextField (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertTextFieldCore (id2item, item, document.createComponent (TextFieldCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertTextFieldCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent textField) {
        convertItem (id2item, item, textField);

        ConverterUtil.convertInteger (textField, TextFieldCD.PROP_CONSTRAINTS, item.getPropertyValue ("constraints")); // NOI18N
        ConverterUtil.convertString (textField, TextFieldCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("initialInputMode")); // NOI18N
        ConverterUtil.convertInteger (textField, TextFieldCD.PROP_CONSTRAINTS, item.getPropertyValue ("maxSize")); // NOI18N
        ConverterUtil.convertStringWithUserCode (textField, TextFieldCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("string")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertDateField (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertDateFieldCore (id2item, item, document.createComponent (DateFieldCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertDateFieldCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent dateField) {
        convertItem (id2item, item, dateField);

        String inputMode = item.getPropertyValue ("inputMode");// NOI18N
        if ("DATE".equals (inputMode)) // NOI18N
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_DATE));
        else if ("DATE_TIME".equals (inputMode)) // NOI18N
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_DATE_TIME));
        else if ("TIME".equals (inputMode)) // NOI18N
            dateField.writeProperty (DateFieldCD.PROP_INPUT_MODE, MidpTypes.createIntegerValue (DateFieldCD.VALUE_TIME));
    }

    // Created: YES, Adds: NO
    static void convertChoiceGroup (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertChoiceGroupCore (id2item, item, document.createComponent (ChoiceGroupCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertChoiceGroupCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent choiceGroup) {
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
                DesignComponent choiceElement = Converter.convertConverterItemComponent (id2item, elementValue, choiceGroup.getDocument ());
                if (choiceElement == null) {
                    Debug.warning ("ChoiceElement not found", elementValue); // NOI18N
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
