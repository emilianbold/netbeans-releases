/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.core.text.completion;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xslt.model.Param;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 * @author Alex Petrov (16.06.2008)
 */
public class XSLTTemplateParameterResultItem extends XSLTCompletionResultItem {
    private static final String BEFORE_PARAM_NAME_SPACE_GAP = "   ";
    
    private Param parameter; 

    public XSLTTemplateParameterResultItem(String itemText, Document document, 
        int caretOffset) {
        super(itemText, document, caretOffset);
    }
    
    public static XSLTTemplateParameterResultItem create(Param parameter, 
        Document document, int caretOffset) {
        try {
            return  (parameter == null ? null : new XSLTTemplateParameterResultItem(
                    parameter, document, caretOffset));
        } catch(Exception e) {
            return null;
        }
    }
    
    private XSLTTemplateParameterResultItem(Param parameter, Document document, 
        int caretOffset) {
        this("", document, caretOffset);
        this.parameter = parameter;
        QName valueofAttributeName = parameter.getName();
        if (valueofAttributeName == null) throw new NullPointerException(); 
        
        itemText = valueofAttributeName.toString();
    }
    
    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(
            BEFORE_PARAM_NAME_SPACE_GAP + itemText, null, g, defaultFont);
    }
    
    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, 
        Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(null, BEFORE_PARAM_NAME_SPACE_GAP + itemText, 
            null, g, defaultFont, (selected ? Color.white : ITEM_TEXT_COLOR), 
            width, height, selected);
    }
}