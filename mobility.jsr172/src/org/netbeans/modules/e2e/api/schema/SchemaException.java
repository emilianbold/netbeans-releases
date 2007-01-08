/*
 * SchemaException.java
 *
 * Created on October 9, 2006, 5:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

/**
 *
 * @author Michal Skvor
 */
public class SchemaException extends Exception {
    
    public SchemaException( String message ) {
        super( message );
    }

    public SchemaException( Throwable cause ) {
        super( cause );
    }
}
