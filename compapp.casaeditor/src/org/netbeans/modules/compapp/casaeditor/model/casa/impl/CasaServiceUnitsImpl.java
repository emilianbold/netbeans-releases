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
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindingComponentServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnections;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
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
public class CasaServiceUnitsImpl extends CasaComponentImpl implements CasaServiceUnits {
    
    /** Creates a new instance of CasaServiceUnitsImpl */
    public CasaServiceUnitsImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaServiceUnitsImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.SERVICE_UNITS));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }

    public List<CasaServiceEngineServiceUnit> getServiceEngineServiceUnits() {
        return getChildren(CasaServiceEngineServiceUnit.class);
    }

    public void removeServiceEngineServiceUnit(CasaServiceEngineServiceUnit engineServiceUnit) {
         removeChild(SERVICE_ENGINE_SERVICE_UNIT_PROPERTY, engineServiceUnit);
    }

    public void addServiceEngineServiceUnit(int index, CasaServiceEngineServiceUnit engineServiceUnit) {
        insertAtIndex(SERVICE_ENGINE_SERVICE_UNIT_PROPERTY, engineServiceUnit, index, CasaServiceEngineServiceUnit.class);
    }
    
    public List<CasaBindingComponentServiceUnit> getBindingComponentServiceUnits() {
        return getChildren(CasaBindingComponentServiceUnit.class);
    }
    
    public void removeBindingComponentServiceUnit(CasaBindingComponentServiceUnit bindingServiceUnit) {
         removeChild(BINDING_COMPONENT_SERVICE_UNIT_PROPERTY, bindingServiceUnit);
    }

    public void addBindingComponentServiceUnit(int index, CasaBindingComponentServiceUnit bindingServiceUnit) {
        insertAtIndex(BINDING_COMPONENT_SERVICE_UNIT_PROPERTY, bindingServiceUnit, index, CasaBindingComponentServiceUnit.class);
    }
}
