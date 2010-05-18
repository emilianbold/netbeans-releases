/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
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
