/*
 * Service.java
 *
 * Created on September 22, 2006, 6:37 PM
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
public interface Service {
    
    public void setName( String name );
    
    public String getName();
    
    public void addPort( Port port );
    
    public Port getPort( String name );
    
    public List<Port> getPorts();
}
