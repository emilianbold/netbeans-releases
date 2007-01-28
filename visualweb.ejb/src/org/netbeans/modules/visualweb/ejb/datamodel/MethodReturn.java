/*
 * MethodReturn.java
 *
 * Created on February 8, 2005, 4:17 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

/**
 *
 * @author  cao
 */
public class MethodReturn implements java.lang.Cloneable {
    
    private String className;
    private boolean isCollection;
    private String elemClassName;
    
    public MethodReturn( String className, boolean isCollection, String elemClassName ) {
        this.className = className;
        this.isCollection = isCollection;
        this.elemClassName = elemClassName;
    }
    
    public MethodReturn() {
        this( null, false, null );
    }
    
    public void setClassName( String className )
    {
        this.className = className;
    }
    
    public void setIsCollection( boolean col )
    {
        this.isCollection = col;
    }
    
    public void setElemClassName( String elemClassName )
    {
        this.elemClassName = elemClassName;
    }
    
    public String getClassName() { return this.className; }
    public boolean isCollection() { return this.isCollection; }
    public String getElemClassName() { return this.elemClassName; }
    
    public boolean isVoid()
    {
        if( className.equals( "void" ) )
            return true;
        else
            return false;
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
