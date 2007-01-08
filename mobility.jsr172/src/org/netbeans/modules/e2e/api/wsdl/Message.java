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

/**
 *
 * @author Michal Skvor
 */
public interface Message {
    
    public void setName( String name );
    
    public String getName();
    
    /**
     * 
     */ 
    public void addPart( Part part );
    
    /**
     * 
     */
    public Part getPart( String name );
    
    /**
     * 
     */ 
    public List<Part> getParts();
    
}
