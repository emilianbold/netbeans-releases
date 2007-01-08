/*
 * PortTypeImpl.java
 *
 * Created on September 24, 2006, 5:41 PM
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
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.PortType;

/**
 *
 * @author Michal Skvor
 */
public class PortTypeImpl implements PortType {
    
    private String name;
    private Map<String, Operation> operations;
    
    /** Creates a new instance of PortTypeImpl */
    public PortTypeImpl( String name ) {
        this.name = name;
        operations = new HashMap();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addOperation(Operation operation) {
        operations.put( operation.getName(), operation );
    }

    public void getOperation(String name) {
        throw new UnsupportedOperationException( "Not implemented." );
    }

    public List<Operation> getOperations() {
        return Collections.unmodifiableList( new ArrayList( operations.values()));
    }
    
}
