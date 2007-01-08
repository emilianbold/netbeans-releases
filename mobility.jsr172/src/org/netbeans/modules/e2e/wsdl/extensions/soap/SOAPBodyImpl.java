/*
 * SOAPBodyImpl.java
 *
 * Created on September 27, 2006, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBody;

/**
 *
 * @author Michal Skvor
 */
public class SOAPBodyImpl implements SOAPBody {
    
    private List<String> parts;
    private String use;
    private List<String> encodingStyles;
    private String namespaceURI;
    
    private QName type = SOAPConstants.BODY;
    
    /** Creates a new instance of SOAPBodyImpl */
    public SOAPBodyImpl( String use ) {
        this.use = use;
        parts = new ArrayList();
    }

    public void setParts( List<String> parts ) {
        this.parts = parts;
    }

    public List<String> getParts() {
        return Collections.unmodifiableList( parts );
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
