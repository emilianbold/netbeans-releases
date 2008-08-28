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

package org.netbeans.modules.db.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author David
 */
public class DerbyDBProvider extends DefaultDBProvider {
    @Override
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        if (!schemaExists(conn, schemaName)) {
            return;
        }

        // With Derby, you can't just drop the schema.  You have go manually
        // deal with all the constraints
        
        // drop views first, as they depend on tables
        DatabaseMetaData md = conn.getMetaData();
        
        ResultSet rs = md.getTables(null, schemaName, null,
                new String[] { "VIEW" } );
        Vector views = new Vector();
        while ( rs.next() ) {
            String view = rs.getString(3);
            views.add(view);
        }
        rs.close();
        
        setSchema(conn, schemaName);

        Iterator it = views.iterator();        
        while (it.hasNext()) {
            String view = (String)it.next();
            dropView(conn, schemaName, view);
        }
        
        // drop all tables
        md = conn.getMetaData();
        
        rs = md.getTables(null, schemaName, null, null);
        Vector tables = new Vector();
        while ( rs.next() ) {
            String table = rs.getString(3);
            tables.add(table);
        }
        rs.close();
        
        setSchema(conn, schemaName);

        it = tables.iterator();        
        while (it.hasNext()) {
            String table = (String)it.next();
            dropTable(conn, schemaName, table);
        }
        
        // drop schema
        conn.createStatement().executeUpdate("DROP SCHEMA " + schemaName + " RESTRICT");
        
    }
}
