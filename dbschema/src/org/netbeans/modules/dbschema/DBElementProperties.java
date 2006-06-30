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

package org.netbeans.modules.dbschema;

/** Names of properties of elements.
 */
public interface DBElementProperties {
	/** Name of {@link DBElement#getName name} property for {@link
	 * DBElement db elements}.
	 */
	public static final String PROP_NAME = "name"; //NOI18N

	/** Name of {@link ColumnElement#getType type} property for {@link
	 * ColumnElement   column elements}.
	 */
	public static final String PROP_TYPE = "type"; //NOI18N

	/** Name of {@link ColumnElement#isNullable nullable} property for {@link
	 * ColumnElement   column elements}.
	 */
	public static final String PROP_NULLABLE = "nullable"; //NOI18N

	/** Name of {@link ColumnElement#getLength length} property for {@link
	 * ColumnElement   column elements}.
	 */
	public static final String PROP_LENGTH = "length"; //NOI18N

	/** Name of {@link ColumnElement#getPrecision precision} property for {@link
	 * ColumnElement   column elements}.
	 */
	public static final String PROP_PRECISION = "precision"; //NOI18N

	/** Name of {@link ColumnElement#getScale scale} property for {@link
	 * ColumnElement   column elements}.
	 */
	public static final String PROP_SCALE = "scale"; //NOI18N

	/** Name of {@link IndexElement#isUnique flag} property for {@link
	 * IndexElement   index elements}.
	 */
	public static final String PROP_UNIQUE = "unique"; //NOI18N

	/** Name of {@link UniqueKeyElement#isPrimaryKey flag} property for {@link
	 * UniqueKeyElement   unique key elements}.
	 */
	public static final String PROP_PK = "primaryKey"; //NOI18N

	/** Name of {@link SchemaElement#getSchema schema} property for {@link
	 * SchemaElement schema elements}. 
	 */
	public static final String PROP_SCHEMA= "schema"; //NOI18N

	/** Name of {@link SchemaElement#getCatalog catalog} property for {@link
	 * SchemaElement schema elements}. 
	 */
	public static final String PROP_CATALOG= "catalog"; //NOI18N
  
    /** Name of tables property for {@link SchemaElement#getTables schema elements}.
     */
    public static final String PROP_TABLES = "tables"; // NOI18N

	/** Name of {@link TableElement#getColumns columns} property for {@link
	 * TableElement tables}.
	 */
	public static final String PROP_COLUMNS = "columns"; //NOI18N
    
	/** Name of {@link TableElement#getColumnPairs column pairs} property for {@link
	 * TableElement tables}.
	 */
	public static final String PROP_COLUMN_PAIRS = "columnPairs"; //NOI18N

	/** Name of {@link TableElement#getIndexes indexes} property for {@link
	 * TableElement tables}.
	 */
	public static final String PROP_INDEXES = "indexes"; //NOI18N

	/** Name of {@link TableElement#getKeys keys} property for
	 * {@link TableElement tables}.
	 */
	public static final String PROP_KEYS = "keys"; //NOI18N

	/** Name of {@link SchemaElement#getStatus status} property for {@link
	 * SchemaElement schema elements}.
	 */
	public static final String PROP_STATUS = "status"; //NOI18N

	/** Name of {@link TableElement#isTableOrView is table or view} property for
	 * {@link TableElement tables}.
	 */
	public static final String PROP_TABLE_OR_VIEW = "tableOrView"; //NOI18N
    
	/** Name of {@link ColumnPairElement#getLocalColumn local column} property for
	 * {@link ColumnPairElement column pair elements}.
	 */
	public static final String PROP_LOCAL_COLUMN = "localColumn"; //NOI18N
    
	/** Name of {@link ColumnPairElement#getReferencedColumn referenced column} property for
	 * {@link ColumnPairElement column pair elements}.
	 */
	public static final String PROP_REFERENCED_COLUMN = "referencedColumn"; //NOI18N
}
