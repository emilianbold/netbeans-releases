/*
 * SOAPFaultImpl.java
 *
 * Created on September 27, 2006, 3:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPFault;

/**
 *
 * @author Michal Skvor
 */
public class SOAPFaultImpl implements SOAPFault {
    
    private String name;
    private String use;
    private List<String> encodingStyles;
    private String namespaceURI;
    
    private QName type = SOAPConstants.FAULT;
    
    /** Creates a new instance of SOAPFaultImpl */
    public SOAPFaultImpl( String name, String use ) {
        this.name = name;
        this.use = use;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUse( String use ) {
        this.use = use;
    }

    public String getUse() {
        return use;
    }

    public void setEncodingStyles( List<String> encodingStyles ) {
        this.encodingStyles = encodingStyles;
    }

    public List<String> getEncodingStyles() {
        return Collections.unmodifiableList( encodingStyles );
    }

    public void setNamespaceURI( String namespaceURI ) {
        this.namespaceURI = namespaceURI;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void setElementType( QName type ) {
        this.type = type;
    }

    public QName getElementType() {
        return type;
    }
}
