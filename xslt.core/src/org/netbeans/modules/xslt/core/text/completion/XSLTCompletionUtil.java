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
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * @author Alex Petrov (30.04.2008)
 */
public class XSLTCompletionUtil {
    public static final String 
        PATTERN_ATTRIB_VALUE_PREFIX = "=\"",
        ATTRIB_NAME = "name",
        ATTRIB_TYPE = "type";
    
    public static boolean attributeValueExpected(Document document, int caretOffset) {
        int startPos = caretOffset - PATTERN_ATTRIB_VALUE_PREFIX.length();
        try {
            boolean result = document.getText(startPos, 
                PATTERN_ATTRIB_VALUE_PREFIX.length()).equals(PATTERN_ATTRIB_VALUE_PREFIX);
            return result;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String extractAttributeName(Document document, int caretOffset, 
        DocumentComponent docComponent) {
        if ((document == null) || (docComponent == null) || (caretOffset < 0)) return null;
        
        int docComponentPos = docComponent.findPosition(),
            startPos = caretOffset - PATTERN_ATTRIB_VALUE_PREFIX.length();
        try {
            int currentPos = startPos - 1;
            StringBuffer strBuf = new StringBuffer();
            while (true) {
                String text = document.getText(currentPos, 1);
                if (! Character.isWhitespace(text.charAt(0))) {
                    strBuf.insert(0, text);
                } else {
                    break;
                }
                if ((--currentPos) <= docComponentPos) break;
            }
            return strBuf.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static List collectChildrenOfType(List children, Class requiredChildClass) {
        List resultList = new ArrayList();
        for (Object child : children) {
            if (requiredChildClass.isAssignableFrom(child.getClass())) {
                resultList.add(child);
            } else {
                List nestedChildren = ((SchemaComponent) child).getChildren();
                List nestedResultList = collectChildrenOfType(nestedChildren, 
                    requiredChildClass);
                if ((nestedResultList != null) && (! nestedResultList.isEmpty())) {
                    resultList.addAll(nestedResultList);
                }
            }
        }
        return resultList;
    }
    
    public static String getAttributeType(List attributes, String attributeName) {
        for (Object obj : attributes) {
            Attribute attribute = (Attribute) obj;
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
    
    public static XslModel getXslModel(Document doc) {
        if (doc == null) return null;
        if (getXsltDataEditorSupport() == null) return null;
        
        ModelSource modelSource = new ModelSource(Lookups.singleton(doc), false);
        XslModelFactory xslModelFactory = XslModelFactory.XslModelFactoryAccess.getFactory();
        XslModel xslModel = xslModelFactory.getModel(modelSource);
        return xslModel;
    }
}