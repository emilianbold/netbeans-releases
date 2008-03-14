package org.netbeans.modules.iep.model;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ModelHelper {

	public static OperatorComponent findOperator(String id, IEPModel model) {
		if(id == null || model == null) {
			return null;
		}
		
		OperatorComponent operatorComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		OperatorComponentContainer operatorsComponent = pComponent.getOperatorComponentContainer();
		if(operatorsComponent != null) {
			operatorComponent = operatorsComponent.findChildComponent(id);
		}
		
		return operatorComponent;
		
	}
	
	public static LinkComponent findLink(OperatorComponent fromComponent, OperatorComponent toComponent, IEPModel model) {
		LinkComponent linkComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		LinkComponentContainer linksComponent = pComponent.getLinkComponentContainer();
		if(linksComponent != null) {
			List<LinkComponent> operators = linksComponent.getAllLinkComponents();
			Iterator<LinkComponent> it = operators.iterator(); 
			while(it.hasNext()) {
				LinkComponent lc = it.next();
				OperatorComponent fc = lc.getFrom();
				OperatorComponent tc = lc.getTo();
				
				if(fc != null 
				   && fc.equals(fromComponent)
				   && tc != null 
				   && tc.equals(toComponent)) {
					linkComponent = lc;
					break;
				}
			}
		}
		
		return linkComponent;
		
	}
	
	public static LinkComponent findLink(String name, IEPModel model) {
		if(name == null) {
			return null;
		}
		
		LinkComponent linkComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		LinkComponentContainer linksComponent = pComponent.getLinkComponentContainer();
		if(linksComponent != null) {
			List<LinkComponent> operators = linksComponent.getAllLinkComponents();
			Iterator<LinkComponent> it = operators.iterator(); 
			while(it.hasNext()) {
				LinkComponent lc = it.next();
				
				if(name.equals(lc.getName())) {
					linkComponent = lc;
					break;
				}
			}
		}
		
		return linkComponent;
		
	}
	public static SchemaComponent findSchema(String name, IEPModel model) {
		if(name == null) {
			return null;
		}
		
		SchemaComponent schemaComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		SchemaComponentContainer schemasComponent = pComponent.getSchemaComponentContainer();
		if(schemasComponent != null) {
			List<SchemaComponent> schemas = schemasComponent.getAllSchemaComponents();
			Iterator<SchemaComponent> it = schemas.iterator(); 
			while(it.hasNext()) {
				SchemaComponent oc = it.next();
				if(name.equals(oc.getName())) {
					schemaComponent = oc;
					break;
				}
			}
		}
		
		return schemaComponent;		
	}
        
        public static WSDLModel getWSDLModel(IEPModel iepModel, 
                                             Import imp) throws Exception {
                WSDLModel model = null;
                FileObject wsdlFileObject = resolveWSDLFileObject(iepModel, imp);
                if(wsdlFileObject != null) {
                    ModelSource modelSource = Utilities.getModelSource(wsdlFileObject, false); 
                    model = getWSDLModel(modelSource);
                }
                
                return model;
            }
	
        
            public static FileObject resolveWSDLFileObject(IEPModel iepModel, 
                                                           Import imp) throws Exception {
                
                FileObject wsdlFile = null;
                FileObject iepFile = iepModel.getModelSource().getLookup().lookup(FileObject.class);

                if(iepFile != null) {
                    String importType = imp.getImportType();
                    if(Import.WSDL_IMPORT_TYPE.equals(importType)) {
                        String location = imp.getLocation();

                        if(location != null){
                            wsdlFile = iepFile.getParent().getFileObject(location);
                        }
                    }
                }
                 
                return wsdlFile;
            }
            
            public static WSDLModel getWSDLModel(ModelSource wsdlSource) throws Exception {
                WSDLModel model = WSDLModelFactory.getDefault().getModel(wsdlSource);
                return model;
                    
            }
        
}
