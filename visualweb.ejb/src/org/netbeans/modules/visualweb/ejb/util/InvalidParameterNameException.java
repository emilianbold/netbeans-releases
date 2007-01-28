/*
 * InvalidParameterNameException.java
 *
 * Created on February 9, 2005, 11:15 AM
 */

package org.netbeans.modules.visualweb.ejb.util;

/**
 * An exception for invalid parameter names
 *
 * @author  cao
 */
public class InvalidParameterNameException extends Exception {
    
    /** Creates a new instance of InvalidParameterNameException */
    public InvalidParameterNameException() {
        super();
    }
    
    public InvalidParameterNameException( String msg ) {
        super( msg );
    }
    
}
