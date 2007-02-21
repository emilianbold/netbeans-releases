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

import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegions;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaRegionImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaRegionsImpl;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connection;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Connections;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumer;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provider;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceAssembly;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Services;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPortTypes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPorts;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServices;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaBindingsImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaEndpointsImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaLinkImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaPortImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaPortTypesImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaPortsImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaServicesImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class JBIComponentFactoryImpl implements JBIComponentFactory {
    
    private JBIModel model;
    
    /**
     * Creates a new instance of JBIComponentFactoryImpl
     */
    public JBIComponentFactoryImpl(JBIModel model) {
        this.model = model;
    }
    
    public JBIComponent create(Element element, JBIComponent context) {
        return new JBICreateVisitor().create(element, context);
        
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
    
    public JBI createJBI() {
        return new JBIImpl(model);
    }
    
    public Services createServices() {
        return new ServicesImpl(model);
    }
    
    public Consumes createConsumes() {
        return new ConsumesImpl(model);
    }
    
    public Provides createProvides() {
        return new ProvidesImpl(model);
    }
    
    public ServiceAssembly createServiceAssembly() {
        return new ServiceAssemblyImpl(model);
    }
    
    public ServiceUnit createServiceUnit() {
        return new ServiceUnitImpl(model);
    }
    
    public Connections createConnections() {
        return new ConnectionsImpl(model);
    }
    
    public Connection createConnection() {
        return new ConnectionImpl(model);
    }
    
    public Consumer createConsumer() {
        return new ConsumerImpl(model);
    }
    
    public Provider createProvider() {
        return new ProviderImpl(model);
    }
    
    public Identification createIdentification() {
        return new IdentificationImpl(model);
    }
    
    public Target createTarget() {
        return new TargetImpl(model);
    }
              
    public static class JBICreateVisitor extends JBIVisitor.Default {
        Element element;
        JBIComponent created;
        
        JBIComponent create(Element element, JBIComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(JBIQNames q) {
            return areSameQName(q, element);
        }
        
        public static boolean areSameQName(JBIQNames q, Element e) {
            return q.getQName().equals(AbstractDocumentComponent.getQName(e));
        }
        
        public void visit(JBI context) {
            if (isElementQName(JBIQNames.SERVICES)) {
                created = new ServicesImpl(context.getModel(), element);
            } else if (isElementQName(JBIQNames.SERVICE_ASSEMBLY)) {
                created = new ServiceAssemblyImpl(context.getModel(), element);
            }
        }
        
        public void visit(Services context) {
            if (isElementQName(JBIQNames.CONSUMES)) {
                created = new ConsumesImpl(context.getModel(), element);
            } else if (isElementQName(JBIQNames.PROVIDES)) {
                created = new ProvidesImpl(context.getModel(), element);
            }
        }
        
        public void visit(ServiceAssembly context) {
            if (isElementQName(JBIQNames.SERVICE_UNIT)) {
                created = new ServiceUnitImpl(context.getModel(), element);
            } else if (isElementQName(JBIQNames.CONNECTIONS)) {
                created = new ConnectionsImpl(context.getModel(), element);
            } else if (isElementQName(JBIQNames.IDENTIFICATION)) {
                created = new IdentificationImpl(context.getModel(), element);
            }
        }
        
        public void visit(ServiceUnit context) {
            JBIModel model = context.getModel();
            if (isElementQName(JBIQNames.IDENTIFICATION)) {
                created = new IdentificationImpl(model, element);
            } else if (isElementQName(JBIQNames.TARGET)) {
                created = new TargetImpl(model, element);
            }
        }
        
        public void visit(Connections context) {
            if (isElementQName(JBIQNames.CONNECTION)) {
                created = new ConnectionImpl(context.getModel(), element);
            }
        }
        
        public void visit(Connection context) {
            if (isElementQName(JBIQNames.CONSUMER)) {
                created = new ConsumerImpl(context.getModel(), element);
            } else if (isElementQName(JBIQNames.PROVIDER)) {
                created = new ProviderImpl(context.getModel(), element);
            }
        }
        
        public void visit(ExtensibilityElement context) {
            ;
        }
    }
}
