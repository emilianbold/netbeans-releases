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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.webservices;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;

public interface PortComponentHandler extends org.netbeans.modules.j2ee.dd.api.common.ComponentInterface {
	
        public Icon newIcon();

	public void setHandlerName(java.lang.String value);

	public java.lang.String getHandlerName();

	public void setHandlerNameId(java.lang.String value);

	public java.lang.String getHandlerNameId();

	public void setHandlerClass(java.lang.String value);

	public java.lang.String getHandlerClass();

	public void setInitParam(int index, InitParam value);

	public InitParam getInitParam(int index);

	public int sizeInitParam();

	public void setInitParam(InitParam[] value);

	public InitParam[] getInitParam();

	public int addInitParam(InitParam value);

	public int removeInitParam(InitParam value);

	public InitParam newInitParam();

	public void setSoapHeader(int index, org.netbeans.modules.schema2beans.QName value);

	public org.netbeans.modules.schema2beans.QName getSoapHeader(int index);

	public int sizeSoapHeader();

	public void setSoapHeader(org.netbeans.modules.schema2beans.QName[] value);

	public org.netbeans.modules.schema2beans.QName[] getSoapHeader();

	public int addSoapHeader(org.netbeans.modules.schema2beans.QName value);

	public int removeSoapHeader(org.netbeans.modules.schema2beans.QName value);

	public void setSoapHeaderId(int index, java.lang.String value);

	public java.lang.String getSoapHeaderId(int index);

	public int sizeSoapHeaderId();

	public void setSoapRole(int index, java.lang.String value);

	public java.lang.String getSoapRole(int index);

	public int sizeSoapRole();

	public void setSoapRole(java.lang.String[] value);

	public java.lang.String[] getSoapRole();

	public int addSoapRole(java.lang.String value);

	public int removeSoapRole(java.lang.String value);

	public void setSoapRoleId(java.lang.String value);

	public java.lang.String getSoapRoleId();

}
