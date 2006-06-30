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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author rico
 * Represents the WSDL definitions section
 */
public interface Definitions extends Named<WSDLComponent>, WSDLComponent {
    public static String IMPORT_PROPERTY = "import";
    public static String BINDING_PROPERTY = "binding";
    public static String TYPES_PROPERTY = "types";
    public static String MESSAGE_PROPERTY = "message";
    public static String PORT_TYPE_PROPERTY = "portType";
    public static String SERVICE_PROPERTY = "service";
    public static String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
    
    void addImport(Import importDefinition);
    void removeImport(Import importDefinition);
    Collection<Import> getImports();
    
    void setTypes(Types types);
    Types getTypes();
    
    void addMessage(Message message);
    void removeMessage(Message message);
    Collection<Message> getMessages();
    
    void addPortType(PortType portType);
    void removePortType(PortType portType);
    Collection<PortType> getPortTypes();
    
    void addBinding(Binding binding);
    void removeBinding(Binding binding);
    Collection<Binding> getBindings();
    
    void addService(Service service);
    void removeService(Service service);
    Collection<Service> getServices();
    
     String getTargetNamespace();
    void setTargetNamespace(String uri);
}
