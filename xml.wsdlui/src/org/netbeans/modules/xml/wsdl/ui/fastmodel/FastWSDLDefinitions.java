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
 * Created on Jan 26, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.fastmodel;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Import;


/**
 * @author radval
 *
 * A FastWSDLDefinitions represent a wsdl document
 * with only some content of the wsdl document parsed in it.
 */
public interface FastWSDLDefinitions {
	
	/**
	 * isWSDL can be used to check if it is really a wsdl
	 * sometimes if wsdl is at url and extension is not know
	 * then calling isWSDL will let you know whether it is really a wsdl
	 * @return
	 */
	public boolean isWSDL();
	
	public String getTargetNamespace();
	
	public void setTargetNamespace(String tNamespace);
	
	public String getParseErrorMessage();
	
	public void setParseErrorMessage(String errorMessage);
	
	public Import createImport();
	
	public void addImport(Import imp);
	
	public List getImports();
	
	public Import getImport(String namespace);
}

