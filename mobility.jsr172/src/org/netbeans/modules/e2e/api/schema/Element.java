/*
 * Element.java
 *
 * Created on October 9, 2006, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class Element extends RepeatableSchemaConstruct {
    
    private boolean nillable;
    private Type elementType ;

    public Element() {
        super( SchemaConstruct.ConstructType.ELEMENT );
        
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        nillable = false;
    }
    
    public Element( QName name ) {
        super( SchemaConstruct.ConstructType.ELEMENT, name );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        nillable = false;
        
        setName( name );
    }

    public Element( QName name, Type elementType ) {
        this( name );
        this.elementType = elementType;
    }
    
    public Element( QName name, Type elementType, int minOccurs, int maxOccurs ) {
        this( name, elementType );
        nillable = false;
        
        setMinOccurs( minOccurs );
        setMaxOccurs( maxOccurs );
    }
    
    public void setNillable( boolean nillable ) {
        this.nillable = nillable;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setType( Type elementType ) {
        this.elementType = elementType;
    }

    public Type getType() {
        return elementType;
    }
    
    public String getJavaName() {
        String javaName = getJavaName();
        if( javaName != null ) return javaName;
        return getName().getLocalPart() + ( getMaxOccurs() > 1 ? "[]" : "" ); 
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "element" ); 
        if( getName() != null ) sb.append( " name='" + getName() + "'" );
        sb.append( " minOccurs='" + getMinOccurs() + "'" );
        if( getMaxOccurs() ==  RepeatableSchemaConstruct.UNBOUNDED ) {
            sb.append( " maxOccurs='unbounded'" );
        } else {
            sb.append( " maxOccurs='" + getMaxOccurs() + "'" );
        }
        sb.append( " nillable='" + nillable + "'" );
//        if( type != null ) sb.append( " type='" + type.getPrefix() + ":" + type.getLocalPart() + "'");
        sb.append( '\n' );
//        for( ComplexType ct : complexTypes ) {
//            sb.append( '\t' + ct.toString());
//        }

        return sb.toString();
    }
}
