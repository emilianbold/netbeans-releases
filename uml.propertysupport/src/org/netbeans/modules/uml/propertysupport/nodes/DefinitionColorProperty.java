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
