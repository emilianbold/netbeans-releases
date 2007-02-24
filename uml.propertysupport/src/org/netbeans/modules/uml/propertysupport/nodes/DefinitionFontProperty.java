/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.propertysupport.nodes;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

import java.awt.Font;
import java.lang.reflect.InvocationTargetException;


public final class DefinitionFontProperty extends DefinitionPropertySupport {


    public DefinitionFontProperty(IPropertyDefinition def, IPropertyElement element, boolean writable, boolean autoCommit) {
        super(def, element, Font.class, writable, autoCommit);
    }


    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        IPropertyElement element = getElement();
        Font f = buildCurrentFont(element);
        return f;
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (val instanceof Font) {
            Font f = (Font) val;
            IPropertyElement element = getElement();
            updateFontOnPropertyElement(element, f);
            PreferenceHelper.saveModifiedPreferences(element);
        }
    }
    
    private static void  updateFontOnPropertyElement(IPropertyElement pEle, Font pFont)
    {
        if (pEle != null && pFont != null)
        {
            String name = pFont.getName();
            IPropertyElement subEleName = pEle.getSubElement("FaceName", null); // NOI18N
            if (subEleName != null){
                subEleName.setValue(name);
                subEleName.setModified(true);
            }
            Integer i = new Integer(pFont.getSize());
            IPropertyElement subEleName2 = pEle.getSubElement("Height", null); // NOI18N
            if (subEleName2 != null){
                subEleName2.setValue(i.toString());
                subEleName2.setModified(true);
            }
            boolean bBold = pFont.isBold();
            IPropertyElement subEleName3 = pEle.getSubElement("Weight", null); // NOI18N
            if (bBold && subEleName3 != null){
                subEleName3.setValue("700"); // NOI18N
                subEleName3.setModified(true);
            }
            else if (subEleName3 != null){
                subEleName3.setValue("400"); // NOI18N
                subEleName3.setModified(true);
            }
            boolean bItalic = pFont.isItalic();
            IPropertyElement subEleName4 = pEle.getSubElement("Italic", null); // NOI18N
            if (bItalic && subEleName4 != null){
                subEleName4.setValue("1"); // NOI18N
                subEleName4.setModified(true);
            }
            else if (subEleName4 != null){
                subEleName4.setValue("0"); // NOI18N
                subEleName4.setModified(true);
            }
            pEle.setModified(true);
        }
    }
    
    public Font buildCurrentFont(IPropertyElement pEle)
    {
        Font pFont = null;
        if (pEle != null)
        {
            IPropertyElement subEleName = pEle.getSubElement("FaceName", null); // NOI18N
            String name = subEleName.getValue();
            String height = pEle.getSubElement("Height", null).getValue(); // NOI18N
            String strWeight = pEle.getSubElement("Weight", null).getValue(); // NOI18N
            int weight = new Integer(strWeight).intValue();
            String italic = pEle.getSubElement("Italic", null).getValue(); // NOI18N
            int style = Font.PLAIN;
            if (weight > 400){
                style |= Font.BOLD;
            }
            if (italic.equals("1")){ // NOI18N
                style |= Font.ITALIC;
            }
            if (name != null && name.length() > 0 && height != null && height.length() > 0){
                  pFont = new Font(name, style, new Integer(height).intValue());
            }
        }
        return pFont;
    }
    
}
