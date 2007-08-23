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

package org.netbeans.modules.iep.model.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.NameUtil;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.lib.GenUtil;
import org.netbeans.modules.iep.model.lib.IOUtil;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class IEPModelImpl extends IEPModel {
	private Component rootComponent;

	private IEPComponentFactory wcf;

	public IEPModelImpl(ModelSource source) {
		super(source);
		wcf = new IEPComponentFactoryImpl(this);
	}

	@Override
	public IEPComponent createRootComponent(Element root) {
		// TODO Auto-generated method stub
		  String namespace = root.getNamespaceURI();
	        //if ( IEPModel.IEP_NAMESPACE.equals(namespace) &&
                //     IEPModel.IEP_COMPONENT.equals( root.getLocalName() )) 
                if (IEPModel.IEP_COMPONENT.equals( root.getLocalName() )) 
	        {
                    rootComponent = new PlanComponentImpl (this, root);
	            return rootComponent;
	        } 
	        return null;
	}

	@Override
	protected ComponentUpdater<IEPComponent> getComponentUpdater() {
		// TODO Auto-generated method stub
		return new ChildComponentUpdateVisitor<IEPComponent>();
	}

        /*
	public List<WSDLModel> findWSDLModel(String namespaceURI) {
		// TODO Auto-generated method stub
        if (namespaceURI == null) {
            return Collections.emptyList();
        }
        
        List<WSDLModel> models = getImportedWSDLModels();

        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        for (WSDLModel m : models) {
            String targetNamespace = m.getDefinitions().getTargetNamespace();
            if (namespaceURI.equals(targetNamespace)) {
                ret.add(m);
            }
        }
        return ret;
	}

    
    public List<WSDLModel> getImportedWSDLModels() {
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        Collection<TImport> imports = getTasks().getImports();
        for (TImport i:imports) {
            try {
                WSDLModel m = i.getImportedWSDLModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedWSDLModels", e);
            }
        }
        return ret;
    } */	

	public IEPComponentFactory getFactory() {
		// TODO Auto-generated method stub
		return wcf;
	}


	public IEPComponent createComponent(IEPComponent parent, Element element) {
		// TODO Auto-generated method stub
		return parent.createChild(element);
	}

	public IEPComponent getRootComponent() {
		return rootComponent;
	}
        
        public PlanComponent getPlanComponent() {
            return (PlanComponent) rootComponent;
        }
	
	
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        change.setParentComponent(parentComponent);
        return change;
    }

	
    public String getIEPFilePath() {
    	DataObject dObj = getModelSource().getLookup().lookup(DataObject.class);
        return FileUtil.toFile(dObj.getPrimaryFile()).getAbsolutePath();
    }
   
    public String getIEPFileName() {
    	DataObject dObj = getModelSource().getLookup().lookup(DataObject.class);
        return dObj.getPrimaryFile().getNameExt();
    }
    
    public File getWsdlFile() {
        String wsdlPath = getIEPFilePath();
        wsdlPath = wsdlPath.substring(0, wsdlPath.length()-4) + ".wsdl"; //4: for '.iep'
        return new File(wsdlPath);
    }
    
    public void saveWsdl() throws Exception {
        // auto generate .wsdl
        // see org.netbeans.modules.iep.editor.jbiadapter.IEPSEDeployer
        String tns = NameUtil.makeJavaId(getIEPFileName());
        FileOutputStream fos = null;
        try {
            // Generate .wsdl in the same dir as its .iep is
        	IEPWSDLGenerator gen = new IEPWSDLGenerator(this);
        	String wsdl = gen.getWSDL(tns);
            String wsdlPath = getIEPFilePath();
            wsdlPath = wsdlPath.substring(0, wsdlPath.length()-4) + ".wsdl"; //4: for '.iep'
            GenUtil.createFile(new File(wsdlPath), false);
            fos = new FileOutputStream(wsdlPath);
            IOUtil.copy(wsdl.getBytes("UTF-8"), fos);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new Exception("ModelImpl.FAIL_SAVE_MODEL",
//                    "org.netbeans.modules.iep.editor.model.Bundle",
//                    new Object[]{getIEPFilePath()}, e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

}
