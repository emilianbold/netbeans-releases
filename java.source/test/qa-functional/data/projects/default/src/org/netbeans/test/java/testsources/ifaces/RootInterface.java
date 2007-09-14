/*
 * RootInterface1.java
 *
 * Created on April 14, 2000, 9:25 AM
 */
 
package org.netbeans.test.java.testsources.ifaces;

/** 
 *
 * @author  jbecicka
 * @version 
 */

public interface RootInterface {

    public static final int zero =0;
    public static final double pi =3.14;
    
    /** method1FromRootInterface() Comment */
    public int method1FromRootInterface();
    
    /** method2FromRootInterface(int x,int y) Comment*/
    public String method2FromRootInterface(int x,int y);
    
    /** method3FromRootInterface(String a, String b) Comment*/
    public boolean method3FromRootInterface(String a, String b);
}
