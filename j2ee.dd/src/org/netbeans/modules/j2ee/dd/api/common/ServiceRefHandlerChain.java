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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 *
 * @author Martin Adamek
 */
public interface ServiceRefHandlerChain {
    
    int addHandler(ServiceRefHandler valueInterface);
    ServiceRefHandler[] getHandler();
    ServiceRefHandler getHandler(int index);
    String getPortNamePattern();
    String getProtocolBindings();
    String getServiceNamePattern();
    ServiceRefHandler newServiceRefHandler();
    int removeHandler(ServiceRefHandler valueInterface);
    void setHandler(int index, ServiceRefHandler valueInterface);
    void setHandler(ServiceRefHandler[] value);
    void setPortNamePattern(String value);
    void setProtocolBindings(String value);
    void setServiceNamePattern(String value);
    int sizeHandler();
    
}
