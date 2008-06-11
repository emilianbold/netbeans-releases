/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
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
     * Should fully qualified table names be used
     */
    public boolean isFullyQualifiedTableNames();

    /**
     * Returns the names of the tables which should be used to generate classes.
     */
    public Set<String> getTableNames();

    /**
     * Returns the schema of the table
     */
    public String getSchema(String tableName);

    /**
     * Returns the catalog of the table
     */
    public String getCatalog(String tableName);

    /**
     * Returns the root folder of the class which will be generated for
     * the specified table.
     */
    public FileObject getRootFolder(String tableName);

    /**
     * Returns the package of the class which will be generated for
     * the specified table.
     */
    public String getPackageName(String tableName);

    /**
     * Returns the name of the class to be generated for the specified table.
     */
    public String getClassName(String tableName);

    /** 
     * Returns the fetch type for the associations
     */
    public FetchType getFetchType();
    
    /**
     * Should the attributes used for regenenating schema from the entity classes be included
     */
    public boolean isRegenSchemaAttrs();
    
    /**
     * Returns the unique constraints defined on the table
     */
    public Set<List<String>> getUniqueConstraints(String tableName);
    
    /**
     * The collection type should be used for the OneToMany or ManyToMany cmr fields
     */
    public CollectionType getCollectionType();
}
