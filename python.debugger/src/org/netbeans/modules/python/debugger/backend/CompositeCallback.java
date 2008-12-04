/**
* Copyright (C) 2005 Jean-Yves Mengant
*
*/

package org.netbeans.modules.python.debugger.backend;

import java.util.TreeMap;


/**
 * This interface is used to callback composite variable introspector 
 * 
 * @author jean-yves
 *
 */
public interface CompositeCallback
{

  public void callbackWithValuesSet( TreeMap values , TreeMap types ) ; 

}
