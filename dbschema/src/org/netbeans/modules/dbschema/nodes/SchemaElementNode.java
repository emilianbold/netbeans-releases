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

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

public class SchemaElementNode extends DBElementNode {
    /** Creates new SchemaElementNode */
    public SchemaElementNode(SchemaElement element, Children children, boolean writeable) {
        super(element, children, writeable);
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return SCHEMA;
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createNameProperty(writeable));
        ps.put(createSchemaProperty(writeable));
        ps.put(createCatalogProperty(writeable));
        ps.put(createDatabaseProductNameProperty(writeable));
        ps.put(createDatabaseProductVersionProperty(writeable));
        ps.put(createDriverNameProperty(writeable));
        ps.put(createDriverVersionProperty(writeable));
        ps.put(createDriverProperty(writeable));
        ps.put(createUrlProperty(writeable));
        ps.put(createUsernameProperty(writeable));

        return sheet;
    }

	/** Create a property for the schema schema.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createSchemaProperty (boolean canW) {
		return new ElementProp(PROP_SCHEMA, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getSchema().getName();
			}
		};
	}
    
	/** Create a property for the schema catalog.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createCatalogProperty (boolean canW) {
		return new ElementProp(PROP_CATALOG, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getCatalog().getName();
			}
		};
	}
    
	/** Create a property for the schema database product name.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDatabaseProductNameProperty (boolean canW) {
		return new ElementProp("databaseProductName", String.class, canW) {  //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDatabaseProductName();
			}
		};
	}

	/** Create a property for the schema database product version.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDatabaseProductVersionProperty (boolean canW) {
		return new ElementProp("databaseProductVersion", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDatabaseProductVersion();
			}
		};
	}

	/** Create a property for the schema driver name.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverNameProperty (boolean canW) {
		return new ElementProp("driverName", String.class, canW) {  //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriverName();
			}
		};
	}

	/** Create a property for the schema driver version.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverVersionProperty (boolean canW) {
		return new ElementProp("driverVersion", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriverVersion();
			}
		};
	}

	/** Create a property for the schema driver URL.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createDriverProperty (boolean canW) {
		return new ElementProp("driver", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getDriver();
			}
		};
	}

    /** Create a property for the schema url.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createUrlProperty (boolean canW) {
		return new ElementProp("url", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getUrl();
			}
		};
	}

	/** Create a property for the schema username.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createUsernameProperty (boolean canW) {
		return new ElementProp("username", String.class, canW) { //NOI18N
			/** Gets the value */
			public Object getValue () {
				return ((SchemaElement) element).getUsername();
			}
        };
	}

}
