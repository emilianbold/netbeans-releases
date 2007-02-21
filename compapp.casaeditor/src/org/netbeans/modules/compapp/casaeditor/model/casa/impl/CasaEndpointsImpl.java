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
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaEndpointsImpl extends CasaComponentImpl implements CasaEndpoints {

    public CasaEndpointsImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaEndpointsImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.ENDPOINTS)); 
    }

    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
//        
//    public void addConsumes(int index, CasaConsumes casaConsumes) {
//        insertAtIndex(CONSUMES_PROPERTY, casaConsumes, index, CasaConsumes.class);
//    }
//
//    public void removeConsumes(CasaConsumes casaConsumes) {
//        removeChild(CONSUMES_PROPERTY, casaConsumes);
//    }
//
//    public List<CasaConsumes> getConsumes() {
//         return getChildren(CasaConsumes.class);
//    }
//           
//    public void addProvides(int index, CasaProvides casaProvides) {
//        insertAtIndex(PROVIDES_PROPERTY, casaProvides, index, CasaProvides.class);
//    }
//
//    public void removeProvides(CasaProvides casaProvides) {
//        removeChild(PROVIDES_PROPERTY, casaProvides);
//    }
//
//    public List<CasaProvides> getProvides() {
//         return getChildren(CasaProvides.class);
//    }
//    
//    // Convenience methods
    
    public List<CasaEndpoint> getEndpoints() {
        return getChildren(CasaEndpoint.class);
    }

    public void addEndpoint(int index, CasaEndpoint casaEndpoint) {
         insertAtIndex(ENDPOINT_PROPERTY, casaEndpoint, index, CasaEndpoint.class);
    }

    public void removeEndpoint(CasaEndpoint casaEndpoint) {
        removeChild(ENDPOINT_PROPERTY, casaEndpoint);
    }
}
