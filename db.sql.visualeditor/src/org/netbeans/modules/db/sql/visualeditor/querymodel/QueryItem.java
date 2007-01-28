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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.Collection;

// the comon base type of every item present in a query
// provides common functionalities to all items
public interface QueryItem {
    // generate text that represent the item
    public String genText();
    // walks recursively the specific item to find all teh columns that are referenced from this item.
    // For instance, called on a WHERE cluase will return all teh columns used in the expression of the WHERE clause
    // could be used by the editor to obtain info on all teh column used in a particular clause
    public void getReferencedColumns(Collection columns);
    // fills the provided Collection with a list of all the items used in this specific item, but only one level
    // So for instance on Query it would provide what the query is composed of (Select, From, Where, etc.)
    // on Where it would provide the expression used
    // on Column it would do nothing as that does not have any "nested" query item
    public void getQueryItems(Collection items);
}

