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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
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

            ConverterUtil.convertConverterItemComponent (eventSource, CommandEventSourceCD.PROP_COMMAND, id2item, commandActionItem.getPropertyValue ("actionSource")); // NOI18N

            convertCommandActionHandler (id2item, commandActionItem, eventSource);
        } else if ("ItemCommandAction".equals (commandActionItem.getTypeID ())) { // NOI18N
            DesignComponent eventSource = document.createComponent (ItemCommandEventSourceCD.TYPEID);
            Converter.convertObject (commandActionItem, eventSource);

            ConverterUtil.convertConverterItemComponent (eventSource, ItemCommandEventSourceCD.PROP_COMMAND, id2item, commandActionItem.getPropertyValue ("actionSource")); // NOI18N

            convertCommandActionHandler (id2item, commandActionItem, eventSource);
        }
        // HINT - SelectCommandAction is recognized by ConverterDisplayables.convertList
        // HINT - SelectCaseCommandAction is recognized by ConverterElements.convertListElement
        // HINT - InternalCommandAction is recognized by ConverterBuilt.convertSplashScreen, ConverterBuilt.convertWaitScreen, ConvertSVG.convertSplashScreen, ConvertSVG.convertWaitScreen
        // HINT - SvgSelectCommandAction is recognized by ConverterSVG.convertMenu
        // HINT - SvgSelectCaseCommandAction is recognized by ConverterSVG.convertMenuElement
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
