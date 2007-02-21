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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connections;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceAssembly;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class ServiceAssemblyImpl extends JBIComponentImpl implements ServiceAssembly {
    
    /** Creates a new instance of ServiceAssemblyImpl */
    public ServiceAssemblyImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public ServiceAssemblyImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.SERVICE_ASSEMBLY));
    }

    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }

    public Identification getIdentification() {
        return getChild(Identification.class);
    }

    public void setIdentification(Identification identification) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(Identification.class, IDENTIFICATION_PROPERTY, identification, empty);
    }

    public List<ServiceUnit> getServiceUnits() {
        return getChildren(ServiceUnit.class);
    }

    public void removeServiceUnit(ServiceUnit serviceUnit) {
        removeChild(SERVICE_UNIT_PROPERTY, serviceUnit);
    }

    public void addServiceUnit(int index, ServiceUnit serviceUnit) {
        insertAtIndex(SERVICE_UNIT_PROPERTY, serviceUnit, index, ServiceUnit.class);
    }

    public Connections getConnections() {
        return getChild(Connections.class);
    }

    public void setConnections(Connections connections) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(Connections.class, CONNECTIONS_PROPERTY, connections, empty);
    }
}
