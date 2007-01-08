/*
 * SOAPOperationImpl.java
 *
 * Created on September 27, 2006, 12:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.extensions.soap;

import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;

/**
 *
 * @author Michal Skvor
 */
public class SOAPOperationImpl implements SOAPOperation {
    
    private String soapActionURI;
    private String style;
    
    private QName type = SOAPConstants.OPERATION;
    
    /** Creates a new instance of SOAPOperationImpl */
    public SOAPOperationImpl() {
    }

    public void setSoapActionURI( String soapActionURI ) {
        this.soapActionURI = soapActionURI;
    }

    public String getSoapActionURI() {
        return soapActionURI;
    }

    public void setStyle( String style ) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public void setElementType( QName type ) {
        this.type = type;
    }

    public QName getElementType() {
        return type;
    }
}
