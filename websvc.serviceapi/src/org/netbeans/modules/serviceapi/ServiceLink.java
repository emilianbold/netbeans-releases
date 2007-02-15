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
package org.netbeans.modules.serviceapi;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.PortType;

/**
 * Represents the service coordination information.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public interface ServiceLink {
    /**
     * @return the first role in this service link.
     */
    ServiceInterface getRole1();

    /**
     * @return the second role in this service link; 
     * or null if there is no second role.
     */
    ServiceInterface getRole2();
    
    /**
     * Returns the technology-specific underlying metadata object for this link.  
     * For example, partner link type.
     */
    <T extends Object> T getLinkDescription(Class<T> type);
}
