/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * Import.java
 *
 * Created on November 11, 2005, 3:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model;

/**
 *
 * @author rico
 * Represents an import wsdl statement to import other namespaces.
 */
public interface Import extends WSDLComponent{
    public static final String NAMESPACE_URI_PROPERTY = "namespaceURI";
    public static final String LOCATION_PROPERTY = "location";
    
    void setNamespaceURI(String namespaceURI);
    String getNamespaceURI();
    
    void setLocation(String locationURI);
    String getLocation();
}
