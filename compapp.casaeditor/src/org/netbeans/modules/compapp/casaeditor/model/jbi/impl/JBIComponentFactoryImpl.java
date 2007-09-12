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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBI;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponentFactory;
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
