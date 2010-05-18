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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
