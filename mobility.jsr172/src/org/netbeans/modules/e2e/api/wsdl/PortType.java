/*
 * PortType.java
 *
 * Created on September 22, 2006, 6:43 PM
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
public interface PortType {
    
    public void setName( String name );
    
    public String getName();
    
    public void addOperation( Operation operation );
    
    public void getOperation( String name );
    
    public List<Operation> getOperations();
}
