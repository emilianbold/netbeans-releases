/*
 * Definition.java
 *
 * Created on September 24, 2006, 5:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl;

import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.schema.SchemaHolder;

/**
 *
 * @author Michal Skvor
 */
public interface Definition {
    
    public void setSchemaHolder( SchemaHolder schemaHolder );
    
    public SchemaHolder getSchemaHolder();
    
    public void addBinding( Binding binding );
    
    public Binding getBinding( String name );
    
    public Map<String, Binding> getBindings();
    
    public void addMessage( Message message );
    
    public Message getMessage( String name );
    
    public Map<String, Message> getMessages();
    
    public void addService( Service service );
    
    public Service getService( String name );
    
    public Map<String, Service> getServices();
    
    public void addPortType( PortType portType );
    
    public PortType getPortType( String name );
    
    public Map<String, PortType> getPortTypes();
    
    public void setDocumentation( String documentation );
    
    public String getDocumentation();
    
    public void setTargetNamespace( String targetNamespace );
    
    public String getTargetNamespace();
}
