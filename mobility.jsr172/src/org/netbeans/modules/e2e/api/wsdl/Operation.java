/*
 * Operation.java
 *
 * Created on September 22, 2006, 6:45 PM
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
public interface Operation {
    
    public void setName( String name );
    
    public String getName();
    
    public String getJavaName();
    
    public void setJavaName(String name);
    
    public void setOutput( Output output );
    
    public Output getOutput();
    
    public void setInput( Input input );
    
    public Input getInput();
    
    public void addFault( Fault fault );
    
    public Fault getFault( String name );
    
    public List<Fault> getFaults();
        
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement );
        
    public List<ExtensibilityElement> getExtensibilityElements();
 
    public void setDocumentation( String documentation );
    
    public String getDocumentation();
}