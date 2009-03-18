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

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface PortType {
    
    
    /**
     * 
     * This interface represents reference to PortType 
     * ( not PortType itself actually but there was no 
     * simple way to realize reference in current
     * impl ) from some wsdl entity ( f.e. binding references to
     * PortType ).  
     * @author ads
     *
     */
    public interface PortTypeReference extends PortType {

        boolean isValid();
    }
    
    public String getName();
    
    public void addOperation( Operation operation );
    
    public List<Operation> getOperations();
    
    QName getQName();
}
