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

package org.openidex.search;

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
public abstract class SearchType extends org.openide.ServiceType implements Cloneable {
  
  public static final long serialVersionUID = 2L;
  
  /** Name of valid property. */
  public static final String PROP_VALID = "valid";
  private boolean valid;
  
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
  public abstract String getTabText();

  /** If any node match criterion it will be
    returnded with following detail classes attached.
    @return array of Classes their instances will have to be 
      attached to matching node.
  */
  public abstract Class[] getDetailClasses();
  
//----------- Utility methods -------------------  
  
  /** @return display name obtained from BeanDescriptor or null.
  */
  public String getDisplayName() {
    try {
      return (new BeanNode(this)).getDisplayName();
    } catch (java.beans.IntrospectionException ex) {
      return null;
    }    
  }
  
  /** SearchType must be cloneable.
  *
  * @return a clone
  */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException("SearchType must be clonable."); // NOI18N
    }
  }

  /** Now the custonized criterion changed validity state. */
  protected final void setValid(boolean state) {
    boolean old = valid;
    valid = state;
    firePropertyChange(PROP_VALID, new Boolean(old), new Boolean(state));
  }
  
  /** @return true if the criterion is currently valid. */
  public final boolean isValid() {
    return valid;
  }
}

/* 
* Log
*  7    Gandalf-post-FCS1.5.1.0     4/4/00   Petr Kuzel      unknown state
*  6    Gandalf   1.5         1/18/00  Jesse Glick     Context help.
*  5    Gandalf   1.4         1/14/00  Ian Formanek    I18N
*  4    Gandalf   1.3         1/4/00   Petr Kuzel      Polymorphism
*  3    Gandalf   1.2         12/15/99 Martin Balin    Fixed package statement
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Enforcing Cloneable.
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
  