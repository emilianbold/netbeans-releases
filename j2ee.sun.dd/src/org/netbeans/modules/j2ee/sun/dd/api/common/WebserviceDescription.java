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
