/*
 * ServiceImpl.java
 *
 * Created on September 24, 2006, 5:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.Service;

/**
 *
 * @author Michal Skvor
 */
public class ServiceImpl implements Service {
    
    private String name;
    private Map<String, Port> ports;
    
    /** Creates a new instance of ServiceImpl */
    public ServiceImpl( String name ) {
        this.name = name;
        ports = new HashMap();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addPort( Port port ) {
        ports.put( port.getName(), port );
    }

    public Port getPort( String name ) {
        return ports.get( name );
    }

    public List<Port> getPorts() {
        return Collections.unmodifiableList( new ArrayList( ports.values()));
    }
    
}
