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
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceAssembly;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Services;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class JBIImpl extends JBIComponentImpl implements JBI {
    
    /** Creates a new instance of JBIImpl */
    public JBIImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public JBIImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.JBI));
    }
    
    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }
    
    public Services getServices() {
        return getChild(Services.class);
    }

    public void setServices(Services services) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(Services.class, SERVICES_PROPERTY, services, empty);
    }

    public ServiceAssembly getServiceAssembly() {
        return getChild(ServiceAssembly.class);
    }

    public void setServiceAssembly(ServiceAssembly serviceAssembly) {
        List<Class<? extends JBIComponent>> empty = Collections.emptyList();
        setChild(ServiceAssembly.class, SERVICE_ASSEMBLY_PROPERTY, serviceAssembly, empty);
    }
}
