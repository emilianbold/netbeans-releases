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
import org.netbeans.modules.j2ee.dd.api.common.Icon;

public interface PortComponent extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean{
	
        public static final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
	public static final String PORTCOMPONENTNAMEID = "PortComponentNameId";	// NOI18N
	public static final String WSDL_PORT = "WsdlPort";	// NOI18N
	public static final String WSDLPORTID = "WsdlPortId";	// NOI18N
	public static final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
	public static final String SERVICE_IMPL_BEAN = "ServiceImplBean";	// NOI18N
	public static final String HANDLER = "Handler";	// NOI18N
	
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


	public void setPortComponentName(java.lang.String value);

	public java.lang.String getPortComponentName();

	public void setPortComponentNameId(java.lang.String value);

	public java.lang.String getPortComponentNameId();

	//public void setWsdlPort(java.lang.String value);

	//public java.lang.String getWsdlPort();
 
    public void setWsdlPort(org.netbeans.modules.schema2beans.QName value);

	public org.netbeans.modules.schema2beans.QName getWsdlPort();

	public void setWsdlPortId(java.lang.String value);

	public java.lang.String getWsdlPortId();

	public void setServiceEndpointInterface(java.lang.String value);

	public java.lang.String getServiceEndpointInterface();

	public void setServiceImplBean(ServiceImplBean value);

	public ServiceImplBean getServiceImplBean();

	public ServiceImplBean newServiceImplBean();

	public void setHandler(int index, PortComponentHandler value);

	public PortComponentHandler getHandler(int index);

	public int sizeHandler();

	public void setHandler(PortComponentHandler[] value);

	public PortComponentHandler[] getHandler();

	public int addHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler value);

	public int removeHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler value);

	public PortComponentHandler newPortComponentHandler();

}
