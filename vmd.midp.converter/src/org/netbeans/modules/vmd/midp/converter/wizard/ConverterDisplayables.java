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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.DisplayablesCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.*;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListSelectCommandEventSourceCD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ConverterDisplayables {

    // Created: NO, Adds: YES
    static void convertDisplayable (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent displayable) {
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
                if (eventSource != null) {
                    MidpDocumentSupport.addEventSource(displayable, DisplayableCD.PROP_COMMANDS, eventSource);
                    eventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(displayable));
                } else
                    Debug.warning ("Unrecognized command", item, commandValue); // NOI18N
            }
    }

    // Created: NO, Adds: NO
    static void convertScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent screen) {
        convertDisplayable (id2item, item, screen);
    }

    // Created: YES, Adds: NO
    static void convertForm (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertFormCore (id2item, item, document.createComponent (FormCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertFormCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent form) {
        convertScreen (id2item, item, form);

        ArrayList<String> itemsList = item.getContainerPropertyValue ("items"); // NOI18N
        if (itemsList != null)
            for (String itemValue : itemsList) {
                DesignComponent itemComponent = Converter.convertConverterItemComponent (id2item, itemValue, form.getDocument ());
                if (itemComponent != null) {
                    form.addComponent (itemComponent);
                    ArraySupport.append (form, FormCD.PROP_ITEMS, itemComponent);
                }
            }
    }

    // Created: YES, Adds: NO
    static void convertTextBox (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertTextBoxCore (id2item, item, document.createComponent (TextBoxCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertTextBoxCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent textbox) {
        convertScreen (id2item, item, textbox);

        ConverterUtil.convertStringWithUserCode (textbox, TextBoxCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
        ConverterUtil.convertInteger (textbox, TextBoxCD.PROP_MAX_SIZE, item.getPropertyValue ("maxSize")); // NOI18N
        ConverterUtil.convertString (textbox, TextBoxCD.PROP_INITIAL_INPUT_MODE, item.getPropertyValue ("initialInputMode")); // NOI18N
        ConverterUtil.convertInteger (textbox, TextBoxCD.PROP_CONSTRAINTS, item.getPropertyValue ("constraints")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertList (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        ComponentProducer producer = DocumentSupport.getComponentProducer (document, ListCD.TYPEID.toString ());
        DesignComponent list = producer.createComponent (document).getMainComponent ();
        convertListCore (id2item, item, list);
    }

    // Created: NO, Adds: NO
    static void convertListCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent list) {
        convertScreen (id2item, item, list);

        ConverterItem selectCommandActionItem = id2item.get (item.getPropertyValue ("selectCommandAction")); // NOI18N
        List<DesignComponent> selectCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (list, ListSelectCommandEventSourceCD.TYPEID);
        DesignComponent selectCommandEventSource = selectCommandEventSourceComponents.get (0);
        Converter.convertObject (selectCommandActionItem, selectCommandEventSource);
        ConverterActions.convertCommandActionHandler (id2item, selectCommandActionItem, selectCommandEventSource);
        
        if (item.isPropertyValueSet ("selectCommand")) { // NOI18N
            DesignComponent selectCommand = Converter.convertConverterItemComponent (id2item, item.getPropertyValue ("selectCommand"), list.getDocument ()); // NOI18N
            if (selectCommand != null) {
                List<DesignComponent> commandEventSources = DocumentSupport.gatherSubComponentsOfType (list, CommandEventSourceCD.TYPEID);
                boolean found = false;
                for (DesignComponent commandEventSource : commandEventSources) {
                    DesignComponent foundCommand = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
                    if (selectCommand == foundCommand) {
                        found = true;
                        list.writeProperty (ListCD.PROP_SELECT_COMMAND, PropertyValue.createComponentReference (commandEventSource));
                        break;
                    }
                }
                if (! found)
                    Debug.warning ("selectCommand not found for", item); // NOI18N
            } else
                list.writeProperty (ListCD.PROP_SELECT_COMMAND, PropertyValue.createNull ());
        }

        String choiceTypeValue = item.getPropertyValue ("choiceType"); // NOI18N
        if ("EXCLUSIVE".equals (choiceTypeValue)) // NOI18N
            list.writeProperty (ListCD.PROP_LIST_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_EXCLUSIVE));
        else if ("IMPLICIT".equals (choiceTypeValue)) // NOI18N
            list.writeProperty (ListCD.PROP_LIST_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_IMPLICIT));
        else if ("MULTIPLE".equals (choiceTypeValue)) // NOI18N
            list.writeProperty (ListCD.PROP_LIST_TYPE, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_MULTIPLE));

        String fitPolicyValue = item.getPropertyValue ("fitPolicy"); // NOI18N
        if ("TEXT_WRAP_DEFAULT".equals (fitPolicyValue)) // NOI18N
            list.writeProperty (ListCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_DEFAULT));
        else if ("TEXT_WRAP_OFF".equals (fitPolicyValue)) // NOI18N
            list.writeProperty (ListCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_OFF));
        else if ("TEXT_WRAP_ON".equals (fitPolicyValue)) // NOI18N
            list.writeProperty (ListCD.PROP_FIT_POLICY, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_ON));

        ArrayList<String> elementsList = item.getContainerPropertyValue ("elements"); // NOI18N
        if (elementsList != null)
            for (String elementValue : elementsList) {
                DesignComponent listElement = Converter.convertConverterItemComponent (id2item, elementValue, list.getDocument ());
                if (listElement == null) {
                    Debug.warning ("ListElement not found", elementValue); // NOI18N
                    continue;
                }
                list.addComponent (listElement);
                ArraySupport.append (list, ListCD.PROP_ELEMENTS, listElement);
            }

        ConverterUtil.convertBoolean (list, ListCD.PROP_INDEX_BASED_SWITCH, item.getPropertyValue ("indexBasedSwitch")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertAlert (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        convertAlertCore (id2item, item, document.createComponent (AlertCD.TYPEID));
    }

    // Created: NO, Adds: NO
    static void convertAlertCore (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent alert) {
        convertScreen (id2item, item, alert);

        ConverterUtil.convertStringWithUserCode (alert, AlertCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
        ConverterUtil.convertInteger (alert, AlertCD.PROP_TIMEOUT, item.getPropertyValue ("timeout")); // NOI18N

        DesignComponent image = Converter.convertConverterItemComponent (id2item, item.getPropertyValue ("image"), alert.getDocument ()); // NOI18N
        if (image != null)
            alert.writeProperty (ListElementEventSourceCD.PROP_IMAGE, PropertyValue.createComponentReference (image));

        String type = item.getPropertyValue ("type"); // NOI18N
        if ("ALERT".equals (type)) // NOI18N
            alert.writeProperty (AlertCD.PROP_ALERT_TYPE, MidpTypes.createAlertTypeValue (MidpTypes.AlertType.ALARM));
        else if ("CONFIRMATION".equals (type)) // NOI18N
            alert.writeProperty (AlertCD.PROP_ALERT_TYPE, MidpTypes.createAlertTypeValue (MidpTypes.AlertType.CONFIRMATION));
        else if ("ERROR".equals (type)) // NOI18N
            alert.writeProperty (AlertCD.PROP_ALERT_TYPE, MidpTypes.createAlertTypeValue (MidpTypes.AlertType.ERROR));
        else if ("INFO".equals (type)) // NOI18N
            alert.writeProperty (AlertCD.PROP_ALERT_TYPE, MidpTypes.createAlertTypeValue (MidpTypes.AlertType.INFO));
        else if ("WARNING".equals (type)) // NOI18N
            alert.writeProperty (AlertCD.PROP_ALERT_TYPE, MidpTypes.createAlertTypeValue (MidpTypes.AlertType.WARNING));

        DesignComponent indicator = Converter.convertConverterItemComponent (id2item, item.getPropertyValue ("indicator"), alert.getDocument ()); // NOI18N
        if (indicator != null) {
            alert.addComponent (indicator);
            alert.writeProperty (AlertCD.PROP_INDICATOR, PropertyValue.createComponentReference (indicator));
        }
    }

    // Created: NO, Adds: NO
    static void convertCanvas (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent canvas) {
        convertDisplayable (id2item, item, canvas);

        ConverterUtil.convertBoolean (canvas, CanvasCD.PROP_IS_FULL_SCREEN, item.getPropertyValue ("fullScreen")); // NOI18N
    }

}
