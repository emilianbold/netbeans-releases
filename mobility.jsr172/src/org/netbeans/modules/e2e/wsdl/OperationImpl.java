/*
 * OperationImpl.java
 *
 * Created on September 24, 2006, 5:35 PM
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
import org.netbeans.modules.e2e.api.wsdl.Fault;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public class OperationImpl implements Operation {
    
    private String name;
    private Output output;
    private Input input;
    private Map<String, Fault> faults;
    private String documentation;
    private String myJavaName;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of OperationImpl */
    public OperationImpl( String name ) {
        this.name = name;
        faults = new HashMap();
        extensibilityElements = new ArrayList();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getJavaName() {
        if ( myJavaName == null ){
            myJavaName = toJavaName( getName() );
        }
        return myJavaName;
    }
    
    public void setJavaName( String name ) {
        myJavaName = name;
    }

    public void setOutput( Output output ) {
        this.output = output;
    }

    public Output getOutput() {
        return output;
    }

    public void setInput( Input input ) {
        this.input = input;
    }

    public Input getInput() {
        return input;
    }

    public void addFault( Fault fault ) {
        faults.put( fault.getName(), fault );
    }

    public Fault getFault( String name ) {
        return faults.get( name );
    }

    public List<Fault> getFaults() {
        return Collections.unmodifiableList( new ArrayList( faults.values()));
    }
    
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }

    public void setDocumentation( String documentation ) {
        this.documentation = documentation;
    }

    public String getDocumentation() {
        return documentation;
    }
    
    public static String toJavaName( String name ){
        if( name.length() >1 ){
            return Character.toLowerCase(name.charAt( 0 ) ) +
                name.substring( 1 );
        }
        return name;
    }

}
