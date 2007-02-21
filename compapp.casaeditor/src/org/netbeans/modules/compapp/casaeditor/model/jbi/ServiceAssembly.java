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
package org.netbeans.modules.compapp.casaeditor.model.jbi;

import java.util.List;

/**
 *
 * @author jqian
 */
public interface ServiceAssembly extends JBIComponent {
    public static final String IDENTIFICATION_PROPERTY = "identification";
    public static final String SERVICE_UNIT_PROPERTY = "service-unit";
    public static final String CONNECTIONS_PROPERTY = "connections";
    
    Identification getIdentification();
    void setIdentification(Identification identification);
    
    List<ServiceUnit> getServiceUnits();
    void removeServiceUnit(ServiceUnit serviceUnit);
    void addServiceUnit(int index, ServiceUnit serviceUnit);
    
    Connections getConnections();
    void setConnections(Connections connections);
    
}
