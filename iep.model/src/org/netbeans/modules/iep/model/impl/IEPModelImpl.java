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

import java.util.List;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;


import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
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
                    rootComponent = new ComponentImpl (this, root);
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
		// TODO Auto-generated method stub
		return rootComponent;
	}
        
        public Component getComponent() {
            return rootComponent;
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

    

}
