/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * WebserviceDescription.java
 *
 * Created on November 17, 2004, 4:52 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface WebserviceDescription extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName";	// NOI18N
    public static final String WSDL_PUBLISH_LOCATION = "WsdlPublishLocation";	// NOI18N
        
    /** Setter for webservice-description-name property
     * @param value property value
     */
    public void setWebserviceDescriptionName(java.lang.String value);
    /** Getter for webservice-description-name property.
     * @return property value
     */
    public java.lang.String getWebserviceDescriptionName();
    /** Setter for wsdl-publish-location property
     * @param value property value
     */
    public void setWsdlPublishLocation(java.lang.String value);
    /** Getter for wsdl-publish-location property.
     * @return property value
     */
    public java.lang.String getWsdlPublishLocation();
}
