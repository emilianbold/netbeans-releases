/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.api;

import java.util.Collection;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;

/**
 * Encapsulates a database schema.
 *
 * @author Andrei Badea
 */
public class Schema extends MetadataElement {

    final SchemaImplementation impl;

    Schema(SchemaImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the catalog containing this schema.
     *
     * @return the parent catalog.
     */
    public Catalog getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of this schema or {@code null} if the name is not known.
     *
     * @return the name or {@code null}.
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Returns {@code true} if this schema is the default one in the parent catalog.
     *
     * @return {@code true} if this is the default schema, {@false} otherwise.
     */
    public boolean isDefault() {
        return impl.isDefault();
    }

    /**
     * Returns {@code true} if this schema is synthetic.
     *
     * @return {@code true} if this is a synthetic schema, {@false} otherwise.
     */
    public boolean isSynthetic() {
        return impl.isSynthetic();
    }

    /**
     * Returns the tables in this schema.
     *
     * @return the tables.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Table> getTables() {
        return impl.getTables();
    }

    /**
     * Returns the table with the given name.
     *
     * @param name a table name.
     * @return a table named {@code name} or {@code null} if there is no such table.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Table getTable(String name) {
        return impl.getTable(name);
    }

    @Override
    public String toString() {
        return "Schema[name='" + impl.getName() + "',default=" + isDefault() + ",synthetic=" + isSynthetic() + "]"; // NOI18N
    }
}
