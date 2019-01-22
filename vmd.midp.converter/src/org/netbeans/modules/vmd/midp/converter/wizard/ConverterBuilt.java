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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.resources.TableModelCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SplashScreenDismissCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenFailureCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenSuccessCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.producers.WaitScreenProducer;

import java.util.HashMap;
import java.util.List;

/**
 * 
 */
public class ConverterBuilt {

    // Created: NO, Adds: NE
    static void convertAbstractInfoScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent screen) {
        ConverterDisplayables.convertCanvas (id2item, item, screen);

        ConverterUtil.convertStringWithUserCode (screen, AbstractInfoScreenCD.PROP_TEXT, item.getPropertyValue ("text")); // NOI18N
        ConverterUtil.convertConverterItemComponent (screen, AbstractInfoScreenCD.PROP_TEXT_FONT, id2item, item.getPropertyValue ("textFont")); // NOI18N
        ConverterUtil.convertConverterItemComponent (screen, AbstractInfoScreenCD.PROP_IMAGE, id2item, item.getPropertyValue ("image")); // NOI18N
        // HINT - display property is not used in mvd file
    }

    // Created: YES, Adds: YES
    static void convertSimpleCancellableTask (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent task = document.createComponent (SimpleCancellableTaskCD.TYPEID);
        Converter.convertClass (item, task);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (task);

        String code = ConverterUtil.decryptStringFromJavaCode (item.getPropertyValue ("runnableMethodBody")); // NOI18N
        if (code != null)
            task.writeProperty (SimpleCancellableTaskCD.PROP_CODE, MidpTypes.createJavaCodeValue (code));
    }

    // Created: YES, Adds: YES
    static void convertSimpleTableModel (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent table = document.createComponent (SimpleTableModelCD.TYPEID);
        Converter.convertClass (item, table);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (table);

        String columns = item.getPropertyValue ("columnNames"); // NOI18N
        if (columns != null)
            table.writeProperty (SimpleTableModelCD.PROP_COLUMN_NAMES, ConverterUtil.decryptStringArrayArray (columns, TableModelCD.TYPEID_COLUMN_NAMES, 1));

        String values = item.getPropertyValue ("values"); // NOI18N
        if (values != null)
            table.writeProperty (SimpleTableModelCD.PROP_VALUES, ConverterUtil.decryptStringArrayArray (values, TableModelCD.TYPEID_VALUES, 2));
    }

    // Created: YES, Adds: NO
    static void convertSplashScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        ComponentProducer producer = DocumentSupport.getComponentProducer (document, SplashScreenCD.TYPEID.toString ());
        DesignComponent screen = producer.createComponent (document).getMainComponent ();
        convertAbstractInfoScreen (id2item, item, screen);

        ConverterUtil.convertInteger (screen, SplashScreenCD.PROP_TIMEOUT, item.getPropertyValue ("timeout")); // NOI18N
        Boolean allow = ConverterUtil.getBoolean (item.getPropertyValue ("allowTimeoutInterrupt")); // NOI18N
        screen.writeProperty (SplashScreenCD.PROP_ALLOW_TIMEOUT_INTERRUPT, MidpTypes.createBooleanValue (allow == null  ||  allow));

        ConverterItem dismissActionItem = id2item.get (item.getPropertyValue ("dismissCommandAction")); // NOI18N
        List<DesignComponent> dismissCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, SplashScreenDismissCommandEventSourceCD.TYPEID);
        DesignComponent dismissEventSource = dismissCommandEventSourceComponents.get (0);
        Converter.convertObject (dismissActionItem, dismissEventSource);
        ConverterActions.convertCommandActionHandler (id2item, dismissActionItem, dismissEventSource);
    }

    // Created: YES, Adds: NO
    static void convertWaitScreen (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent screen = WaitScreenProducer.produceWaitScreen (document, document.createComponent (WaitScreenCD.TYPEID), false).getMainComponent ();
        convertAbstractInfoScreen (id2item, item, screen);

        ConverterUtil.convertConverterItemComponent (screen, WaitScreenCD.PROP_TASK, id2item, item.getPropertyValue ("task")); // NOI18N

        ConverterItem successActionItem = id2item.get (item.getPropertyValue ("successCommandAction")); // NOI18N
        List<DesignComponent> successCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, WaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent successEventSource = successCommandEventSourceComponents.get (0);
        Converter.convertObject (successActionItem, successEventSource);
        ConverterActions.convertCommandActionHandler (id2item, successActionItem, successEventSource);

        ConverterItem failureActionItem = id2item.get (item.getPropertyValue ("failureCommandAction")); // NOI18N
        List<DesignComponent> failureCommandEventSourceComponents = DocumentSupport.gatherSubComponentsOfType (screen, WaitScreenFailureCommandEventSourceCD.TYPEID);
        DesignComponent failureEventSource = failureCommandEventSourceComponents.get (0);
        Converter.convertObject (failureActionItem, failureEventSource);
        ConverterActions.convertCommandActionHandler (id2item, failureActionItem, failureEventSource);
    }

    static void convertTableItem (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent table = document.createComponent (TableItemCD.TYPEID);
        ConverterItems.convertCustomItem (id2item, item, table);

        ConverterUtil.convertString (table, TableItemCD.PROP_TITLE, item.getPropertyValue ("title")); // NOI18N
        ConverterUtil.convertBoolean (table, TableItemCD.PROP_BORDERS, item.getPropertyValue ("borders")); // NOI18N
        ConverterUtil.convertConverterItemComponent (table, TableItemCD.PROP_MODEL, id2item, item.getPropertyValue ("model")); // NOI18N
        ConverterUtil.convertConverterItemComponent (table, TableItemCD.PROP_TITLE_FONT, id2item, item.getPropertyValue ("titleFont")); // NOI18N
        ConverterUtil.convertConverterItemComponent (table, TableItemCD.PROP_HEADERS_FONT, id2item, item.getPropertyValue ("headersFont")); // NOI18N
        ConverterUtil.convertConverterItemComponent (table, TableItemCD.PROP_VALUES_FONT, id2item, item.getPropertyValue ("valuesFont")); // NOI18N
    }

}
