/*
 * SOAPHeaderFaultImpl.java
 *
 * Created on September 27, 2006, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPHeaderFault;

/**
 *
 * @author Michal Skvor
 */
public class SOAPHeaderFaultImpl implements SOAPHeaderFault {
    
    private QName message;
    private String part;
    private String use;
    private List<String> encodingStyles;
    private String namespaceURI;
    
    private QName type = SOAPConstants.HEADER_FAULT;
    
    /** Creates a new instance of SOAPHeaderFaultImpl */
    public SOAPHeaderFaultImpl( QName message, String part, String use ) {
        this.message = message;
        this.part = part;
        this.use = use;
    }
    
    public void setMessage( QName message ) {
        this.message = message;
    }

    public QName getMessage() {
        return message;
    }

    public void setPart( String part ) {
        this.part = part;
    }

    public String getPart() {
        return part;
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
