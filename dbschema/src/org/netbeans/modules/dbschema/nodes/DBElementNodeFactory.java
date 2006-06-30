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

import org.openide.nodes.Node;

import org.netbeans.modules.dbschema.*;

/** A factory used to create instances of hierarchy node implementations.
* Loaders that use the element hierarchy should implement this factory
* so as to provide their own implementations of hierarchy element nodes.
* @see TableChildren
*/
public interface DBElementNodeFactory {
	/** Make a node representing a schema.
	 * @param element the schema
	 * @return a schema node instance
	 */
	public Node createSchemaNode (SchemaElement element);
  
	/** Make a node representing a column.
	 * @param element the column
	 * @return a column node instance
	 */
	public Node createColumnNode (ColumnElement element);
    
	/** Make a node representing a column pair.
	 * @param element the column pair
	 * @return a column pair node instance
	 */
	public Node createColumnPairNode (ColumnPairElement element);

	/** Make a node representing an index.
	 * @param element the index
	 * @return an index node instance
	 */
	public Node createIndexNode (IndexElement element);

	/** Make a node representing a foreign key.
	 * @param element the foreign key
	 * @return a foreign key node instance
	 */
	public Node createForeignKeyNode (ForeignKeyElement element);

	/** Make a node representing a table.
	 * @param element the table
	 * @return a table node instance
	 */
	public Node createTableNode (TableElement element);

	/** Make a node indicating that the creation of children
	 * is still under way.
	 * It should be used when the process is slow.
	 * @return a wait node
	 */
	public Node createWaitNode ();

	/** Make a node indicating that there was an error creating
	 * the element children.
	 * @return the error node
	 */
	public Node createErrorNode ();
}
