/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties;

import java.util.Collection;
import org.netbeans.modules.bpel.core.BPELCatalog;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.properties.Constants.StandardImportType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.soa.ui.SoaUtil;

/**
 *
 * @author Alexey
 */
public class ImportRegistrationHelper {
    public ImportRegistrationHelper(BpelModel model) {
        this.model = model;
    }
    
    public BpelModel getBpelModel() {
        return model;
    }
    
    public void addImport(Model imp_model){
//System.out.println();
//System.out.println("ADD INPORT");
      addImport(createImport(imp_model));
      
      if ( !(imp_model instanceof WSDLModel)) {
        return;
      }
      Definitions defs = ((WSDLModel) imp_model).getDefinitions();
      
      if (defs == null) {
        return;
      }
      Collection<org.netbeans.modules.xml.wsdl.model.Import> imps = defs.getImports();

      if (imps == null) {
        return;
      }
      for (org.netbeans.modules.xml.wsdl.model.Import i: imps){
        try {
            // check if the imported model is itself #87107
            Model tmpImpModel = i.getImportedWSDLModel();
       
            if (tmpImpModel == null || tmpImpModel.equals(i.getModel())) {
                continue;
            }
            if ( !ResolverUtility.isModelImported(tmpImpModel, model)) {
                addImport(tmpImpModel);
            }
        }
        catch (CatalogModelException e) {
          //Just ignore this type of exception
        }
      }
    }
    
    public void addImport(Import new_imp){
//System.out.println();
//System.out.println("add import: " + new_imp);
        if (new_imp == null) {
            return;
        }
        Process process = model.getProcess();
//System.out.println("process: " + process);

        if (process == null) {
            return;
        }
        if ( !isImported(new_imp)) {
//System.out.println("ADD !!!");
            process.addImport(new_imp);
        }
    }
    
    public Import createImport(Model model){
        FileObject modelFo = SoaUtil.getFileObjectByModel(model);
        if (modelFo != null) {
            return createImport(modelFo);
        }
        // may be this model is known by global catalog
        BPELCatalog bpelCatalog = BPELCatalog.getDefault();
        WsAddressingImportHelper wsAddressingHelper 
                = WsAddressingImportHelper.getInstance(getBpelModel(), this);
        String namespace= null;
        StandardImportType importType = null;
        if (model instanceof SchemaModel) {
            Schema schema = ((SchemaModel)model).getSchema();
            namespace = schema == null ? null : schema.getTargetNamespace();
            importType = StandardImportType.IMPORT_SCHEMA;
        } else if (model instanceof WSDLModel) {
            Definitions defs = ((WSDLModel)model).getDefinitions();
            namespace = defs == null ? null : defs.getTargetNamespace();
            importType = StandardImportType.IMPORT_WSDL;
        }
        
        String modelUri = null;
        if (namespace != null) {
            if (wsAddressingHelper != null && wsAddressingHelper.isAccepted(
                    namespace)) {
                return wsAddressingHelper.createWsAddressingImport(namespace);
            }
            modelUri = bpelCatalog.resolveURI(namespace);
        }
        
        if (modelUri != null && importType != null) {
            return createImport(namespace, modelUri, importType.getImportType());
        }
        
        return null;
    }

    public Import createImport(FileObject fo){
        if (fo != null) {
            StandardImportType importType = StandardImportType.forExt(fo.getExt());
            
            return createImport(Util.getNewModelNamespace(fo, importType),
                    Util.getNewModelLocation(model, fo),
                    importType.getImportType());
        } 
        return null;
    }
    
    public Import createImport(String namespace, String location, String type){
        Import imp = model.getBuilder().createImport();
        if (namespace != null){
            try {
                imp.setNamespace(namespace);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        if (location != null) {
            try {
                //Fix for IZ84824
                location = ResolverUtility.encodeLocation(location);
                
                imp.setLocation(location);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        if (type != null){
            try {
                imp.setImportType(type);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return imp;
    }
    
    private boolean isImported(Import new_imp){
        Process process = model.getProcess();

        if (process == null) {
            return false;
        }
        Import[] imports = process.getImports();
        
        if (imports == null) {
            return false ;
        }
        String namespace = new_imp.getNamespace();
        String location = ResolverUtility.decodeLocation(new_imp.getLocation());
        String type = new_imp.getImportType();
        
        for (Import imp : imports) {
            if (namespace != null && !namespace.equals(imp.getNamespace())) {
                continue;
            }
            if (location != null && !location.equals(ResolverUtility.decodeLocation(imp.getLocation()))) {
                continue;
            }
            if (type != null && !type.equals(imp.getImportType())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private final BpelModel model;
}
