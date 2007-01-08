/*
 * SOAPHeaderFault.java
 *
 * Created on September 22, 2006, 7:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl.extensions.soap;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public interface SOAPHeaderFault extends ExtensibilityElement {
    
    public void setMessage( QName message );
    
    public QName getMessage();
    
    public void setPart( String part );
    
    public String getPart();
    
    public void setUse( String use );
    
    public String getUse();
    
    public void setEncodingStyles( List<String> encodingStyles );
    
    public List<String> getEncodingStyles();
    
    public void setNamespaceURI( String namespaceURI );
    
    public String getNamespaceURI();
}
