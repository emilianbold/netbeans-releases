/*
 * Type.java
 *
 * Created on September 26, 2006, 11:01 AM
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
public class Type extends RepeatableSchemaConstruct {
        
    public static final short FLAVOR_SEQUENCE               = 1;
    public static final short FLAVOR_PRIMITIVE_ENUMERATION  = 2;
    public static final short FLAVOR_PRIMITIVE              = 3;
    
    private short flavor;
    private String javaTypeName;
    
    private List<SchemaConstruct> subconstructs;
    
    /** Creates a new instance of Type */
    public Type() {
        super( SchemaConstruct.ConstructType.TYPE );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        subconstructs = new ArrayList();
    }
    
    public Type( QName name ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        
        subconstructs = new ArrayList();
    }
    
    public Type( QName name, short flavor ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        setMinOccurs( 1 );
        setMaxOccurs( 1 );
        
        subconstructs = new ArrayList();
        
        this.flavor = flavor;
    }
    
    public Type( QName name, short flavor, int minOccurs, int maxOccurs ) {
        super( SchemaConstruct.ConstructType.TYPE, name );
        subconstructs = new ArrayList();
        
        this.flavor = flavor;
        setMinOccurs( minOccurs );
        setMaxOccurs( maxOccurs );
    }
    
    public void setFlavor( short flavor ) {
        this.flavor = flavor;
    }
    
    public short getFlavor() {
        return flavor;
    }
    
    public void setJavaTypeName( String javaTypeName ) {
        this.javaTypeName = javaTypeName;
    }
    
    public String getJavaTypeName() {
        return javaTypeName;
    }
    
    public void addSubconstruct( SchemaConstruct subconstruct ) {
        subconstructs.add( subconstruct );
    }
    
    public List<SchemaConstruct> getSubconstructs() {
        return Collections.unmodifiableList( subconstructs );
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "name='" + getName() + "' ");
        sb.append( "type='" );
        if( FLAVOR_PRIMITIVE == flavor ) {
            sb.append( "primitive" );
        } else if( FLAVOR_SEQUENCE == flavor ) {
            sb.append( "sequence" );
        }
        sb.append( "' name='" + getName());
        sb.append( "' minOccurs='" + getMinOccurs() + "'" );
        if( getMaxOccurs() ==  RepeatableSchemaConstruct.UNBOUNDED ) {
            sb.append( " maxOccurs='unbounded'" );
        } else {
            sb.append( " maxOccurs='" + getMaxOccurs() + "'" );
        }
        
        return sb.toString();
    }
    
    
}
