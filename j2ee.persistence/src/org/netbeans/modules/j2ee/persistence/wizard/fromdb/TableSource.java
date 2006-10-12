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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;

/**
 * Describes a source for tables and can save and retrieve the source
 * for a given project.
 *
 * <p>The source for tables consists of the type (data source,
 * database connection or dbschema file) and the name of the source,
 * whose meaning is: the JNDI name for the data source, the
 * {@link org.netbeans.api.db.explorer.DatabaseConnection#getName() name}
 * of the database connection or the absolute path of the dbschema file.</p>
 *
 * @author Andrei Badea
 */
public class TableSource {

    public enum Type { DATA_SOURCE, CONNECTION, SCHEMA_FILE };

    private final static Map<Project, TableSource> PROJECT_TO_SOURCE = new WeakHashMap<Project, TableSource>();

    private final Type type;
    private final String name;

    public static TableSource get(Project project) {
        synchronized (TableSource.class) {
            return PROJECT_TO_SOURCE.get(project);
        }
    }

    public static void put(Project project, TableSource tableSource) {
        synchronized (TableSource.class) {
            PROJECT_TO_SOURCE.put(project, tableSource);
        }
    }

    public TableSource(String name, Type type) {
        assert name != null;
        assert type != null;

        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
