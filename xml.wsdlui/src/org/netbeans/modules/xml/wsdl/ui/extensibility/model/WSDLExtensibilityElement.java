/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
