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

public interface WebserviceDescription extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean{
	
        public static final String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName";	// NOI18N
	public static final String WEBSERVICEDESCRIPTIONNAMEID = "WebserviceDescriptionNameId";	// NOI18N
	public static final String WSDL_FILE = "WsdlFile";	// NOI18N
	public static final String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile";	// NOI18N
	public static final String PORT_COMPONENT = "PortComponent";	// NOI18N
        
	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setDescription(java.lang.String value);

	public java.lang.String getDescription();

	public void setDescriptionId(java.lang.String value);

	public java.lang.String getDescriptionId();

	public void setDescriptionXmlLang(java.lang.String value);

	public java.lang.String getDescriptionXmlLang();

	public void setDisplayName(java.lang.String value);

	public java.lang.String getDisplayName();

	public void setDisplayNameId(java.lang.String value);

	public java.lang.String getDisplayNameId();

	public void setDisplayNameXmlLang(java.lang.String value);

	public java.lang.String getDisplayNameXmlLang();

	public void setIcon(Icon value);

	public Icon getIcon();

	public Icon newIcon();


	public void setWebserviceDescriptionName(java.lang.String value);

	public java.lang.String getWebserviceDescriptionName();

	public void setWebserviceDescriptionNameId(java.lang.String value);

	public java.lang.String getWebserviceDescriptionNameId();

	public void setWsdlFile(java.lang.String value);

	public java.lang.String getWsdlFile();

	public void setJaxrpcMappingFile(java.lang.String value);

	public java.lang.String getJaxrpcMappingFile();

	public void setPortComponent(int index, PortComponent value);

	public PortComponent getPortComponent(int index);

	public int sizePortComponent();

	public void setPortComponent(PortComponent[] value);

	public PortComponent[] getPortComponent();

	public int addPortComponent(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent value);

	public int removePortComponent(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent value);

	public PortComponent newPortComponent();

}
