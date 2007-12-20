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

import java.util.List;

import org.openide.loaders.DataObject;

/**
 * @author radval
 *
 * WSDLExtensibilityElementInfoContainer are DataFolder is layer.xml.
 * For a given constant as defined in @see WSDLExtensibilityElements
 * There could be a combination of folder and file under the folder
 * for that constant. folder are represented by WSDLExtensibilityElementInfoContainer
 * and file are represented by WSDLExtensibilityElementInfo
 */
public interface WSDLExtensibilityElementInfoContainer {
    
    /**
     * Get the name of the WSDLExtensibilityElementInfoContainer
     * @return name
     */
    String getName();
    
    /**
     * get the display name of WSDLExtensibilityElementInfoContainer
     * @return displayName
     */
    String getDisplayName();
    
    /**
     * Get the DataObject associated with WSDLExtensibilityElementInfoContainer
     * @return DataObject
     */
    DataObject getDataObject();
    
    /**
     * Get a list of all WSDLExtensibilityElementInfo (which are <file> elements)
     * under this WSDLExtensibilityElementInfoContainer (which is <folder>)
     * @return List of WSDLExtensibilityElementInfo
     */
    List<WSDLExtensibilityElementInfo> getAllWSDLExtensibilityElementInfo();
    
    
    List<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfo(String namespace);
}
