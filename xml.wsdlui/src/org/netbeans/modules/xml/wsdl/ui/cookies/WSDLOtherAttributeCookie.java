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
 * Created on Jun 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.cookies;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.openide.nodes.Node;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLOtherAttributeCookie implements Node.Cookie {
	
	private QName mAttrQName;
	
	private WSDLComponent mElement;
	
	private WSDLDataObject mDataObject;
	
	public WSDLOtherAttributeCookie(QName attrQName, 
	        WSDLComponent element,
	        WSDLDataObject dataObject) {
		
		this.mAttrQName = attrQName;
		this.mElement = element;
		this.mDataObject = dataObject;
	}
	
	public WSDLComponent getWSDLComponent() {
		return this.mElement;
	}
	
	public WSDLDataObject getWSDLDataObject() {
		return this.mDataObject;
	}
	
	public QName getAttributeName() {
		return this.mAttrQName;
	}
}

