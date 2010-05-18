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

package org.netbeans.modules.wsdleditorapi.generator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

//import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mbhasin
 */
public class SchemaImportsGenerator implements Command {
    
    private WSDLModel mModel;
    
    private Map mConfigurationMap;
    
    private Collection<Import> mImports = new ArrayList<Import>();
    
    /** Creates a new instance of OperationGenerator */
    public SchemaImportsGenerator(WSDLModel model, Map configurationMap) {
        this.mModel = model;
        this.mConfigurationMap = configurationMap;        
    }
    
    public Collection<Import> getImports() {
        return this.mImports;
    }
    
    public void execute() {
        if(mModel != null) {
            List<PartAndElementOrType> inputMessageParts = 
                (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_INPUT);
            
            List<PartAndElementOrType> outputMessageParts = 
                (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_OUTPUT);
            
            List<PartAndElementOrType> faultMessageParts = 
                (List<PartAndElementOrType>) this.mConfigurationMap.get(WSDLWizardConstants.OPERATION_FAULT);
            
            List<PartAndElementOrType> allParts = new ArrayList<PartAndElementOrType>();
            if (inputMessageParts != null) {
                allParts.addAll(inputMessageParts);
            }
            if (outputMessageParts != null) {
                allParts.addAll(outputMessageParts);
            }
            if (faultMessageParts != null) {
                allParts.addAll(faultMessageParts);
            }
            
            Map<String, String> namespaceToPrefixMap = (Map) this.mConfigurationMap.get(WSDLWizardConstants.NAMESPACE_TO_PREFIX_MAP);
            if (namespaceToPrefixMap != null) {
                for (String namespace : namespaceToPrefixMap.keySet()) {
                    ((AbstractDocumentComponent) mModel.getDefinitions()).addPrefix(namespaceToPrefixMap.get(namespace), namespace);
                }
            }
            boolean fromWizard = false;
            if (mConfigurationMap.containsKey(WSDLWizardConstants.IS_FROM_WIZARD)) {
                fromWizard = true;
            }
            
            processImports(allParts, fromWizard);
        }
        
    }
    
    /* Similiar logic can be found in Utility.addSchemaImport(). So if there are changes here, also change in Utility*/
     private void processImports(List<PartAndElementOrType> allParts, boolean fromWizard) {
         Map<String, String> locationToNamespaceMap = new HashMap<String, String>();
         Map<String, String> existingLocationToNamespaceMap = new HashMap<String, String>();
         
         FileObject wsdlFileObj = mModel.getModelSource().getLookup().lookup(FileObject.class);
         URI wsdlFileURI = FileUtil.toFile(wsdlFileObj).toURI();
         
         Definitions def = mModel.getDefinitions();
         Types types = def.getTypes();
         if (types == null) {
             types = mModel.getFactory().createTypes();
             def.setTypes(types);
         }
         Schema defaultInlineSchema = null;
         String wsdlTNS = def.getTargetNamespace();
         if (wsdlTNS != null) {
             Collection<Schema> schmas = types.getSchemas();
             if (schmas != null) {
                 for (Schema s : schmas) {
                     if (s.getTargetNamespace() != null && s.getTargetNamespace().equals(wsdlTNS)) {
                         defaultInlineSchema = s;
                         break;
                     }
                 }
             }
         }
         
         WSDLSchema wsdlSchema = null;
         if (defaultInlineSchema == null) {
             wsdlSchema = mModel.getFactory().createWSDLSchema();
             SchemaModel schemaModel = wsdlSchema.getSchemaModel();
             defaultInlineSchema = schemaModel.getSchema();
             defaultInlineSchema.setTargetNamespace(mModel.getDefinitions().getTargetNamespace());
         }

         //if any import with same namespace is present, dont import it.
         Collection<Import> imports = defaultInlineSchema.getImports();
         for (Import imp : imports) {
             existingLocationToNamespaceMap.put(imp.getSchemaLocation(), imp.getNamespace());
         }
         
         if (!fromWizard) {
             Collection<Schema> schemas = types.getSchemas();
             if (schemas != null) {
                  for (Schema schema : schemas) {
                      Collection<Import> schemaImports = schema.getImports();
                      for (Import imp : schemaImports) {
                          existingLocationToNamespaceMap.put(imp.getSchemaLocation(), imp.getNamespace());
                      }
                  }
             }
         }
         
         for (PartAndElementOrType part : allParts) {
             ElementOrType eot = part.getElementOrType();
             GlobalElement element = eot.getElement();
             GlobalType type = eot.getType();
             SchemaModel model = null;
             if (element != null) {
                 model = element.getModel();
             } else if (type != null) {
                 model = type.getModel();
             }
             
             if (model != null) {
                 String schemaTNS = model.getSchema().getTargetNamespace();
                 if (schemaTNS != null && 
                         !schemaTNS.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                     
                     FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
                     
                     if (fo != null) {
                         String path = null;

                         if (fromWizard) {
                             // generate absolute URI, this will get changed later in wizard post process import
                             path = FileUtil.toFile(fo).toURI().toString();
                         } 
                         
                         // TODO
                         // Msking catalog support as this require this module to added as friend of
                         // catalog support module which is maintained by base IDE (not soa-dev)
//                         else if (!FileUtil.toFile(fo).toURI().equals(wsdlFileURI)) { 
//                             //should be different files. in case of inline schemas.
//                             DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFileObj);
//                             if (catalogSupport.needsCatalogEntry(wsdlFileObj, fo)) {
//                                 // Remove the previous catalog entry, then create new one.
//                                 URI uri;
//                                 try {
//                                     uri = catalogSupport.getReferenceURI(wsdlFileObj, fo);
//                                     catalogSupport.removeCatalogEntry(uri);
//                                     catalogSupport.createCatalogEntry(wsdlFileObj, fo);
//                                     path = catalogSupport.getReferenceURI(wsdlFileObj, fo).toString();
//                                 } catch (URISyntaxException use) {
//                                     ErrorManager.getDefault().notify(use);
//                                 } catch (IOException ioe) {
//                                     ErrorManager.getDefault().notify(ioe);
//                                 } catch (CatalogModelException cme) {
//                                     ErrorManager.getDefault().notify(cme);
//                                 }
//                             } else {
//                                 path = RelativePath.getRelativePath(FileUtil.toFile(wsdlFileObj).getParentFile(), FileUtil.toFile(fo));
//                             }
//                         }
                         if (path != null && (!existingLocationToNamespaceMap.containsKey(path) ||
                                 existingLocationToNamespaceMap.get(path) == null ||
                                 !existingLocationToNamespaceMap.get(path).equals(schemaTNS)))
                         { 
                             locationToNamespaceMap.put(path, schemaTNS);
                         }
                     }
                 }
             }
         }
         
         
         for (String location : locationToNamespaceMap.keySet()) {
             Import schemaImport =
                 defaultInlineSchema.getModel().getFactory().createImport();
             String namespace = locationToNamespaceMap.get(location);
             schemaImport.setNamespace(namespace);
             schemaImport.setSchemaLocation(location);
             defaultInlineSchema.addExternalReference(schemaImport);
             mImports.add(schemaImport);
             
         }
         
         if (wsdlSchema != null && !locationToNamespaceMap.isEmpty()) {
             types.addExtensibilityElement(wsdlSchema);
         }
    }
}
