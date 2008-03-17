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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.axis2.services.model.impl;

import org.netbeans.modules.websvc.axis2.services.model.MessageReceiver;
import org.netbeans.modules.websvc.axis2.services.model.MessageReceivers;
import org.netbeans.modules.websvc.axis2.services.model.Operation;
import org.netbeans.modules.websvc.axis2.services.model.Parameter;
import org.netbeans.modules.websvc.axis2.services.model.Schema;
import org.netbeans.modules.websvc.axis2.services.model.Service;
import org.netbeans.modules.websvc.axis2.services.model.ServiceGroup;
import org.netbeans.modules.websvc.axis2.services.model.Services;
import org.netbeans.modules.websvc.axis2.services.model.ServicesComponent;
import org.netbeans.modules.websvc.axis2.services.model.ServicesComponentFactory;
import org.netbeans.modules.websvc.axis2.services.model.ServicesQNames;
import org.netbeans.modules.websvc.axis2.services.model.ServicesVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

public class ServicesComponentFactoryImpl implements ServicesComponentFactory {
    private ServicesModelImpl model;
    
    public ServicesComponentFactoryImpl(ServicesModelImpl model) {
        this.model = model;
    }
    
    public ServicesComponent create(Element element, ServicesComponent context) {
        if (context == null) {
            if (areSameQName(ServicesQNames.SERVICE, element)) {
                model.setServicesGroup(false);
                return new ServiceImpl(model, element);
            } else if (areSameQName(ServicesQNames.SERVICE_GROUP, element)) {
                model.setServicesGroup(true);
                return new ServiceGroupImpl(model, element);
            } else {
                return null;
            }
        } else {
            return new CreateVisitor().create(element, context);
        }
    }
    
    public static boolean areSameQName(ServicesQNames q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
    
    public static class CreateVisitor extends ServicesVisitor.Default {
        Element element;
        ServicesComponent created;
        
        ServicesComponent create(Element element, ServicesComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(ServicesQNames q) {
            return areSameQName(q, element);
        }
        
        @Override
        public void visit(Services context) {
            if (isElementQName(ServicesQNames.SERVICE)) {
                created = new ServiceImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.SERVICE_GROUP)) {
                created = new ServiceGroupImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.SCHEMA)) {
                created = new SchemaImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.PARAMETER)) {
                created = new ParameterImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.OPERATION)) {
                created = new OperationImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.MESSAGE_RECEIVERS)) {
                created = new MessageReceiversImpl((ServicesModelImpl)context.getModel(), element);
            }
            if (isElementQName(ServicesQNames.MESSAGE_RECEIVER)) {
                created = new MessageReceiverImpl((ServicesModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(MessageReceivers context) {
            if (isElementQName(ServicesQNames.MESSAGE_RECEIVER)) {
                created = new MessageReceiverImpl((ServicesModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(Operation context) {
            if (isElementQName(ServicesQNames.MESSAGE_RECEIVER)) {
                created = new MessageReceiverImpl((ServicesModelImpl)context.getModel(), element);
            }
        }
        
    }

    
    public Service createService() {
        return new ServiceImpl(model);
    }
    
    public ServiceGroup createServiceGroup() {
        return new ServiceGroupImpl(model);
    }

    public Parameter createParameter() {
        return new ParameterImpl(model);
    }
    
    public Schema createSchema() {
        return new SchemaImpl(model);
    }

    public MessageReceivers createMessageReceivers() {
        return new MessageReceiversImpl(model);
    }

    public MessageReceiver createMessageReceiver() {
        return new MessageReceiverImpl(model);
    }
}
