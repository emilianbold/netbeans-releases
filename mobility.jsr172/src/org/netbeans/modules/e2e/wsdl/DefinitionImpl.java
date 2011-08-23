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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.e2e.api.schema.SchemaHolder;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;

/**
 *
 * @author Michal Skvor
 */
public class DefinitionImpl implements Definition {
    
    private SchemaHolder schemaHolder;
    
    private Map<String, Binding> bindings = new HashMap<String, Binding>();
    private Map<QName, Message> messages = new HashMap<QName, Message>();
    private Map<String, Service> services = new HashMap<String, Service>();
    private Map<QName, PortType> portTypes = new HashMap<QName, PortType>();
    
    private String documentation;
    
    private String targetNamespace;
    
    /** Creates a new instance of DefinitionImpl */
    public DefinitionImpl() {
    }

    @Override
    public void setSchemaHolder(SchemaHolder schemaHolder) {
        this.schemaHolder = schemaHolder;
    }

    @Override
    public SchemaHolder getSchemaHolder() {
        return schemaHolder;
    }

    @Override
    public void addBinding(Binding binding) {
        bindings.put(binding.getName(), binding);
    }

    @Override
    public Binding getBinding(String name) {
        return bindings.get(name);
    }

    @Override
    public Map<String, Binding> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    @Override
    public void addMessage(Message message) {
        messages.put(message.getQName(), message);
    }

    @Override
    public Message getMessage(QName name) {
        return messages.get(name);
    }

    @Override
    public Map<QName, Message> getMessages() {
        return Collections.unmodifiableMap(messages);
    }

    @Override
    public void addService(Service service) {
        services.put(service.getName(), service);
    }

    @Override
    public Service getService(String name) {
        return services.get(name);
    }

    @Override
    public Map<String, Service> getServices() {
        return Collections.unmodifiableMap(services);
    }

    @Override
    public void addPortType(PortType portType) {
        portTypes.put(portType.getQName(), portType);
    }

    @Override
    public PortType getPortType(QName name) {
        return portTypes.get(name);
    }

    @Override
    public Map<QName, PortType> getPortTypes() {
        return Collections.unmodifiableMap(portTypes);
    }

    @Override
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String getDocumentation() {
        return documentation;
    }

    @Override
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @Override
    public String getTargetNamespace() {
        return targetNamespace;
    }

}
