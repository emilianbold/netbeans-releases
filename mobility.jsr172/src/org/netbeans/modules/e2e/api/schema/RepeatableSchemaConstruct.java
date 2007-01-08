/*
 * RepeatableSchemaConstruct.java
 *
 * Created on October 9, 2006, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class RepeatableSchemaConstruct extends SchemaConstruct {
    
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    private int minOccurs;
    private int maxOccurs;

    public RepeatableSchemaConstruct( SchemaConstruct.ConstructType constructType ) {
        super( constructType );
    }
    
    public RepeatableSchemaConstruct( SchemaConstruct.ConstructType constructType, QName name ) {
        super( constructType, name );
    }
    
    public void setMinOccurs( int minOccurs ) {
        this.minOccurs = minOccurs;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public void setMaxOccurs( int maxOccurs ) {
        this.maxOccurs = maxOccurs;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }     
}
