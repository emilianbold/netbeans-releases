/*
 * SOAPAddressImpl.java
 *
 * Created on September 27, 2006, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;

/**
 *
 * @author Michal Skvor
 */
public class SOAPAddressImpl implements SOAPAddress {
    
    private String locationURI;
    private QName type = SOAPConstants.ADDRESS;
    
    /** Creates a new instance of SOAPAddressImpl */
    public SOAPAddressImpl( String locationURI ) {
        this.locationURI = locationURI;
    }

    public void setLocationURI( String locationURI ) {
        this.locationURI = locationURI;
    }

    public String getLocationURI() {
        return locationURI;
    }

    public void setElementType( QName type ) {
        this.type = type;
    }

    public QName getElementType() {
        return type;
    }
    
}
