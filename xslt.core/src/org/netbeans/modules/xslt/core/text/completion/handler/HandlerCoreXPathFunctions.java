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

package org.netbeans.modules.xslt.core.text.completion.handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xslt.core.XSLTDataLoader;
import org.netbeans.modules.xslt.core.text.completion.IllegalXsltVersionException;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionConstants;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionResultItem;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionUtil;
import org.netbeans.modules.xslt.core.text.completion.XSLTEditorComponentHolder;

/**
 * @author Alex Petrov (16.06.2008)
 */
public class HandlerCoreXPathFunctions extends BaseCompletionHandler implements 
    XSLTCompletionConstants {
    private static Map<String, String>
        // key - String:XSLT_version, value - String:XSLT_schema_file_name
        mapXsltFunctionFileNames = new HashMap<String, String>(3);

    private static Map<String, List<String>>
        // key - String:XSLT_version, value - List:XSLT_core_function_names
        mapXsltFunctionNames = new HashMap<String, List<String>>();
        
    static {
        mapXsltFunctionFileNames.put(XSLT_VERSION_1_0, FILE_XSLT_1_0_FUNCTIONS);
        mapXsltFunctionFileNames.put(XSLT_VERSION_1_1, FILE_XSLT_1_1_FUNCTIONS);
        mapXsltFunctionFileNames.put(XSLT_VERSION_2_0, FILE_XSLT_2_0_FUNCTIONS);
    }
    
    @Override
    public List<XSLTCompletionResultItem> getResultItemList(
        XSLTEditorComponentHolder editorComponentHolder) {
        initHandler(editorComponentHolder);
        return getCoreFunctionList();
    }
    
    private List<XSLTCompletionResultItem> getCoreFunctionList() {
        if ((attributeName == null) || (xslModel == null)) 
            return Collections.emptyList();

        if ((schemaModel == null) || (surroundTag == null) || (attributeName == null)) 
            return Collections.emptyList();
        
        if (! isAttributeTypeXslExpression()) return Collections.emptyList();
        
        List<XSLTCompletionResultItem> resultItemList = 
            new ArrayList<XSLTCompletionResultItem>();
        List<String> listFunctionNames = getFunctionNameList();
        if (listFunctionNames == null) return Collections.emptyList();
            
        for (String functionName : listFunctionNames) {
            if ((functionName != null) && (functionName.trim().length() > 0)) {
                XSLTCompletionResultItem resultItem = new XSLTCompletionResultItem(
                    functionName.trim(), document, caretOffset);
                resultItem.setSortPriority(resultItemList.size());
                resultItemList.add(resultItem);
            }   
        }
        return resultItemList;
    }
    
    private boolean isAttributeTypeXslExpression() {
        NamedReferenceable<SchemaComponent> refSchemaComponent = 
            schemaModel.findByNameAndType(XSLTCompletionUtil.ignoreNamespace(
            surroundTag.getTagName()), GlobalElement.class);
        if (refSchemaComponent == null) return false;    
        
        List<SchemaComponent> children = refSchemaComponent.getChildren();
        List<Attribute> attributes = XSLTCompletionUtil.collectChildrenOfType(children, 
            Attribute.class);
        String attrTypeName = XSLTCompletionUtil.getAttributeType(attributes, 
            attributeName);
        if (attrTypeName == null) return false; 
        
        return ((attrTypeName.equals(ATTRIBUTE_TYPE_XSL_EXPRESSION)) || 
                (attrTypeName.equals(XSLTCompletionUtil.ignoreNamespace(
                    ATTRIBUTE_TYPE_XSL_EXPRESSION))));
    }

    private List<String> getFunctionNameList() {
        InputStream inputStream = null;
        try {
            String xsltVersion = xslModel.getStylesheet().getVersion().toString().trim();
            if (! setSupportedXsltVersions.contains(xsltVersion)) {
                throw new IllegalXsltVersionException(xsltVersion);
            }
            List<String> listFunctionNames = mapXsltFunctionNames.get(xsltVersion);
            if (listFunctionNames == null) { 
                String functionFileName = mapXsltFunctionFileNames.get(xsltVersion);
                String resourcePath = RESOURCES_DIR + "/" + functionFileName;

                inputStream = XSLTDataLoader.class.getResourceAsStream(resourcePath);
                listFunctionNames = getFunctionNameList(inputStream);
                mapXsltFunctionNames.put(xsltVersion, listFunctionNames);
            }
            return listFunctionNames;
        }
        catch (IllegalXsltVersionException ixve) {
            Logger.getLogger(HandlerCoreXPathFunctions.class.getName()).log(
                Level.WARNING, ixve.getMessage(), ixve);
            return null;
        } catch (Exception ex) {
            return null;
        } finally {
            if (inputStream != null) {
                try { inputStream.close();} catch(Exception e) {}
            }
        }
    }
    
    private List<String> getFunctionNameList(InputStream inputStream) {
        if (inputStream == null) return null;
        List<String> listFunctionNames = new ArrayList<String>();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                inputStream));
            while(true) {
                String functionName = inputReader.readLine();
                if ((functionName != null) && (functionName.trim().length() > 0)) {
                    listFunctionNames.add(XSLTCompletionUtil.ignoreNamespace(
                        functionName).trim());
                } else if (functionName == null) {
                    break;
                }
            }
            return listFunctionNames;
        } catch(Exception e) {
            Logger.getLogger(HandlerCoreXPathFunctions.class.getName()).log(
                Level.INFO, e.getMessage(), e);
            return null;
        }
    }
}
