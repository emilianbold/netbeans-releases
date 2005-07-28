/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.nodes;

/** Interface for filtering and ordering the items in the visual
* presentation of a source element.
* Used to control the children of a source element node.
* <p>Note that this does <em>not</em> fire events for changes
* in its properties; it is expected that a new filter will instead
* be created and applied to the source children.
*
* @see org.openide.src.SourceElement
* @see SourceChildren
* @author Dafe Simonek, Jan Jancura
*/
public class SchemaElementFilter {

  /** Specifies a child representing a package or class import. */
  public static final int       TABLE = 1;
  /** Specifies a child representing a (top-level) class. */
  public static final int       VIEW = 2;
  /** Does not specify any top-level element. */
  public static final int       ALL = TABLE + VIEW;

  /** Default order of the top-level element types in the hierarchy.
  * A list, each of whose elements is a bitwise disjunction of element types.
  * By default, only classes and interfaces are listed, and these together.
  */
  public static final int[]     DEFAULT_ORDER = {TABLE + VIEW};

  /** stores property value */
  private boolean               allTables = false;
  /** stores property value */
  private int[]                 order = null;
  

  /** Test whether all classes in the source should be recursively shown.
  * @return <code>true</code> to include inner classes/interfaces, <code>false</code> to only
  * include top-level classes/interfaces
  */
  public boolean isAllTables () {
    return allTables;
  }

  /** Set whether all classes should be shown.
  * @param type <code>true</code> if so
  * @see #isAllClasses
  */
  public void setAllTables (boolean allTables) {
    this.allTables = allTables;
  }

  /** Get the current order for elements.
  * @return the current order, as a list of bitwise disjunctions among element
  * types (e.g. {@link #CLASS}). If <code>null</code>, the {@link #DEFAULT_ORDER},
  * or no particular order at all, may be used.
  */
  public int[] getOrder () {
    return order;
  }

  /** Set a new order for elements.
  * Should update the children list of the source element node.
  * @param order the new order, or <code>null</code> for the default
  * @see #getOrder
  */
  public void setOrder (int[] order) {
    this.order = order;
  }
}
