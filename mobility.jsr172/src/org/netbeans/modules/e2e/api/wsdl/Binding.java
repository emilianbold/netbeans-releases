/*
 * Binding.java
 *
 * Created on September 22, 2006, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl;

import java.util.List;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public interface Binding {
    
    public void setName( String name );
    
    public String getName();
    
    public void setPortType( PortType portType );
    
    public PortType getPortType();
    
    public void addBindingOperation( BindingOperation bindingOperation );
    
    public BindingOperation getBindingOperation( String name );
    
    public List<BindingOperation> getBindingOperations();
    
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement );
        
    public List<ExtensibilityElement> getExtensibilityElements();    
}
