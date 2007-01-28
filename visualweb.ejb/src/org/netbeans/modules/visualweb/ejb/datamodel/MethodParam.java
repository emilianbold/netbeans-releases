/*
 * MethodParam.java
 *
 * Created on February 7, 2005, 6:03 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;



/**
 * To encapsulate the method parameter name and type
 *
 * @author  cao
 */
public class MethodParam implements java.lang.Cloneable {
    
    private String name;
    private String type;
    
    public MethodParam() {
    }
    
    public MethodParam( String name, String type ) {
        this.name = name.trim();
        this.type = type;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getType()
    {
        return this.type;
    }
    
    public void setName( String name )
    {
        this.name = name;
    }
    
    public void setType( String type )
    {
        this.type = type;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( getType() );
        buf.append( " " );
        buf.append( getName() );
        
        return buf.toString();
    }
    
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch( java.lang.CloneNotSupportedException e )
        {
            return null;
        }
    }
}
