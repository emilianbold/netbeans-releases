/*
 * BindingImpl.java
 *
 * Created on September 24, 2006, 4:59 PM
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
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public class BindingImpl implements Binding {
    
    private String name;
    private PortType portType;
    private Map<String, BindingOperation> bindingOperations;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of BindingImpl */
    public BindingImpl( String name ) {
        this.name = name;
        bindingOperations = new HashMap();
        extensibilityElements = new ArrayList();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPortType( PortType portType ) {
        this.portType = portType;
    }

    public PortType getPortType() {
        return portType;
    }
        
    public void addBindingOperation( BindingOperation bindingOperation ) {
        bindingOperations.put( bindingOperation.getName(), bindingOperation );
    }

    public BindingOperation getBindingOperation( String name ) {
        return bindingOperations.get( name );
    }

    public List<BindingOperation> getBindingOperations() {
        return Collections.unmodifiableList( new ArrayList( bindingOperations.values()));
    }

    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }    
}
