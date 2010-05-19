/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import java.util.List;
import org.netbeans.modules.wlm.model.api.EmailAddress;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMComponentFactory;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMVisitor;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.wlm.model.spi.OperationReference;
import org.netbeans.modules.wlm.model.spi.PortTypeReference;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.w3c.dom.Element;

/**
 *
 * @author anjeleevich
 */
public class EmailImpl extends WLMComponentBase implements TEmail {

    WSDLReference<PortType> portType;
            
    public EmailImpl(WLMModel model, Element e) {
        super(model, e);
    }

    public EmailImpl(WLMModel model) {
        this(model, createNewElement(WLMQNames.EMAIL.getQName(), model));
    }

    public void accept(WLMVisitor visitor) {
        visitor.visitEmail(this);
    }

    public WLMComponent createChild(Element childElement) {
        WLMComponent child = null;
        if (childElement != null) {
            String localName = childElement.getLocalName();
            if (localName == null || localName.length() == 0) {
                localName = childElement.getTagName();
            }

            if (ADDRESS_ELEMENT_NAME.equals(localName)) {
                child = new EmailAddressImpl(getModel(), childElement);
            }
        }
        return child;
    }

    public List<EmailAddress> getAddresses() {
        return getChildren(EmailAddress.class);
    }

    public void addAddress(EmailAddress address) {
        addAfter(ADDRESS_ELEMENT_NAME, address, EMAIL_POSITION);
    }

    public void removeAddress(EmailAddress address) {
        removeChild(ADDRESS_ELEMENT_NAME, address);
    }

    public boolean hasAddresses() {
        return (getChild(EmailAddress.class) != null);
    }

    public WSDLReference<PortType> getPortType() {
        String ptStr = getPortTypeAsString();
        if (ptStr == null || ptStr.length() == 0) {
            return null;
        }
        //
        PortTypeReference ptRef = new PortTypeReference(this, ptStr);
        return ptRef;
    }

    public String getPortTypeAsString() {
        String ptStr = getAttribute(WLMAttribute.PORT_TYPE);
        return ptStr;
    }

//    public void setPortType(WSDLReference<PortType> portType) {
//        String refStr = portType.getRefString();
//        setAttribute(PORT_TYPE_PROPERTY, WLMAttribute.PORT_TYPE, refStr);
//    }

    public WSDLReference<Operation> getOperation() {
        String ptStr = getPortTypeAsString();
        if (ptStr == null || ptStr.length() == 0) {
            return null;
        }
        //
        String oprStr = getOperationAsString();
        if (oprStr == null || oprStr.length() == 0) {
            return null;
        }
        OperationReference optRef = new OperationReference(this, oprStr);
        return optRef;
    }

    public String getOperationAsString() {
        String optStr = getAttribute(WLMAttribute.OPERATION);
        return optStr;
    }

    public void setOperation(WSDLReference<Operation> ref) {
        Operation opt = ref.get();
        if (opt == null) {
            return;
        }
        //
        WSDLComponent parent = opt.getParent();
        assert parent instanceof PortType;
        if (parent instanceof PortType) {
            setPortType(createPortTypeReference((PortType) parent));
        }
        //
        String val = opt.getName(); //ref.getRefString();
        setAttribute(OPERATION_PROPERTY, WLMAttribute.OPERATION, val);
        //
        // Add import
        if (!getOperation().isResolved()) {
            WLMComponentFactory factory = getModel().getFactory();
            TImport importEl = factory.createImport(getModel());
            importEl.setWSDL(opt.getModel());
            getModel().getTask().addImport(importEl);
        }
    }

    public void setOperation(WSDLReference<Operation> ref, String wsdlCatalogId) {
        Operation opt = ref.get();
        if (opt == null) {
            return;
        }
        //
        WSDLComponent parent = opt.getParent();
        assert parent instanceof PortType;
        if (parent instanceof PortType) {
            setPortType(createPortTypeReference((PortType) parent));
        }
        //
        String val = opt.getName(); //ref.getRefString();
        setAttribute(OPERATION_PROPERTY, WLMAttribute.OPERATION, val);
        //
        // Add import
        if (!getOperation().isResolved()) {
            WLMComponentFactory factory = getModel().getFactory();
            TImport importEl = factory.createImport(getModel());
            importEl.setWSDL(opt.getModel(), wsdlCatalogId);
            getModel().getTask().addImport(importEl);
        }
    }

    public void setPortType(WSDLReference<PortType> portTypeRef) {
        String refStr = portTypeRef.getRefString();
        setAttribute(PORT_TYPE_PROPERTY, WLMAttribute.PORT_TYPE, refStr);
    }
    
    public OperationReference createOperationReference(Operation referenced) {
        OperationReference ref = new OperationReference(referenced, this);
        return ref;
    }

    public PortTypeReference createPortTypeReference(PortType referenced) {
        PortTypeReference ref = new PortTypeReference(referenced, this);
        return ref;
    }
    
    private static final ElementPosition EMAIL_POSITION 
            = new ElementPosition(EmailAddress.class);
}
