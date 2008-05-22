package org.netbeans.modules.iep.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

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
        
            public void generateOperatorSchema(MultiWSDLComponentReference ref) {
                NamedComponentReference<Message> msgRef = ref.getMessage();

                if(msgRef != null) {
                    Message msg = msgRef.get();
                    if(msg != null) {
                        Collection<Part> parts = msg.getParts();
                        Iterator<Part> it = parts.iterator();
                        //(1) More than one part:
                        //all parts should be of builtin types
                        //if a part is not builting type it will
                        //be generated as CLOB
                        
                        //(2) Only one part
                        if(it.hasNext()) {
                            Part part = it.next();
                            //(a) part has element
                            NamedComponentReference<GlobalElement> element = part.getElement();
                            if(element != null && element.get() != null) {
                                GlobalElement ge = element.get();
                                //ge should not be of simple type ex:string
                                //we can not handle this case
                                //to handle any message we need to ask user to
                                //pick what he wants to get out of message
                                //and name them as schema attributes of
                                //an operator, we 
                                //keep xpath mappings to extract these fields
                                //then we can have a generic solution.
                                
                                //for now assume that this element is wrapper
                                //and content is within it.
                                
                            }
                            
                            //(b) part has type
                            NamedComponentReference<GlobalType> type = part.getType();
                            if(type != null && type.get() != null) {
                                GlobalType gt = type.get();
                            }
                        }
                    }
                }
            }
            
            
            public static String getPackageName(DataObject dataObject) {
                
                String packageName = "";
                
                FileObject iepFile = dataObject.getPrimaryFile();
                FileObject iepFileParent = iepFile.getParent();
                if(iepFile != null) {
                    Project project = FileOwnerQuery.getOwner(iepFile);
                    if(project != null) {
                        Sources sources = ProjectUtils.getSources(project);
                        if(sources != null) {
                            //SourceGroup[] sg = sources.getSourceGroups(Sources.TYPE_GENERIC);
                            SourceGroup[] sg = sources.getSourceGroups("BIZPRO");
                            
                            if(sg != null) {
                                for(int i =0; i < sg.length; i++) {
                                    FileObject rootFolder = sg[i].getRootFolder();
                                    if(FileUtil.isParentOf(rootFolder, iepFileParent)) {
                                        packageName = FileUtil.getRelativePath(rootFolder, iepFileParent);
                                        break;
                                    }
                                }
                                
                            }
                        }
                    }
                }
                
                if(packageName != null) {
//                    int dotIndex = packageName.lastIndexOf(".");
//                    if(dotIndex != -1) {
//                        packageName = packageName.substring(0, dotIndex) + "_" + qualifiedName.substring(dotIndex +1, qualifiedName.length());
//                    }
                    
                    packageName = packageName.replaceAll("/", ".");
                } else {
                    //no package so iep file resides in src folder
                    // so "" indicate default package
                    packageName = ""; 
                }
                
                return packageName;
                
            }
}
