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

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLinksContainer;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegions;
import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindingComponentServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnections;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPortTypes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPorts;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnits;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServices;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaComponentFactoryImpl implements CasaComponentFactory {
    
    private CasaModel model;
    
    /**
     * Creates a new instance of CasaComponentFactoryImpl
     */
    public CasaComponentFactoryImpl(CasaModel model) {
        this.model = model;
    }
    
    public CasaComponent create(Element element, CasaComponent context) {
        return new CasaCreateVisitor().create(element, context);
        
        /*
        QName qname = Util.getQName(element, (JBIComponentImpl)context);
        String nsURI = qname.getNamespaceURI();
        if (nsURI.equals(JBIQNames.JBI_NS_URI)) {
            return new JBICreateVisitor().create(element, context);
        } else if (nsURI.equals(CasaQName.CASA_NS_URI)) {
            return new CasaCreateVisitor().create(element, context);
        } else {
            throw new RuntimeException("Unknown namespace: " + nsURI);
        }
        */
    }
    
    public Casa createCasa() {
        return new CasaImpl(model);
    }
    
    public CasaServiceUnits createCasaServiceUnits() {
        return new CasaServiceUnitsImpl(model);
    }
    
    public CasaServiceEngineServiceUnit createCasaEngineServiceUnit() {
        return new CasaServiceEngineServiceUnitImpl(model);
    }
    
    public CasaBindingComponentServiceUnit createCasaBindingServiceUnit() {
        return new CasaBindingComponentServiceUnitImpl(model);
    }
    
    public CasaConnections createCasaConnections() {
        return new CasaConnectionsImpl(model);
    }
    
    public CasaConnection createCasaConnection() {
        return new CasaConnectionImpl(model);
    }
        
    public CasaEndpoints createCasaEndpoints() {
        return new CasaEndpointsImpl(model);
    }
    
    public CasaEndpoint createCasaEndpoint() {
        return new CasaEndpointImpl(model);
    }
        
    public CasaConsumes createCasaConsumes() {
        return new CasaConsumesImpl(model);
    }
    
    public CasaProvides createCasaProvides() {
        return new CasaProvidesImpl(model);
    }
    
    public CasaPorts createCasaPorts() {
        return new CasaPortsImpl(model);
    }
    
    public CasaPort createCasaPort() {
        return new CasaPortImpl(model);
    }
    
    public CasaPortTypes createCasaPortTypes() {
        return new CasaPortTypesImpl(model);
    }
    
    public CasaBindings createCasaBindings() {
        return new CasaBindingsImpl(model);
    }
    
    public CasaServices createCasaServices() {
        return new CasaServicesImpl(model);
    }
    
    public CasaLink createCasaLink() {
        return new CasaLinkImpl(model);
    }
        
    public static class CasaCreateVisitor extends CasaComponentVisitor.Default {
        Element element;
        CasaComponent created;
        
        CasaComponent create(Element element, CasaComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(CasaQName q) {
            return areSameQName(q, element);
        }
        
        public static boolean areSameQName(CasaQName q, Element e) {
            return q.getQName().equals(AbstractDocumentComponent.getQName(e));
        }
        
        @Override
        public void visit(Casa context) {
            if (isElementQName(CasaQName.SERVICE_UNITS)) {
                created = new CasaServiceUnitsImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.CONNECTIONS)) {
                created = new CasaConnectionsImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.PORTTYPES)) {
                created = new CasaPortTypesImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.BINDINGS)) {
                created = new CasaBindingsImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.SERVICES)) {
                created = new CasaServicesImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.REGIONS)) {
                created = new CasaRegionsImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.ENDPOINTS)) {
                created = new CasaEndpointsImpl(context.getModel(), element);
            }
        }
         
        @Override
        public void visit(CasaServiceUnits context) {
            if (isElementQName(CasaQName.ENGINE_ENGINE_SERVICE_UNIT)) {
                created = new CasaServiceEngineServiceUnitImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.BINDING_COMPONENT_SERVICE_UNIT)) {
                created = new CasaBindingComponentServiceUnitImpl(context.getModel(), element);
            }
        }
        
        @Override
        public void visit(CasaConnections context) {
            if (isElementQName(CasaQName.CONNECTION)) {
                created = new CasaConnectionImpl(context.getModel(), element);
            }
        }
          
        @Override
        public void visit(CasaServiceEngineServiceUnit context) {
            if (isElementQName(CasaQName.PROVIDES)) {
                created = new CasaProvidesImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.CONSUMES)) {
                created = new CasaConsumesImpl(context.getModel(), element);
            }
        }
        
        @Override
        public void visit(CasaBindingComponentServiceUnit context) {
            if (isElementQName(CasaQName.PORTS)) {
                created = new CasaPortsImpl(context.getModel(), element);
            } 
        }
        
        @Override
        public void visit(CasaPorts context) {
            if (isElementQName(CasaQName.PORT)) {
                created = new CasaPortImpl(context.getModel(), element);
            }
        }
        
        @Override
        public void visit(CasaPort context) {
            if (isElementQName(CasaQName.LINK)) {
                created = new CasaLinkImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.PROVIDES)) {
                created = new CasaProvidesImpl(context.getModel(), element);
            } else if (isElementQName(CasaQName.CONSUMES)) {
                created = new CasaConsumesImpl(context.getModel(), element);
            } else {
                created = new CasaGenericExtensibilityElementImpl(context.getModel(), element);
            }
        }
        
        @Override
        public void visit(CasaEndpoints context) {
            if (isElementQName(CasaQName.ENDPOINT)) {
                created = new CasaEndpointImpl(context.getModel(), element);
            } 
        }
        
        @Override
        public void visit(CasaRegions context) {
            if (isElementQName(CasaQName.REGION)) {
                created = new CasaRegionImpl(context.getModel(), element);
            }
        }
        
        @Override
        public void visit(CasaConnection context) {
            created = new CasaGenericExtensibilityElementImpl(context.getModel(), element);
        }
        
        @Override
        public void visit(CasaConsumes context) {
            created = new CasaGenericExtensibilityElementImpl(context.getModel(), element);
        }
         
        @Override
        public void visit(CasaProvides context) {
            created = new CasaGenericExtensibilityElementImpl(context.getModel(), element);
        }
        
        @Override
        public void visit(CasaExtensibilityElement context) {
            created = new CasaGenericExtensibilityElementImpl(context.getModel(), element);
        }
         
        @Override
        public void visit(CasaPortTypes context) {
            visit((CasaLinksContainer) context);
        }
        
        @Override
        public void visit(CasaBindings context) {
            visit((CasaLinksContainer) context);
        }
        
        @Override
        public void visit(CasaServices context) {
            visit((CasaLinksContainer) context);
        }
        
        @Override
        public void visit(CasaLink context) {
        }
        
        @Override
        public void visit(CasaRegion context) {
        }        
        
        private void visit(CasaLinksContainer context) {
            if (isElementQName(CasaQName.LINK)) {
                created = new CasaLinkImpl(context.getModel(), element);
            }
        }
    }
}
