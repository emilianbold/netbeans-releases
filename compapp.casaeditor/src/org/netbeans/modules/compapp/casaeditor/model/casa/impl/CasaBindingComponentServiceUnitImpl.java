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
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindingComponentServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPorts;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaBindingComponentServiceUnitImpl extends CasaServiceUnitImpl 
        implements CasaBindingComponentServiceUnit {
    
    public CasaBindingComponentServiceUnitImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaBindingComponentServiceUnitImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.BINDING_COMPONENT_SERVICE_UNIT));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
    
    public CasaPorts getPorts() {
        return getChild(CasaPorts.class);
    }
    
    public void setPorts(CasaPorts ports) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaPorts.class, PORTS_PROPERTY, ports, empty);
    }        
}