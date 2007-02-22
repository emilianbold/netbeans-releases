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
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLExtensibilityElementInfoImpl implements WSDLExtensibilityElementInfo {

	private static final Logger mLogger = Logger.getLogger(WSDLExtensibilityElementInfoImpl.class.getName());
	
	private DataObject mDataObject;
	
	private Schema mSchema;
	
	private XMLSchemaFileInfo mSchemaFileInfo = null;
	
	public WSDLExtensibilityElementInfoImpl(DataObject dataObject, XMLSchemaFileInfo schemaFileInfo) {
		this.mDataObject = dataObject;
		if ((dataObject instanceof DataFolder) && dataObject.getPrimaryFile().hasExt(Constants.XSD_EXT)) {
		    this.mSchema = ExtensibilityUtils.readSchema(dataObject);
		}
		this.mSchemaFileInfo = schemaFileInfo;
	}
	
	public String getElementName() {
		Object val = mDataObject.getPrimaryFile().getAttribute(Constants.ELEMENT);
		if(val instanceof String) {
			return (String) val;
		}
		
		return null;
	}
	
	public GlobalElement getElement() {
		String name = getElementName();
		if(name != null && getSchema() != null) {
            Collection elements = getSchema().getElements();
            Iterator iter = elements.iterator();
            while (iter.hasNext()) {
                GlobalElement elem = (GlobalElement) iter.next();
                if (elem.getName().equals(name)) {
                    return elem;
                }
            }
		}
		
		return null;
	}
	
	public DataObject getDataObject() {
		return mDataObject;
	}
	
	public Schema getSchema() {
		if(mSchema != null) {
			return mSchema;
		} else if(mSchemaFileInfo != null) {
			return mSchemaFileInfo.getSchema();
		}
		
		return null;
	}
	
	public String getPrefix() {
		Object val = mDataObject.getPrimaryFile().getAttribute(Constants.PREFIX);
		if(val instanceof String) {
			return (String) val;
		} else if(mSchemaFileInfo != null) {
			return mSchemaFileInfo.getPrefix();
		}
		
		return null;
	}
	
}
