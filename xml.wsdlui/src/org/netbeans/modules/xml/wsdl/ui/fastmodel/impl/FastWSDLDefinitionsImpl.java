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

/*
 * Created on Mar 10, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.fastmodel.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.ui.fastmodel.FastWSDLDefinitions;






/**
 * @author radval
 *
 * A FastWSDLDefinitions represent a wsdl document
 * with only some content of the wsdl document parsed in it.
 */
public class FastWSDLDefinitionsImpl implements FastWSDLDefinitions {
	
	private String targetNamespace; 
	
	private String parseErrorMessage;
	
	private List imports = new ArrayList();
	
	private boolean isWSDL = false;
	
	public FastWSDLDefinitionsImpl() {
		
	}
	
	public String getTargetNamespace() {
		return this.targetNamespace;
	}
	
	public void setTargetNamespace(String tNamespace) {
		this.targetNamespace = tNamespace;
	}
	
	public String getParseErrorMessage() {
		return this.parseErrorMessage;
	}
	
	public void setParseErrorMessage(String errorMessage) {
		this.parseErrorMessage = errorMessage;
	}
	
	public void addImport(Import imp) {
		this.imports.add(imp);
	}
	
	public List getImports() {
		return this.imports;
	}
	
	public Import getImport(String namespace) {
		if(namespace == null) {
			return null;
		}
		
		Import imp = null;
		Iterator it = this.imports.iterator();
		
		while(it.hasNext()) {
			Import im = (Import) it.next();
			if(namespace.equals(im.getNamespace()/*im.getNamespaceAttr()*/)) {
				imp = im;
				break;
			}
		}
		
		return imp;
	}
	
	public Import createImport() {
		return null;//TODO:SKINI new ImportImpl();
	}
	
	public boolean isWSDL() {
		return isWSDL;
	}
	
    void setWSDL(boolean wsdl) {
    	this.isWSDL = wsdl;
	}
}
