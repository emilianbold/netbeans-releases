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
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;

/**
 * @author David Kaspar
 */
public class ConverterResources {

    // Created: YES, Adds: YES
    static void convertFont (ConverterItem item, DesignDocument document) {
        DesignComponent font = document.createComponent (FontCD.TYPEID);
        Converter.convertClass (item, font);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (font);

        String specifier = item.getPropertyValue ("specifier"); // NOI18N
        if ("FONT_INPUT_TEXT".equals (specifier)) { // NOI18N
            font.writeProperty (FontCD.PROP_FONT_KIND, MidpTypes.createIntegerValue (FontCD.VALUE_KIND_INPUT));
        } else if ("FONT_STATIC_TEXT".equals (specifier)) { // NOI18N
            font.writeProperty (FontCD.PROP_FONT_KIND, MidpTypes.createIntegerValue (FontCD.VALUE_KIND_STATIC));
        } else {
            ConverterUtil.convertInteger (font, FontCD.PROP_STYLE, item.getPropertyValue ("style")); // NOI18N

            String face = item.getPropertyValue ("face"); // NOI18N
            if ("FACE_MONOSPACE".equals (face)) // NOI18N
                font.writeProperty (FontCD.PROP_FACE, MidpTypes.createIntegerValue (FontCD.VALUE_FACE_MONOSPACE));
            else if ("FACE_PROPORTIONAL".equals (face)) // NOI18N
                font.writeProperty (FontCD.PROP_FACE, MidpTypes.createIntegerValue (FontCD.VALUE_FACE_PROPORTIONAL));
            else if ("FACE_SYSTEM".equals (face)) // NOI18N
                font.writeProperty (FontCD.PROP_FACE, MidpTypes.createIntegerValue (FontCD.VALUE_FACE_SYSTEM));

            String size = item.getPropertyValue ("size"); // NOI18N
            if ("SIZE_SMALL".equals (size)) // NOI18N
                font.writeProperty (FontCD.PROP_SIZE, MidpTypes.createIntegerValue (FontCD.VALUE_SIZE_SMALL));
            else if ("SIZE_MEDIUM".equals (size)) // NOI18N
                font.writeProperty (FontCD.PROP_SIZE, MidpTypes.createIntegerValue (FontCD.VALUE_SIZE_MEDIUM));
            else if ("SIZE_LARGE".equals (size)) // NOI18N
                font.writeProperty (FontCD.PROP_SIZE, MidpTypes.createIntegerValue (FontCD.VALUE_SIZE_LARGE));

            boolean nonDefault = item.isPropertyValueSet ("style")  ||  item.isPropertyValueSet ("face")  ||  item.isPropertyValueSet ("size"); // NOI18N
            font.writeProperty (FontCD.PROP_FONT_KIND, MidpTypes.createIntegerValue (nonDefault ? FontCD.VALUE_KIND_STATIC : FontCD.VALUE_KIND_DEFAULT));
        }
    }

    // Created: YES, Adds: YES
    static void convertImage (ConverterItem item, DesignDocument document) {
        DesignComponent image = document.createComponent (ImageCD.TYPEID);
        Converter.convertClass (item, image);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (image);

        ConverterUtil.convertString (image, ImageCD.PROP_RESOURCE_PATH, item.getPropertyValue ("imageResourcePath")); // NOI18N
    }

    // Created: YES, Adds: YES
    static void convertTicker (ConverterItem item, DesignDocument document) {
        DesignComponent ticker = document.createComponent (TickerCD.TYPEID);
        Converter.convertClass (item, ticker);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (ticker);

        ConverterUtil.convertStringWithUserCode (ticker, TickerCD.PROP_STRING, item.getPropertyValue ("string")); // NOI18N
    }

    // Created: YES, Adds: YES
    static void convertCommand (ConverterItem item, DesignDocument document) {
        DesignComponent command = document.createComponent (CommandCD.TYPEID);
        Converter.convertClass (item, command);
        MidpDocumentSupport.getCategoryComponent (document, CommandsCategoryCD.TYPEID).addComponent (command);

        ConverterUtil.convertStringWithUserCode (command, CommandCD.PROP_LABEL, item.getPropertyValue ("label")); // NOI18N
        ConverterUtil.convertStringWithUserCode (command, CommandCD.PROP_LONG_LABEL, item.getPropertyValue ("longLabel")); // NOI18N
        ConverterUtil.convertInteger (command, CommandCD.PROP_PRIORITY, item.getPropertyValue ("priority")); // NOI18N

        String typeValue = item.getPropertyValue ("type"); // NOI18N
        int type;
        if ("SCREEN".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_SCREEN;
        else if ("BACK".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_BACK;
        else if ("CANCEL".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_CANCEL;
        else if ("OK".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_OK;
        else if ("HELP".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_HELP;
        else if ("STOP".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_STOP;
        else if ("EXIT".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_EXIT;
        else if ("ITEM".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_ITEM;
        else
            type = CommandCD.VALUE_OK;
        command.writeProperty (CommandCD.PROP_TYPE, MidpTypes.createIntegerValue (type));
    }

}
