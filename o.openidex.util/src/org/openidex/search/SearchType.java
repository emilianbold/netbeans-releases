/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package src_modules.org.openidex.search;

import java.util.Enumeration;

import javax.swing.*;

import org.openide.*;
import org.openide.nodes.*;


/** A class that represents one search type to the user and 
* has the ability to create object that can search on the basis
* of this style.
* <p>
* Semantic constrain: All properties defining state must be bound.
* <P>
* There is a list of all SearchTypes in the system and one can 
* easily find which is enabled on which nodes.
*
* @author  Jaroslav Tulach
*/
public abstract class SearchType extends org.openide.ServiceType {
  
  
  /** Is this object enabled on given set of nodes or not.
  * @param arr array of nodes to use in this kriterium
  * @return true if this style of search can be done on given nodes
  */
  public abstract boolean enabled (Node[] arr);
  
  /** List of all SearchTypes in the system.
  * @return enumeration of SearchType instances
  */
  public static Enumeration enumerateSearchTypes () {
    return TopManager.getDefault().getServices().services(SearchType.class);
  }
  
  /** Gives the Scanner's class that instance can be used for
  * the search.
  *
  * @return the class that must be subclass of Scanner
  */
  public abstract Class getScannerClass ();
  
  /** Gives short type name.
  * @return String representing name used as tab label or null
  */
  public static String getTabText() {
    return null;
  }

  
  /** @return display name obtained from BeanDescriptor or null.
  */
  public String getDisplayName() {
    try {
      return (new BeanNode(this)).getDisplayName();
    } catch (java.beans.IntrospectionException ex) {
      return null;
    }    
  }
  
}

/* 
* Log
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
  