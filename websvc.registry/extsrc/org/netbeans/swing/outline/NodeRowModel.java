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

package org.netbeans.swing.outline;

/**
 * This interface provides information about nodes as they related to rows.
 * @author  David Botterill
 */
public interface NodeRowModel {
    /**
     * For the given row, return the corresponding node in the tree.
     * @param row, the row to use to get the corresponding node in the tree.
     */
     Object getNodeForRow(int row);
    
}
