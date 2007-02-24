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

import java.lang.reflect.InvocationTargetException;
import java.awt.Color;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

public final class DefinitionColorProperty extends DefinitionPropertySupport {

    public DefinitionColorProperty(IPropertyDefinition def, IPropertyElement element, boolean writable, boolean autoCommit) {
        super(def, element, Color.class, writable, autoCommit);
    }

    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        IPropertyElement element = getElement();
        return buildCurrentColor(element);
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (val instanceof Color) {
            Color col = (Color) val;
            IPropertyElement element = getElement();
            updateColorOnPropertyElement(element, col);
            PreferenceHelper.saveModifiedPreferences(element);
        }
    }
    
    private static Color buildCurrentColor(IPropertyElement pEle)
    {
        Color c = null;
        if (pEle != null)
        {
            String val = pEle.getValue();
            if (val != null && val.length() > 0)
            {
                ETList<String> strs = StringUtilities.splitOnDelimiter(val, ", ");
                if (strs != null)
                {
                    int count = strs.size();
                    if (count == 3)
                    {
                        // RGB
                        Integer i = new Integer(strs.get(0));
                        Integer i2 = new Integer(strs.get(1));
                        Integer i3 = new Integer(strs.get(2));
                        c = new Color(i.intValue(), i2.intValue(), i3.intValue());
                    }
                }
            }
        }
        return c;
    }
    
    private static void  updateColorOnPropertyElement(IPropertyElement pEle, Color pColor)
    {
        if (pEle != null && pColor != null)
        {
            int red = pColor.getRed();
            int green = pColor.getGreen();
            int blue = pColor.getBlue();
            Integer redI = new Integer(red);
            Integer greenI = new Integer(green);
            Integer blueI = new Integer(blue);
            String colorString = redI.toString() + ", " + greenI.toString() + ", " + blueI.toString(); // NOI18N
            pEle.setValue(colorString);
            pEle.setModified(true);
        }
    }
    
}
