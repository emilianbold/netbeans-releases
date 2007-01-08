/*
 * SOAPBinding.java
 *
 * Created on September 22, 2006, 7:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl.extensions.soap;

import java.util.List;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public interface SOAPBinding extends ExtensibilityElement {
        
    public void setTransportURI( String transportURI );
    
    public String getTransportURI();
    
    public void setStyle( String style );
    
    public String getStyle();
    
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement );
        
    public List<ExtensibilityElement> getExtensibilityElements();    
}
