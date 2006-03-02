/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
