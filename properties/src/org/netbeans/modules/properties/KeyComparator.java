/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.util.Comparator;

/** Comparator for comparing property keys (Strings).
*
* @author Petr Jiricka
*/
public final class KeyComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    int res1 = String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
    if (res1 != 0)
      return res1;
    if (o1 instanceof String)
      return ((String)o1).compareTo(o2);
    else  
      throw new ClassCastException(o1.getClass().getName());
  }
}
/*
* <<Log>>
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/10/99  Petr Jiricka    
* $
*/
