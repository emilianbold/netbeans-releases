/*
 * SOAPFault.java
 *
 * Created on September 22, 2006, 7:19 PM
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
public interface SOAPFault extends ExtensibilityElement {
    
    public void setName( String name );
    
    public String getName();
    
    public void setUse( String use );
    
    public String getUse();
    
    public void setEncodingStyles( List<String> encodingStyles );
    
    public List<String> getEncodingStyles();
    
    public void setNamespaceURI( String namespaceURI );
    
    public String getNamespaceURI();
}
