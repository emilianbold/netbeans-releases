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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import java.awt.Component;
import java.nio.CharBuffer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An editor for properties that take CSS style classes. The editor will accept
 * one or more of the style classes defined by all CSS style sheets in the
 * current project. Inline editing of style classes is allowed, however, the
 * style class names entered must correspond to style classes defined by style
 * sheets in project scope.
 *
 * @author gjmurphy
 */
public class StyleClassPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.StyleClassPropertyEditor {

    List styleClassList;

    String[] getStyleClasses() {
        if (this.styleClassList == null)
            return new String[0];
        return (String[]) this.styleClassList.toArray(new String[this.styleClassList.size()]);
    }

    void setStyleClasses(String[] styleClasses) {
        this.styleClassList = Arrays.asList(styleClasses);
    }

    @Override
    public Object getValue() {
        if (styleClassList == null || styleClassList.size() == 0)
            return null;
        return getAsText();
    }

    @Override
    public void setValue(Object value) {
        String text = (String) value;
        if (text == null || text.trim().length() == 0) {
            this.styleClassList = null;
        } else {
            this.styleClassList = Arrays.asList(text.trim().split("\\s"));
        }
    }

    @Override
    public String getAsText() {
        if (styleClassList == null || styleClassList.size() == 0)
            return "";
        StringBuffer buffer = new StringBuffer();
        buffer.append(styleClassList.get(0));
        for (int i = 1; i < styleClassList.size(); i++) {
            buffer.append(" ");
            buffer.append(styleClassList.get(i));
        }
        return buffer.toString();
    }

    @Override
    public void setAsText(String text) {
        text = text.trim();
        if (text == null || text.length() == 0) {
            this.styleClassList = null;
        } else {            
            String[] styleClasses = null;
            String styleClass = null;
            if (text.contains(",")) {            
                 // text is comma delimited.
                 int size = text.length();
                 CharBuffer charBuff = CharBuffer.allocate(size);
                 char c;
                 boolean commaFound = false;
                 for (int index = 0; index < size; index++) {
                     c = text.charAt(index);
                     if (c == ',') {
                         commaFound = true;                         
                         // Check if the previous charecters are space.
                         // If so, set the position to the first non-space
                         // charecter.
                         int pos = charBuff.position() - 1;                         
                         while (charBuff.get(pos) == ' ') {                                                 
                             pos--;
                         }
                         charBuff.position(++pos);
                         
                     }
                     if (commaFound && c != ',') {
                         if (c == ' ') {
                             // Don't add spaces that are
                             // followed the comma.
                             continue;
                         } else {
                            commaFound = false;
                         }
                     }
                     charBuff.append(c);
                 }
                 // rewind the buffer.
                 charBuff.rewind();
                 String newText = charBuff.toString();
                 styleClasses = newText.trim().split(" ");
                 this.styleClassList = new ArrayList();
                 for (int i = 0; i < styleClasses.length; i++) {
                     styleClass = styleClasses[i];
                     if (styleClass.length() != 0) {
                         this.styleClassList.add(styleClass);
                     }
                 }
            } else {
                // text is space separated.
                styleClasses = text.split("\\s");
                this.styleClassList = new ArrayList();                   
                for (int i = 0; i < styleClasses.length; i++) {
                    this.styleClassList.add(styleClasses[i]);
                }            
            }
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return new StyleClassPropertyPanel(this);
    }

    String[] getAvailableStyleClasses() {
        DesignProperty designProp = this.getDesignProperty();
        if (designProp == null)
            return new String[0];
        DesignContext designContext = designProp.getDesignBean().getDesignContext();
        // According to the API documentation, this should return an array of
        // StyleClassDescriptor objects, but this does not appear to have been
        // implemented yet.
        Object[] styleClasses = (Object[]) designContext.getContextData(
                Constants.ContextData.CSS_STYLE_CLASS_DESCRIPTORS);
        String[] styleClassNames = new String[styleClasses.length];
        for (int i = 0; i < styleClasses.length; i++)
            styleClassNames[i] = styleClasses[i].toString();
        Collator collator = Collator.getInstance(Locale.US);
        collator.setStrength(Collator.PRIMARY);
        Arrays.sort(styleClassNames, collator);
        return styleClassNames;
    }
}
