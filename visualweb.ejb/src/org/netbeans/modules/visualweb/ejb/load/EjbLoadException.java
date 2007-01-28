/*
 * EjbLoadException.java
 *
 * Created on May 5, 2004, 7:04 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

/**
 * This exception will be thrown if there is an error or exception
 * happened during loading the ejb group
 *
 * @author  cao
 */
public class EjbLoadException extends Exception 
{
    public static final int USER_ERROR = 1;
    public static final int SYSTEM_EXCEPTION = 2;
    public static final int WARNING = 3;
    
    private int exceptionType = SYSTEM_EXCEPTION;
    
    public EjbLoadException() 
    {
        super();
    }
    
    public EjbLoadException(String message)
    {
        super( message );
    }
    
    public EjbLoadException( int type, String message )
    {
        super( message );
        
        exceptionType = type;
    }
    
    public int getExceptionType()
    {
        return this.exceptionType;
    }
}
