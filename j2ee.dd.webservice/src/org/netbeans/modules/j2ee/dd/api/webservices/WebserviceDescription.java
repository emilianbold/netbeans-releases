/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 * This interface has all of the bean info accessor methods.
 * 
 * @Generated
 */

package org.netbeans.modules.j2ee.dd.api.webservices;
import org.netbeans.api.web.dd.Icon;

public interface WebserviceDescription extends org.netbeans.api.web.dd.common.CommonDDBean{
	
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
