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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.*;
import org.netbeans.modules.vmd.midpnb.components.svg.*;
import org.netbeans.modules.vmd.midpnb.producers.SVGWaitScreenProducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ConverterSVG {

    // Created: YES, Adds: YES
    static void convertImage (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent image = document.createComponent (SVGImageCD.TYPEID);
        Converter.convertClass (item, image);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (image);

        ConverterUtil.convertString (image, SVGImageCD.PROP_RESOURCE_PATH, item.getPropertyValue ("imageResourcePath")); // NOI18N

        String handler = item.getPropertyValue ("externalResourceHandler"); // NOI18N
        if (handler != null)
            image.writeProperty (SVGImageCD.PROP_EXTERNAL_RESOURCE_HANDLER, MidpTypes.createJavaCodeValue (handler));
    }

    // Created: NO, Adds: NO
    static void convertAnimatorWrapper (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent wrapper) {
        ConverterDisplayables.convertCanvas (id2item, item, wrapper);

        ConverterUtil.convertConverterItemComponent (wrapper, SVGPlayerCD.PROP_SVG_IMAGE, id2item, item.getPropertyValue ("svgImage")); // NOI18N
        ConverterUtil.convertBoolean (wrapper, SVGPlayerCD.PROP_START_ANIM_IMMEDIATELY, item.getPropertyValue ("startAnimationImmediately")); // NOI18N
        ConverterUtil.convertBoolean (wrapper, SVGPlayerCD.PROP_RESET_ANIMATION_WHEN_STOPPED, item.getPropertyValue ("resetAnimationWhenStopped")); // NOI18N
        ConverterUtil.convertFloat (wrapper, SVGPlayerCD.PROP_TIME_INCREMENT, item.getPropertyValue ("animationIncrement")); // NOI18N
    }

    // Created: YES, Adds: NO
    static void convertPlayer (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent player = document.createComponent (SVGPlayerCD.TYPEID);
        convertAnimatorWrapper (id2item, item, player);
    }

    // Created: YES, Adds: NO
    static void convertMenu (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        ComponentProducer producer = DocumentSupport.getComponentProducer (document, SVGMenuCD.TYPEID.toString ());
        DesignComponent menu = producer.createComponent (document).getMainComponent ();
        convertAnimatorWrapper (id2item, item, menu);

        ConverterUtil.convertBoolean (menu, SVGMenuCD.PROP_INDEX_BASED_SWITCH, item.getPropertyValue ("indexBasedSwitch")); // NOI18N
        
        ConverterItem selectCommandActionItem = id2item.get (item.getPropertyValue ("selectCommandAction")); // NOI18N
        List<DesignComponent> selectCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (menu, SVGMenuSelectCommandEventSourceCD.TYPEID);
        DesignComponent selectCommandEventSource = selectCommandEventSourceComponents.get (0);
        Converter.convertObject (selectCommandActionItem, selectCommandEventSource);
        // ConverterActions.convertCommandActionHandler (id2item, selectCommandActionItem, selectCommandEventSource); // HINT - handler is not used in mvd file

        ArrayList<String> elementsList = item.getContainerPropertyValue ("menuElements"); // NOI18N
        if (elementsList != null)
            for (String elementValue : elementsList) {
                DesignComponent menuElement = Converter.convertConverterItemComponent (id2item, elementValue, document);
                if (menuElement == null) {
                    Debug.warning ("MenuElement not found", elementValue); // NOI18N
                    continue;
                }
                menu.addComponent (menuElement);
                ArraySupport.append (menu, SVGMenuCD.PROP_ELEMENTS, menuElement);
            }
    }

    // Created: YES, Adds: NO
    static void convertMenuElement (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent menuElement = document.createComponent (SVGMenuElementEventSourceCD.TYPEID);
        Converter.convertObject (item, menuElement);

        ConverterUtil.convertStringWithUserCode (menuElement, SVGMenuElementEventSourceCD.PROP_STRING, item.getPropertyValue ("elementName")); // NOI18N

        ConverterItem commandActionItem = id2item.get (item.getPropertyValue ("commandAction")); // NOI18N
        if (commandActionItem != null) {
            Converter.convertObject (commandActionItem, menuElement);
            ConverterActions.convertCommandActionHandler (id2item, commandActionItem, menuElement);
        }
    }

    // Created: YES, Adds: NO
    static void convertSplashScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        ComponentProducer producer = DocumentSupport.getComponentProducer (document, SVGSplashScreenCD.TYPEID.toString ());
        DesignComponent screen = producer.createComponent (document).getMainComponent ();
        convertAnimatorWrapper (id2item, item, screen);

        ConverterUtil.convertBoolean (screen, SVGSplashScreenCD.PROP_ALLOW_TIMEOUT_INTERRUPT, item.getPropertyValue ("allowTimeoutInterrupt")); // NOI18N
        ConverterUtil.convertInteger (screen, SVGSplashScreenCD.PROP_TIMEOUT, item.getPropertyValue ("timeout")); // NOI18N

        ConverterItem dismissActionItem = id2item.get (item.getPropertyValue ("dismissCommandAction")); // NOI18N
        List<DesignComponent> dismissCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, SVGSplashScreenDismissCommandEventSourceCD.TYPEID);
        DesignComponent dismissEventSource = dismissCommandEventSourceComponents.get (0);
        Converter.convertObject (dismissActionItem, dismissEventSource);
        ConverterActions.convertCommandActionHandler (id2item, dismissActionItem, dismissEventSource);
    }

    // Created: YES, Adds: NO
    static void convertWaitScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent screen = SVGWaitScreenProducer.produceSVGWaitScreen (document, document.createComponent (SVGWaitScreenCD.TYPEID), false).getMainComponent ();
        convertAnimatorWrapper (id2item, item, screen);

        ConverterUtil.convertConverterItemComponent (screen, WaitScreenCD.PROP_TASK, id2item, item.getPropertyValue ("task")); // NOI18N

        ConverterItem successActionItem = id2item.get (item.getPropertyValue ("successCommandAction")); // NOI18N
        List<DesignComponent> successCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, SVGWaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent successEventSource = successCommandEventSourceComponents.get (0);
        Converter.convertObject (successActionItem, successEventSource);
        ConverterActions.convertCommandActionHandler (id2item, successActionItem, successEventSource);

        ConverterItem failureActionItem = id2item.get (item.getPropertyValue ("failureCommandAction")); // NOI18N
        List<DesignComponent> failureCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, SVGWaitScreenFailureCommandEventSourceCD.TYPEID);
        DesignComponent failureEventSource = failureCommandEventSourceComponents.get (0);
        Converter.convertObject (failureActionItem, failureEventSource);
        ConverterActions.convertCommandActionHandler (id2item, failureActionItem, failureEventSource);
    }

}
