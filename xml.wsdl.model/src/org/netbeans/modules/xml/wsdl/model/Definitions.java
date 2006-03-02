/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
