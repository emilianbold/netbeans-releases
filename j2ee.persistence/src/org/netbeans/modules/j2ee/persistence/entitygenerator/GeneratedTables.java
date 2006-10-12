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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * This interface describes the tables used to generate
 * classes and these classes. It contains a set of tables
 * and the locations of the classes generated for these tables
 * (the root folder, the package name and the class name).
 *
 * @author Andrei Badea
 */
public interface GeneratedTables {

    /**
     * Returns the names of the tables which should be used to generate classes.
     */
    public Set<String> getTableNames();

    /**
     * Returns the root folder of the class which will be generated for
     * the specified table.
     */
    public FileObject getRootFolder(String tableName);

    /**
     * Returns the package of the class which will be generated for
     * the specified table.
     */
    public String  getPackageName(String tableName);

    /**
     * Returns the name of the class to be generated for the specified table.
     */
    public String getClassName(String tableName);
}
