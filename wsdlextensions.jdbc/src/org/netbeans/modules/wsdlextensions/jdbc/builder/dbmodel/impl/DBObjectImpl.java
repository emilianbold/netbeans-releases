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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBObject;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class DBObjectImpl implements DBObject {
    private String name;

    private String javaName;

    private String description;

    private String schema;

    private String catalog;

    // private DBObjectModel parent;

    public DBObjectImpl() {
    }

    /*
     * public DBObjectImpl(DBObject src) { this(); if (src == null) { ResourceBundle cMessages =
     * NbBundle.getBundle(DBObjectImpl.class); throw new IllegalArgumentException(
     * cMessages.getString("ERROR_NULL_DBOBJECT")+("ERROR_NULL_DBOBJECT"));//NO i18n }
     * copyFrom(src); }
     */

    public DBObjectImpl(final String objectName, final String schemaName, final String catalogName) {
        this.name = objectName;
        this.schema = schemaName;
        this.catalog = catalogName;
    }

    /**
     * Performs deep copy of contents of given DBObject. We deep copy (that is, the method clones
     * all child objects such as columns) because columns have a parent-child relationship that must
     * be preserved internally.
     * 
     * @param source JDBC- providing contents to be copied.
     */
    public void copyFrom(final DBObject source) {
        if (source == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBObjectImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_REF") + "ERROR_NULL_REF");// NO
            // i18n
        } else if (source == this) {
            return;
        }

        this.name = source.getName();
        this.description = source.getDescription();
        this.schema = source.getSchema();
        this.catalog = source.getCatalog();

        // parent = source.getParent();
        this.deepCopyReferences(source);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getCatalog() {
        return this.catalog;
    }

    /*
     * public DBObjectModel getParent() { return this.parent; }
     */
    /*
     * Perform deep copy of columns. @param source JDBCTable whose columns are to be copied.
     */
    private void deepCopyReferences(final DBObject source) {
        // PP:ToDO
    }

    /*
     * public void setParent(DBObjectModel databaseObject) { this.parent = databaseObject; }
     */
    public String getJavaName() {
        return this.javaName;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setJavaName(final String javaName) {
        this.javaName = javaName;
    }

    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }
}
