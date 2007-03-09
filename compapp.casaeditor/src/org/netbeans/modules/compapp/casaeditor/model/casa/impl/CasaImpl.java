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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnections;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPortTypes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegions;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnits;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServices;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaImpl extends CasaComponentImpl implements Casa {
    
    /** Creates a new instance of CasaImpl */
    public CasaImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.CASA));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }

    public CasaServiceUnits getServiceUnits() {
        return getChild(CasaServiceUnits.class);
    }

    public void setServiceUnits(CasaServiceUnits serviceUnits) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaServiceUnits.class, SERVICE_UNITS_PROPERTY, serviceUnits, empty);
    }

    public CasaConnections getConnections() {
        return getChild(CasaConnections.class);
    }

    public void setConnections(CasaConnections connections) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaConnections.class, CONNECTIONS_PROPERTY, connections, empty);
    }

    public CasaPortTypes getPortTypes() {
        return getChild(CasaPortTypes.class);
    }

    public void setPortTypes(CasaPortTypes portTypes) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaPortTypes.class, PORTTYPES_PROPERTY, portTypes, empty);
    }

    public CasaBindings getBindings() {
        return getChild(CasaBindings.class);
    }

    public void setBindings(CasaBindings bindings) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaBindings.class, BINDINGS_PROPERTY, bindings, empty);
    }

    public CasaServices getServices() {
        return getChild(CasaServices.class);
    }

    public void setServices(CasaServices services) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaServices.class, SERVICES_PROPERTY, services, empty);
    }

    public CasaEndpoints getEndpoints() {
        return getChild(CasaEndpoints.class);
    }

    public void setEndpoints(CasaEndpoints endpoints) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaEndpoints.class, ENDPOINTS_PROPERTY, endpoints, empty);
    }

    public CasaRegions getRegions() {
        return getChild(CasaRegions.class);
    }

    public void setRegions(CasaRegions regions) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaRegions.class, REGIONS_PROPERTY, regions, empty);
    }
    
    // HACK FIXME
    public void setDefaultNamespace(String ns) {
        setAttribute("DefaultNamespace", CasaAttribute.NS, ns); // NOI18N
    }

}
