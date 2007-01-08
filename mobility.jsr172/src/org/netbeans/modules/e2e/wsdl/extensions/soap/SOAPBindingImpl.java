/*
 * SOAPBindingImpl.java
 *
 * Created on September 27, 2006, 2:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;

/**
 *
 * @author Michal Skvor
 */
public class SOAPBindingImpl implements SOAPBinding {
    
    private String transportURI;
    private String style;
    
    private List<ExtensibilityElement> extensibilityElements;    
    
    private QName type = SOAPConstants.BINDING;
    
    /** Creates a new instance of SOAPBindingImpl */
    public SOAPBindingImpl( String transportURI, String style ) {
        this.transportURI = transportURI;
        this.style = style;
    }

    public void setTransportURI( String transportURI ) {
        this.transportURI = transportURI;
    }

    public String getTransportURI() {
        return transportURI;
    }

    public void setStyle( String style ) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }

    public void setElementType( QName type ) {
        this.type = type;
    }

    public QName getElementType() {
        return type;
    }
    
}
