/*
 * Message.java
 *
 * Created on September 22, 2006, 1:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl;

import java.util.List;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface Message {
    
    /**
     * 
     * This interface represents reference to Message 
     * ( not Message itself actually but there was no 
     * simple way to realize reference in current
     * impl ) from some wsdl entity ( f.e. input or output 
     * reference to Message ).  
     * @author ads
     *
     */
    public interface MessageReference extends Message {
        
        boolean isValid();
    }
    
    public String getName();
    
    QName getQName();
    
    public void addPart( Part part );
    
    public Part getPart( String name );
      
    public List<Part> getParts();
    
}
