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

import java.util.List;
import org.netbeans.modules.websvc.axis2.services.model.MessageReceivers;
import org.netbeans.modules.websvc.axis2.services.model.Operation;
import org.netbeans.modules.websvc.axis2.services.model.Parameter;
import org.netbeans.modules.websvc.axis2.services.model.Schema;
import org.netbeans.modules.websvc.axis2.services.model.Service;
import org.netbeans.modules.websvc.axis2.services.model.ServicesAttributes;
import org.netbeans.modules.websvc.axis2.services.model.ServicesQNames;
import org.netbeans.modules.websvc.axis2.services.model.ServicesVisitor;
import org.w3c.dom.Element;

public class ServiceImpl extends ServicesComponentImpl implements Service {
    
    public ServiceImpl(ServicesModelImpl model, Element e) {
        super(model, e);
    }
    
    public ServiceImpl(ServicesModelImpl model) {
        this(model, createElementNS(model, ServicesQNames.SERVICE));
    }
    
    public void accept(ServicesVisitor visitor) {
        visitor.visit(this);
    }

    public String getDescription() {
        return getChildElementText(ServicesQNames.DESCRIPTION.getQName());
    }

    public void setDescription(String descriptionsid) {
        setChildElementText(DESCRIPTION_PROP, descriptionsid, ServicesQNames.DESCRIPTION.getQName());
    }

    public List<Parameter> getParameters() {
        return super.getChildren(Parameter.class);
    }

    public void addParameter(Parameter param) {
        appendChild(PARAMETER_PROP, param);
    }

    public void removeParameter(Parameter param) {
        removeChild(PARAMETER_PROP, param);
    }

    public Schema getSchema() {
        return super.getChild(Schema.class);
    }

    public void setSchema(Schema schema) {
        appendChild(SCHEMA_PROP, schema);
    }
    
    public void removeSchema(Schema schema) {
        removeChild(SCHEMA_PROP, schema);
    }

    public List<Operation> getOperations() {
        return super.getChildren(Operation.class);
    }

    public void addOperation(Operation operation) {
        appendChild(OPERATION_PROP, operation);
    }

    public void removeOperation(Operation operation) {
        removeChild(OPERATION_PROP, operation);
    }

    public MessageReceivers geMessageReceivers() {
        return super.getChild(MessageReceivers.class);
    }

    public void setMessageReceivers(MessageReceivers receiverGroup) {
        appendChild(MESSAGE_RECEIVERS_PROP, receiverGroup);
    }

    public String getNameAttr() {
        return getAttribute(ServicesAttributes.attrName);
    }

    public void setNameAttr(String name) {
        super.setAttribute(NAME_ATTR_PROP, ServicesAttributes.attrName, name);
    }

    public String getScopeAttr() {
        return getAttribute(ServicesAttributes.attrScope);
    }

    public void setScopeAttr(String scope) {
        super.setAttribute(SCOPE_ATTR_PROP, ServicesAttributes.attrScope, scope);
    }

    public String getTargetNamespaceAttr() {
        return getAttribute(ServicesAttributes.attrTargetNamespace);
    }

    public void setTargetNamespaceAttr(String targetNamespace) {
        super.setAttribute(TARGET_NAMESPACE_ATTR_PROP, ServicesAttributes.attrTargetNamespace, targetNamespace);
    }    
}
