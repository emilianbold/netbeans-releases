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
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Endpoint;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.GenericExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPorts;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaPortImpl extends CasaComponentImpl implements CasaPort {
    
    public CasaPortImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaPortImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.PORT));
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
    
    public CasaLink getLink() {
        return getChild(CasaLink.class);
    }
    
    public void setLink(CasaLink link) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaLink.class, LINK_PROPERTY, link, empty);
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
//
//    public String getBindingState() {
//        return getAttribute(CasaAttribute.BINDINGSTATE.getName());
//    }
//
//    public void setBindingState(String bindingState) {
//        setAttribute(CasaAttribute.BINDINGSTATE.getName(), bindingState);
//    }
    
    public String getPortType() {
        return getAttribute(CasaAttribute.PORTTYPE);
    }
    
    public void setPortType(String portType) {
        setAttribute(PORTTYPE_PROPERTY, CasaAttribute.PORTTYPE, portType);
    }
    
    public String getBindingType() {
        return getAttribute(CasaAttribute.BINDINGTYPE);
    }
    
    public void setBindingType(String bindingType) {
        setAttribute(BINDINGTYPE_PROPERTY, CasaAttribute.BINDINGTYPE, bindingType);
    }
    
    // Convenience method
//
//    public void setLocation(int x, int y) {
//        CasaLocation location = getLocation();
//        location.setX(x);
//        location.setY(y);
//    }
//
    public CasaConsumes getConsumes() {
        return getChild(CasaConsumes.class);
    }
    
    public void setConsumes(CasaConsumes casaConsumes) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaConsumes.class, CONSUMES_PROPERTY, casaConsumes, empty);
    }
    
    public CasaProvides getProvides() {
        return getChild(CasaProvides.class);
    }
    
    public void setProvides(CasaProvides casaProvides) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaProvides.class, PROVIDES_PROPERTY, casaProvides, empty);
    }
    
    // Convenience methods
    
    public String getEndpointName() {        
        CasaEndpointRef endpointRef = getConsumes();
        if (endpointRef == null) {
            endpointRef = getProvides();
        }
        return endpointRef.getEndpointName();
    }
}