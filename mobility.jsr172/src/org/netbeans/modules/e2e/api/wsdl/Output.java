/*
 * Ouput.java
 *
 * Created on September 22, 2006, 6:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl;

/**
 *
 * @author Michal Skvor
 */
public interface Output {
    
    public void setName( String name );
    
    public String getName();
    
    public void setMessage( Message message );
    
    public Message getMessage();
}
