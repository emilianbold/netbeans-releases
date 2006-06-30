/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
