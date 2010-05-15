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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.windows.TopComponent;

/**
 * @author Alex Petrov (30.04.2008)
 */
public class XSLTCompletionUtil implements XSLTCompletionConstants {
    private static enum AttributeNameParsingState {
        START_PARSING, LEFT_QUOTE_FOUND, EQUAL_SIGN_FOUND, CHAR_OF_NAME_FOUND
    };
    
    public static String getAttributeNameBeforeCaret(Document document, int caretOffset, 
        SyntaxElement surroundTag) {
        if ((document == null) || (surroundTag == null) || (caretOffset < 0)) 
            return null;
        
        int startTagPos = surroundTag.getElementOffset();
        if (startTagPos < 0) return null;
        try {
            StringBuffer attributeNameBuf = new StringBuffer();
            getAttributeNameBeforeCaret(attributeNameBuf, document, startTagPos, 
                caretOffset, AttributeNameParsingState.START_PARSING);
            return (attributeNameBuf.length() < 1 ? null : attributeNameBuf.toString());
        } catch (Exception e) {
            Logger.getLogger(XSLTCompletionUtil.class.getName()).log(Level.INFO, 
                e.getMessage(), e);
            return null;
        }
    }
    
    private static void getAttributeNameBeforeCaret(StringBuffer attributeNameBuf, 
        Document document, int startTagPos, int currentPos, 
        AttributeNameParsingState parsingState) throws Exception {
        while (currentPos > startTagPos) {
            --currentPos;
            if (currentPos <= startTagPos) break;

            String text = document.getText(currentPos, 1);
            char lastChar = text.charAt(0);
        
            if (parsingState.equals(AttributeNameParsingState.START_PARSING)) {
                if (lastChar == '"') {
                    parsingState = AttributeNameParsingState.LEFT_QUOTE_FOUND;
                }
            } else if (parsingState.equals(AttributeNameParsingState.LEFT_QUOTE_FOUND)) {
                if (lastChar == '=') {
                    parsingState = AttributeNameParsingState.EQUAL_SIGN_FOUND;
                } else if (! Character.isSpaceChar(lastChar)) {
                    break;
                }
            } else if (parsingState.equals(AttributeNameParsingState.EQUAL_SIGN_FOUND)) {
                if (! Character.isSpaceChar(lastChar)) {
                    parsingState = AttributeNameParsingState.CHAR_OF_NAME_FOUND;
                    attributeNameBuf.insert(0, text);
                }
            } else if (parsingState.equals(AttributeNameParsingState.CHAR_OF_NAME_FOUND)) {
                if (! Character.isSpaceChar(lastChar)) {
                    attributeNameBuf.insert(0, text);
                } else {
                    break;
                }
            }
        }
    } 
    
    public static <T extends SchemaComponent> List<T> collectChildrenOfType(
        List<SchemaComponent> children, Class<T> requiredChildClass) {
        List<T> resultList = new ArrayList<T>();
        for (SchemaComponent child : children) {
            if (child == null) continue;
            
            if (requiredChildClass.isAssignableFrom(child.getClass())) {
                resultList.add((T) child);
            } else {
                List<SchemaComponent> nestedChildren = child.getChildren();
                List<T> nestedResultList = collectChildrenOfType(nestedChildren, 
                    requiredChildClass);
                if ((nestedResultList != null) && (! nestedResultList.isEmpty())) {
                    resultList.addAll(nestedResultList);
                }
            }
        }
        return resultList;
    }
    
    public static String getAttributeType(List<Attribute> attributes, String attributeName) {
        for (Attribute attribute : attributes) {
            String name = attribute.getPeer().getAttribute(ATTRIB_NAME);
            if ((name != null) && (name.equals(attributeName))) {
                String attrTypeName = attribute.getPeer().getAttribute(ATTRIB_TYPE);
                return attrTypeName;
            }
        }
        return null;
    }
    
    public static String ignoreNamespace(String dataWithNamespace) {
        if (dataWithNamespace == null) return null;
        int index = dataWithNamespace.indexOf(":");
        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }
    
    public static XSLTDataEditorSupport getXsltDataEditorSupport() {
        try {
            TopComponent topComponent = TopComponent.getRegistry().getActivated();
            XSLTDataEditorSupport editorSupport = topComponent.getLookup().lookup(
                XSLTDataEditorSupport.class);
            return editorSupport;
        } catch(Exception e) {
            Logger logger = Logger.getLogger(XSLTCompletionUtil.class.getName());
            logger.log(Level.INFO, null, e);
            return null;
        }
    }
    
    public static XslModel getXslModel() {
        XSLTDataEditorSupport editorSupport = getXsltDataEditorSupport();
        if (editorSupport == null) return null;
        XslModel xslModel = editorSupport.getXslModel();
        return xslModel;
    }
}