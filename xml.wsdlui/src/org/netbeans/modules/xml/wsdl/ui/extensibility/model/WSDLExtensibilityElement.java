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
package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;



/**
 * @author radval
 *
 * WSDLExtensibilityElement is orginized as <folder> in layer.xml
 * one for each constants as defined in WSDLExtensibilityElements
 */
public interface WSDLExtensibilityElement {

    /**
     * Get the name of the element which is extensibile.
     * This will be one of the constants in @see WSDLExtensibilityElements
     * @return name
     */
    String getName();
    
    /**
     * Get All WSDLExtensibilityElementInfo which are define
     * under this WSDLExtensibilityElement. This will return 
     * all WSDLExtensibilityElementInfo which are grouped under 
     * WSDLExtensibilityElementInfoContainer
     * @return List of all WSDLExtensibilityElementInfo
     */
    List<WSDLExtensibilityElementInfo> getAllWSDLExtensibilityElementInfos();
    
    /**
     * Return only top level WSDLExtensibilityElementInfo which are define
     * under this WSDLExtensibilityElement.  This will not return WSDLExtensibilityElementInfo
     * which are defined under  WSDLExtensibilityElementInfoContainer
     * @return List of top level WSDLExtensibilityElementInfo
     */
    List<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfos();
    
    /**
     * Return all WSDLExtensibilityElementInfoContainer defined under
     * this WSDLExtensibilityElement
     * @return List of WSDLExtensibilityElementInfoContainer
     */
    List<WSDLExtensibilityElementInfoContainer> getAllWSDLExtensibilityElementInfoContainers();
    
    /**
     * Check if there are zero or more WSDLExtensibilityElementInfo
     * under this WSDLExtensibilityElement
     * @return true of there are more than zero WSDLExtensibilityElementInfo
     */
    boolean isExtensibilityElementsAvailable();
    
    /**
     * Get a particular WSDLExtensibilityElementInfo based on the matching QName
     * WSDLExtensibilityElementInfo represents one schema element which is
     * from a wsdl extension schema.
     * @param elementQName name of the element
     * @return WSDLExtensibilityElementInfo
     */
    WSDLExtensibilityElementInfo getWSDLExtensibilityElementInfos(QName elementQName);
    
    
    Collection<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfos(String namespace);
}
