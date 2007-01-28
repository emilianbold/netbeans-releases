/*
 * EjbMethodFilter.java
 *
 * Created on June 16, 2004, 6:02 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import java.lang.reflect.Method;

/**
 * To filter out the following 5 methods from the business methods
 *  - void remove() throws java.rmi.RemoteException, javax.ejb.RemoveException
 *  - javax.ejb.Handle getHandle() throws java.rmi.RemoteException
 *  - javax.ejb.EJBHome getEJBHome() throws java.rmi.RemoteException
 *  - java.lang.Object getPrimaryKey() throws java.rmi.RemoteException
 *  - boolean isIdentical( javax.ejb.EJBObject ) throws java.rmi.RemoteException
 *
 * @author  cao
 */
public class EjbMethodFilter 
{
    public static boolean isEjbSpecMethod( Method method )
    {
        // Make sure method name, parameters are all the same
        if( method.getName().equals( "remove" ) &&
            method.getParameterTypes().length == 0 )
        {
            // void remove() throws java.rmi.RemoteException, javax.ejb.RemoveException
            return true;
        }
        else if( method.getName().equals( "getHandle" ) &&
                 method.getParameterTypes().length == 0 )
        {
            // javax.ejb.Handle getHandle() throws java.rmi.RemoteException
            return true;
        }
        else if( method.getName().equals( "getEJBHome" )  &&
                 method.getParameterTypes().length == 0 )
        {
            // ax.ejb.EJBHome getEJBHome() throws java.rmi.RemoteException
            return true;
        }
        else if( method.getName().equals( "getPrimaryKey" ) &&
                 method.getParameterTypes().length == 0 )
        {
            // java.lang.Object getPrimaryKey() throws java.rmi.RemoteException
            return true;
        }
        else if( method.getName().equals( "isIdentical" ) &&
                 method.getParameterTypes().length == 1 )
        {
            // boolean isIdentical( javax.ejb.EJBObject ) throws java.rmi.RemoteException
            return true;
        }
        else
            return false;
    }
}
