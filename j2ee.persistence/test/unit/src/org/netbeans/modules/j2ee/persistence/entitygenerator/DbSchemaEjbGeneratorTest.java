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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Andrei Badea
 */
public class DbSchemaEjbGeneratorTest extends NbTestCase {

    public DbSchemaEjbGeneratorTest(String testName) {
        super(testName);
    }

    public void testIssue92031() throws Exception {
        /*
        create table part1 (id int not null primary key);
        create table part2 (part1_id int not null references part1(id) primary key);
         */
        SchemaElement schema = SchemaElementUtil.forName(URLMapper.findFileObject(getClass().getResource("Issue92031.dbschema")));

        DbSchemaEjbGenerator generator = new DbSchemaEjbGenerator(
                new GeneratedTablesImpl(new HashSet(Arrays.asList("PART1", "PART2"))),
                schema);

        EntityClass[] beans = generator.getBeans();

        EntityClass bean = getBeanByTableName(beans, "PART2");
        assertNotNull(getFieldByName(bean, "part1"));
        RelationshipRole role = (RelationshipRole)bean.getRoles().iterator().next();
        assertEquals("part11", role.getFieldName());
        assertNotNull("Should have CMR mapping for field part11", bean.getCMPMapping().getCmrFieldMapping().get("part11"));
    }

    private static EntityMember getFieldByName(EntityClass bean, String fieldName) {
        for (Iterator i = bean.getFields().iterator(); i.hasNext();) {
            EntityMember member = ((EntityMember)i.next());
            if (fieldName.equals(member.getMemberName())) {
                return member;
            }
        }
        return null;
    }

    private static EntityClass getBeanByTableName(EntityClass[] beans, String tableName) {
        for (int i = 0; i < beans.length; i++) {
            if (tableName.equals(beans[i].getTableName())) {
                return beans[i];
            }
        }
        return null;
    }

    private static final class GeneratedTablesImpl implements GeneratedTables {

        private final Set<String> tableNames;

        public GeneratedTablesImpl(Set<String> tableNames) {
            this.tableNames = tableNames;
        }

        public Set<String> getTableNames() {
            return tableNames;
        }

        public FileObject getRootFolder(String tableName) {
            return null;
        }

        public String getPackageName(String tableName) {
            return null;
        }

        public String getClassName(String tableName) {
            return tableName;
        }
    }
}
