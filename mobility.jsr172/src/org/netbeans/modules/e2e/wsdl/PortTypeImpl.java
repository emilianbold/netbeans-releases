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
        operations = new HashMap<String,Operation>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addOperation(Operation operation) {
        Operation op = operations.get(operation.getJavaName());
        if ( op != null ){
            String name = operation.getName();
            if ( name != null && name.length() != 0 ){
                String newJavaName = generateJavaName( operation );
                operation.setJavaName( newJavaName );
            }
        }
        op = operations.get( getUpperCaseName(operation));
        if ( op != null ){
            String name = op.getName();
            if ( name != null && name.length() != 0 ){
                String newJavaName = generateJavaName( op );
                op.setJavaName( newJavaName );
            }
        }
        operations.put( operation.getName(), operation );
    }
    
    private String generateJavaName( Operation op ) {
        String name = op.getName();
        int count = 1;
        String newName = name+count;
        while ( operations.containsKey(OperationImpl.toJavaName( newName) )){
            count++;
            newName = name +count;
        }
        return OperationImpl.toJavaName( newName );
    }

    public void getOperation(String name) {
        throw new UnsupportedOperationException( "Not implemented." );
    }

    public List<Operation> getOperations() {
        return Collections.unmodifiableList( new ArrayList( operations.values()));
    }
    
    public String getUpperCaseName( Operation op) {
        String name = op.getName();
        if( name.length() >1 ){
            return Character.toUpperCase(name.charAt( 0 ) ) +
                name.substring( 1 );
        }
        return name;
    }
    
}
