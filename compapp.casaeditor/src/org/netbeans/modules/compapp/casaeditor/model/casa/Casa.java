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
package org.netbeans.modules.compapp.casaeditor.model.casa;

/**
 *
 * @author jqian
 */
public interface Casa extends CasaComponent {
    
    public static final String SERVICE_UNITS_PROPERTY = "service-units";
    public static final String CONNECTIONS_PROPERTY = "connections";
    public static final String PORTTYPES_PROPERTY = "porttypes";
    public static final String BINDINGS_PROPERTY = "bindings";
    public static final String SERVICES_PROPERTY = "services";
    public static final String ENDPOINTS_PROPERTY = "endpoints";
    public static final String REGIONS_PROPERTY = "regions";
    
    CasaServiceUnits getServiceUnits();
    void setServiceUnits(CasaServiceUnits serviceUnits);
    
    CasaConnections getConnections();
    void setConnections(CasaConnections connections);
    
    CasaPortTypes getPortTypes();
    void setPortTypes(CasaPortTypes portTypes);
    
    CasaBindings getBindings();
    void setBindings(CasaBindings bindings);
        
    CasaServices getServices();
    void setServices(CasaServices services);
    
    CasaEndpoints getEndpoints();
    void setEndpoints(CasaEndpoints endpoints);
    
    CasaRegions getRegions();
    void setRegions(CasaRegions regions);
    
}
