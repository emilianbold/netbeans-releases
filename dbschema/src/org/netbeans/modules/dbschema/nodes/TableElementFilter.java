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

/** Orders and filters members in a table element node.
* Can be used for columns, indexes, etc.
*/
public class TableElementFilter extends SchemaElementFilter {
    /** Specifies a child representing a column. */
    public static final int       COLUMN = 4;
    /** Specifies a child representing an index. */
    public static final int     INDEX = 8;
    /** Specifies a child representing a foreign key. */
    public static final int     FK = 16;
    /** Specifies a child representing a column pair. */
    public static final int     COLUMN_PAIR = 32;
    /** Does not specify a child type. */
    public static final int     ALL = SchemaElementFilter.ALL | COLUMN | COLUMN_PAIR | INDEX | FK;

    /** Default order and filtering.
    * Places all columns, indexes, and foreign keys together in one block.
    */
    public static final int[] DEFAULT_ORDER = {COLUMN | COLUMN_PAIR | INDEX | FK };
    
    /** stores property value */
    private boolean sorted = true;
  
    /** Test whether the elements in one element type group are sorted.
    * @return <code>true</code> if groups in getOrder () field are sorted, <code>false</code> 
    * to default order of elements
    */
    public boolean isSorted () {
        return sorted;
    }

    /** Set whether groups of elements returned by getOrder () should be sorted.
    * @param sorted <code>true</code> if so
    */
    public void setSorted (boolean sorted) {
        this.sorted = sorted;
    }
}
