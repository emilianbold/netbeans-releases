/*
 * BindingOperationImpl.java
 *
 * Created on September 24, 2006, 5:27 PM
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
import org.netbeans.modules.e2e.api.wsdl.BindingFault;
import org.netbeans.modules.e2e.api.wsdl.BindingInput;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.BindingOutput;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public class BindingOperationImpl implements BindingOperation {
    
    private String name;
    private BindingInput bindingInput;
    private BindingOutput bindingOutput;
    private Map<String, BindingFault> bindingFaults;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of BindingOperationImpl */
    public BindingOperationImpl( String name ) {
        this.name = name;
        bindingFaults = new HashMap();
        extensibilityElements = new ArrayList();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBindingInput( BindingInput bindingInput ) {
        this.bindingInput = bindingInput;
    }

    public BindingInput getBindingInput() {
        return bindingInput;
    }

    public void setBindingOutput( BindingOutput bindingOutput ) {
        this.bindingOutput = bindingOutput;
    }

    public BindingOutput getBindingOutput() {
        return bindingOutput;
    }

    public void addBindingFault( BindingFault bindingFault ) {
        bindingFaults.put( bindingFault.getName(), bindingFault );
    }

    public BindingFault getBindingFault( String name ) {
        return bindingFaults.get( name );
    }

    public List<BindingFault> getBindingFaults() {
        return Collections.unmodifiableList( new ArrayList( bindingFaults.values()));
    }
    
    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }    
}
