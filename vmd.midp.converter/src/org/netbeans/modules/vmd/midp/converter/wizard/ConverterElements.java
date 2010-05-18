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
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;

import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class ConverterElements {

    // Created: YES, Adds: NO
    static void convertListElement (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent listElement = document.createComponent (ListElementEventSourceCD.TYPEID);
        Converter.convertObject (item, listElement);

        ConverterUtil.convertStringWithUserCode (listElement, ListElementEventSourceCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
        ConverterUtil.convertConverterItemComponent (listElement, ListElementEventSourceCD.PROP_IMAGE, id2item, item.getPropertyValue ("image")); // NOI18N
        ConverterUtil.convertBoolean (listElement, ListElementEventSourceCD.PROP_SELECTED, item.getPropertyValue ("selected")); // NOI18N

        ConverterItem commandActionItem = id2item.get (item.getPropertyValue ("commandAction")); // NOI18N
        if (commandActionItem != null) {
            Converter.convertObject (commandActionItem, listElement);
            ConverterActions.convertCommandActionHandler (id2item, commandActionItem, listElement);
        }
    }

    // Created: YES, Adds: NO
    static void convertChoiceElement (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        DesignComponent choiceElement = document.createComponent (ChoiceElementCD.TYPEID);
        Converter.convertObject (item, choiceElement);

        ConverterUtil.convertStringWithUserCode (choiceElement, ListElementEventSourceCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
        ConverterUtil.convertConverterItemComponent (choiceElement, ListElementEventSourceCD.PROP_IMAGE, id2item, item.getPropertyValue ("image")); // NOI18N
        ConverterUtil.convertBoolean (choiceElement, ListElementEventSourceCD.PROP_SELECTED, item.getPropertyValue ("selected")); // NOI18N
    }

}
