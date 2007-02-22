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

package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.loaders.DataObject;


/**
 *
 * @author radval
 *
 */
public class XMLSchemaFileInfoImpl implements XMLSchemaFileInfo {

	private DataObject mDataObject;
	
	private Schema mSchema;
	
	public XMLSchemaFileInfoImpl(DataObject dataObject) {
		this.mDataObject = dataObject;
		this.mSchema = ExtensibilityUtils.readSchema(this.mDataObject);
	}
	
	public Schema getSchema() {
		return this.mSchema;
	}
	
	public DataObject getDataObject() {
		return this.mDataObject;
	}
	
	
	public String getPrefix() {
		Object val = mDataObject.getPrimaryFile().getAttribute(Constants.PREFIX);
		if(val instanceof String) {
			return (String) val;
		}
		
		return null;
	}
	
	public String getNamespace() {
		Object val = mDataObject.getPrimaryFile().getAttribute(Constants.NAMESPACE);
		if(val instanceof String) {
			return (String) val;
		}
		
		return null;
	}
}
