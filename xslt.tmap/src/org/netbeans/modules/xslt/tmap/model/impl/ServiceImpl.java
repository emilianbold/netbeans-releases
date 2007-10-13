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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ServiceImpl extends TMapComponentContainerImpl 
    implements Service
{

    public ServiceImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.SERVICE, model));
    }

    public ServiceImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public List<Operation> getOperations() {
        return getChildren(Operation.class);
    }

    public void addOperation(Operation operation) {
        addAfter(TYPE.getTagName(), operation, TYPE.getChildTypes());
    }

    public int getSizeOfOperations() {
        List<Operation> operations = getChildren(Operation.class);
        return operations == null ? 0 : operations.size();
    }

    public void removeOperation(Operation operation) {
        removeChild(TYPE.getTagName(), operation);
    }

    public WSDLReference<PartnerLinkType> getPartnerLinkType() {
        return getWSDLReference(TMapAttributes.PARTNER_LINK_TYPE, PartnerLinkType.class);
    }

    public void setPartnerLinkType(WSDLReference<PartnerLinkType> pltRef) {
        setWSDLReference( TMapAttributes.PARTNER_LINK_TYPE, pltRef);
    }

    public WSDLReference<Role> getRole() {
        return getWSDLReference(TMapAttributes.ROLE_NAME, Role.class);
    }

    public void setRole(WSDLReference<Role> roleRef) {
        setWSDLReference( TMapAttributes.ROLE_NAME, roleRef);
    }

    public Reference[] getReferences() {
        return new Reference[] {getPartnerLinkType(), getRole()};
    }

    public Class<Service> getComponentType() {
        return Service.class;
    }
}
