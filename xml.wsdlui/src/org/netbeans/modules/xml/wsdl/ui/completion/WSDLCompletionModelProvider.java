/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.wsdl.ui.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.ExtensibilityUtils;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.WSDLElementNode;

/**
 * CompletionModelProvider for WSDL document. The extensibility elements need to write their own completionmodelprovider.
 *
 * @author Shivanand (shivanand.kini@sun.com)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.class)
public class WSDLCompletionModelProvider extends CompletionModelProvider {
    
    String XMLNS_COLON_CONSTANT = XMLConstants.XMLNS_ATTRIBUTE + ":";
    
    public WSDLCompletionModelProvider() {
    }   

    /**
     * Returns a list of CompletionModel. Default implementation looks for
     * schemaLocation attribute in the document and if specified creates model
     * for each schema mentioned in there.
     */    
    @Override
    public List<CompletionModel> getModels(CompletionContext context) {
        String ext = (context != null && context.getPrimaryFile() != null) ? context.getPrimaryFile().getExt() : null;
        
        //check the ext is wsdl
        if (ext != null && !ext.equals("wsdl")) {
            return null;
        }
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        
        String extensibilityElementType = null;
        
        List<QName> path = context.getPathFromRoot();
        if (path != null && !path.isEmpty()) {
            QName elementQName = path.get(path.size() - 1);
            extensibilityElementType = ExtensibilityUtils.getExtensibilityElementType(elementQName);
        }
        
        
        try {
            WSDLExtensibilityElements elements = WSDLExtensibilityElementsFactory.getInstance().getWSDLExtensibilityElements();
            XMLSchemaFileInfo wsdlXMLSchemaInfo = elements.getXMLSchemaFileInfo(WSDLElementNode.WSDL_NAMESPACE);
            
            List<XMLSchemaFileInfo> xmlSchemaFileInfos = new ArrayList<XMLSchemaFileInfo>();
            
            //Get only those models that make sense at this point
            if (extensibilityElementType != null) {
                WSDLExtensibilityElement element = elements.getWSDLExtensibilityElement(extensibilityElementType);
                if (element != null) {
                    List<WSDLExtensibilityElementInfo> infos = element.getAllWSDLExtensibilityElementInfos();
                    for (WSDLExtensibilityElementInfo info : infos) {
                        String ns = info.getSchema().getTargetNamespace();
                        xmlSchemaFileInfos.add(elements.getXMLSchemaFileInfo(ns));
                    }
                }
            }
            
            if (xmlSchemaFileInfos.isEmpty()) {
                xmlSchemaFileInfos = Arrays.asList(elements.getAllXMLSchemaFileInfos());
            } else {
                //Add wsdl xsd
                xmlSchemaFileInfos.add(wsdlXMLSchemaInfo);
            }
            

            
            for (XMLSchemaFileInfo info : xmlSchemaFileInfos) {
                CompletionModel model = createExtensibilityElementSchemaCompletionModel(info, context);
                if (model != null) {
                    models.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return models;
    }
    
    private CompletionModel createExtensibilityElementSchemaCompletionModel(XMLSchemaFileInfo info, CompletionContext context) {
        if (info.getSchema() != null) {
            return new ExtensibilityElementCompletionModel(context, info);
        }
        return null;
    }
    
    class ExtensibilityElementCompletionModel extends CompletionModel {

        XMLSchemaFileInfo xmlSchemaFileInfo;
        CompletionContext context;
        String prefix;
        
        public ExtensibilityElementCompletionModel(CompletionContext context, XMLSchemaFileInfo xmlSchemaFileInfo) {
            this.xmlSchemaFileInfo = xmlSchemaFileInfo;
            this.context = context;
        }
        
        @Override
        public SchemaModel getSchemaModel() {
            return xmlSchemaFileInfo.getSchema().getModel();
        }

        @Override
        public String getSuggestedPrefix() {
            if (prefix != null) return prefix;
            
            return prefix = generatePrefix();
        }
        
        
        private String generatePrefix() {   
            String tns = getTargetNamespace();
            String dns = context.getDefaultNamespace();
            if (dns != null && dns.equals(tns)) {
                return "";
            }

            HashMap<String, String> map = context.getDeclaredNamespaces();
            if (map.containsValue(tns)) {
                for (String key : map.keySet()) {
                    String ns = map.get(key);
                    if (ns.equals(tns)) {
                        return key.replace(XMLNS_COLON_CONSTANT, "");
                    }
                }
            }

            String suggestedPrefix = xmlSchemaFileInfo.getPrefix();

            String nsDecl = XMLNS_COLON_CONSTANT +suggestedPrefix;
            if (map.containsKey(nsDecl)) {
                if (map.get(nsDecl).equals(tns)) {
                    return suggestedPrefix;
                }
                //generate a new prefix.
                int i = 0;
                String newPrefix = suggestedPrefix; 
                while(context.getDeclaredNamespaces().get(nsDecl) != null) {
                    String ns = context.getDeclaredNamespaces().get(nsDecl);
                    if(ns.equals(tns))
                        return newPrefix;

                    newPrefix = newPrefix + i;  //NOI18N
                    nsDecl = XMLNS_COLON_CONSTANT + newPrefix;
                    i++;
                }        
                return newPrefix;
            }

            return suggestedPrefix;
        }
        

        @Override
        public String getTargetNamespace() {
            return xmlSchemaFileInfo.getSchema().getTargetNamespace();
        }
        
    }
        
}
