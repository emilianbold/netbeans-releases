/*
 * BindingOperation.java
 *
 * Created on September 22, 2006, 6:52 PM
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
public interface BindingOperation {
    
    public void setName( String name );
    
    public String getName();
    
    public void setBindingInput( BindingInput bindingInput );
    
    public BindingInput getBindingInput();
    
    public void setBindingOutput( BindingOutput bindingOutput );
    
    public BindingOutput getBindingOutput();
    
    public void addBindingFault( BindingFault bindingFault );
    
    public BindingFault getBindingFault( String name );
    
    public List<BindingFault> getBindingFaults();
    
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement );
        
    public List<ExtensibilityElement> getExtensibilityElements();        
}
