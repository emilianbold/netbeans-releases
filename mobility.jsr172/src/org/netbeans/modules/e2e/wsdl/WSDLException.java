/*
 * WSDLException.java
 *
 * Created on February 20, 2007, 11:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

/**
 *
 * @author Misk
 */
public class WSDLException extends Exception {
    
    public WSDLException( String message ) {
        super( message );
    }

    public WSDLException( Throwable cause ) {
        super( cause );
    }    
}
