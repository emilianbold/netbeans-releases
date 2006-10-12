/*
 * OperationGenerator.java
 *
 * Created on September 6, 2006, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel.PartAndElementOrType;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.RelativePath;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author radval
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
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> inputMessageParts = 
                (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mConfigurationMap.get(WizardPortTypeConfigurationStep.OPERATION_INPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> outputMessageParts = 
                (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mConfigurationMap.get(WizardPortTypeConfigurationStep.OPERATION_OUTPUT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> faultMessageParts = 
                (List<PartAndElementOrTypeTableModel.PartAndElementOrType>) this.mConfigurationMap.get(WizardPortTypeConfigurationStep.OPERATION_FAULT);
            
            List<PartAndElementOrTypeTableModel.PartAndElementOrType> allParts = new ArrayList<PartAndElementOrTypeTableModel.PartAndElementOrType>();
            if (inputMessageParts != null) {
                allParts.addAll(inputMessageParts);
            }
            if (outputMessageParts != null) {
                allParts.addAll(outputMessageParts);
            }
            if (faultMessageParts != null) {
                allParts.addAll(faultMessageParts);
            }
            
            Map<String, String> namespaceToPrefixMap = (Map) this.mConfigurationMap.get(WizardPortTypeConfigurationStep.NAMESPACE_TO_PREFIX_MAP);
            if (namespaceToPrefixMap != null) {
                for (String namespace : namespaceToPrefixMap.keySet()) {
                    ((AbstractDocumentComponent) mModel.getDefinitions()).addPrefix(namespaceToPrefixMap.get(namespace), namespace);
                }
            }
            boolean fromWizard = false;
            if (mConfigurationMap.containsKey(WizardPortTypeConfigurationStep.IS_FROM_WIZARD)) {
                fromWizard = true;
            }
            
            processImports(allParts, fromWizard);
        }
        
    }
    
     private void processImports(List<PartAndElementOrType> allParts, boolean fromWizard) {
         Map<String, String> locationToNamespaceMap = new HashMap<String, String>();
         Map<String, String> existingLocationToNamespaceMap = new HashMap<String, String>();
         
         FileObject wsdlFileObj = (FileObject) mModel.getModelSource().getLookup().lookup(FileObject.class);
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
                         !schemaTNS.equals("http://www.w3.org/2001/XMLSchema")) {
                     
                     FileObject fo = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
                     if (fo != null) {
                         String path = null; 
                         if (fromWizard) {
                             // generate absolute URI, this will get changed later in wizard post process import
                             path = FileUtil.toFile(fo).toURI().toString();
                         } else {
                             path = RelativePath.getRelativePath(FileUtil.toFile(wsdlFileObj).getParentFile(), FileUtil.toFile(fo));
                         }
                         if (!existingLocationToNamespaceMap.containsKey(path) ||
                                 existingLocationToNamespaceMap.get(path) == null ||
                                 !existingLocationToNamespaceMap.get(path).equals(schemaTNS))
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
