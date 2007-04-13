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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelTest extends EntityMappingsTestCase {

    public EntityMappingsMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerImpl.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity(name = \"Customer\")" +
                "public class CustomerImpl {" +
                "   @Id()" +
                "   @Column(name = \"CUST_ID\")" +
                "   private int id;" +
                "   @Basic(optional = false)" +
                "   private int age;" +
                "   @Column(name = \"CUST_NAME\", nullable = false)" +
                "   private String name;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   @Column(name=\"EMP_ID\")" +
                "   private int id;" +
                "}");
        final String expectedResult = "foo";
        String result = createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, String>() {
            public String run(EntityMappingsMetadata metadata) {
                EntityMappings entityMappings = metadata.getRoot();
                Entity[] entityList = entityMappings.getEntity();
                assertEquals(2, entityList.length);
                // test Customer
                Entity entity = getEntityByName(entityList, "Customer");
                assertEquals("foo.CustomerImpl", entity.getClass2());
                assertEquals("CUSTOMER", entity.getTable().getName());
                assertEquals("", entity.getTable().getSchema());
                assertEquals("", entity.getTable().getCatalog());
                Id[] idList = entity.getAttributes().getId();
                assertEquals(1, idList.length);
                assertEquals("id", idList[0].getName());
                assertEquals("CUST_ID", idList[0].getColumn().getName());
                Basic[] basicList = entity.getAttributes().getBasic();
                assertEquals(2, basicList.length);
                assertEquals("age", basicList[0].getName());
                assertFalse(basicList[0].isOptional());
                assertEquals("AGE", basicList[0].getColumn().getName());
                assertEquals("name", basicList[1].getName());
                assertTrue(basicList[1].isOptional());
                assertEquals("CUST_NAME", basicList[1].getColumn().getName());
                assertEquals(255, basicList[1].getColumn().getLength());
                assertFalse(basicList[1].getColumn().isNullable());
                // test Employee
                entity = getEntityByName(entityList, "Employee");
                assertNotNull(entity);
                return expectedResult;
            }
        });
        assertSame(expectedResult, result);
    }
}
