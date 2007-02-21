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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaServiceEngineServiceUnitImpl extends CasaServiceUnitImpl 
        implements CasaServiceEngineServiceUnit {
    
    public CasaServiceEngineServiceUnitImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaServiceEngineServiceUnitImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.ENGINE_ENGINE_SERVICE_UNIT));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
//    
//    public CasaEndpoints getEndpoints() {
//        return getChild(CasaEndpoints.class);
//    }
//    
//    public void setEndpoints(CasaEndpoints endpoints) {
//        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
//        setChild(CasaEndpoints.class, ENDPOINTS_PROPERTY, endpoints, empty);
//    }
    
    public void addConsumes(int index, CasaConsumes casaConsumes) {
        insertAtIndex(CONSUMES_PROPERTY, casaConsumes, index, CasaConsumes.class);
    }

    public void removeConsumes(CasaConsumes casaConsumes) {
        removeChild(CONSUMES_PROPERTY, casaConsumes);
    }

    public List<CasaConsumes> getConsumes() {
         return getChildren(CasaConsumes.class);
    }
           
    public void addProvides(int index, CasaProvides casaProvides) {
        insertAtIndex(PROVIDES_PROPERTY, casaProvides, index, CasaProvides.class);
    }

    public void removeProvides(CasaProvides casaProvides) {
        removeChild(PROVIDES_PROPERTY, casaProvides);
    }

    public List<CasaProvides> getProvides() {
         return getChildren(CasaProvides.class);
    }
        
    public int getX() {
        return Integer.parseInt(getAttribute(CasaAttribute.X));
    }

    public void setX(int x) {
        setAttribute(X_PROPERTY, CasaAttribute.X, new Integer(x).toString());
    }

    public int getY() {
        return Integer.parseInt(getAttribute(CasaAttribute.Y));
    }

    public void setY(int y) {
        setAttribute(Y_PROPERTY, CasaAttribute.Y, new Integer(y).toString());
    }

    public boolean isInternal() {
        return getAttribute(CasaAttribute.INTERNAL).equalsIgnoreCase("true");
    }

    public void setInternal(boolean internal) {
        setAttribute(INTERNAL_PROPERTY, CasaAttribute.INTERNAL, internal ? "true" : "false");        
    }

    public boolean isDefined() {
        return getAttribute(CasaAttribute.DEFINED).equalsIgnoreCase("true");
    }

    public void setDefined(boolean defined) {
        setAttribute(DEFINED_PROPERTY, CasaAttribute.DEFINED, defined ? "true" : "false");        
    }

    public boolean isUnknown() {
        return getAttribute(CasaAttribute.UNKNOWN).equalsIgnoreCase("true");
    }

    public void setUnknown(boolean unknown) {
        setAttribute(UNKNOWN_PROPERTY, CasaAttribute.UNKNOWN, unknown ? "true" : "false");        
    }
        
    // Convenience methods
    public List<CasaEndpointRef> getEndpoints() {
        List<CasaEndpointRef> ret = new ArrayList<CasaEndpointRef>();
        ret.addAll(getConsumes());
        ret.addAll(getProvides());
        return ret;
    }
       
}