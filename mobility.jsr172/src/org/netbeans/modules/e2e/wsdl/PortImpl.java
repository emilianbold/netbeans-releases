/*
 * PortImpl.java
 *
 * Created on September 24, 2006, 5:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public class PortImpl implements Port {
    
    private String name;
    private Binding binding;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of PortImpl */
    public PortImpl( String name, Binding binding ) {
        this.name = name;
        this.binding = binding;
        
        extensibilityElements = new ArrayList();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBinding( Binding binding ) {
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }

    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }
}
