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

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;

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
                "import java.util.*;" +
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
                "   @Temporal(TemporalType.DATE)" +
                "   private Date birthDate;" +
                "   @Temporal(TemporalType.TIME)" +
                "   private Date birthTime;" +
                "   @Version()" +
                "   @Column(name = \"VER\", nullable = false)" +
                "   private int version;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "import java.util.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   @Column(name=\"EMP_ID\")" +
                "   @Temporal(TemporalType.TIMESTAMP)" +
                "   private Date id;" +
                "   @Version()" +
                "   @Temporal(TemporalType.DATE)" +
                "   private Date entryDate;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/DateBasedId.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "import java.util.*;" +
                "@Embeddable()" +
                "public class DateBasedId {" +
                "   @Temporal(TemporalType.DATE)" +
                "   private Date date;" +
                "   @Column(name=\"ORDER_NUM\")" +
                "   private int order;" +
                "}");
        final String expectedResult = "foo";
        String result = createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, String>() {
            public String run(EntityMappingsMetadata metadata) {
                EntityMappings entityMappings = metadata.getRoot();
                // test entities
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
                assertNull(idList[0].getTemporal());
                Basic[] basicList = entity.getAttributes().getBasic();
                assertEquals(4, basicList.length);
                assertEquals("age", basicList[0].getName());
                assertFalse(basicList[0].isOptional());
                assertEquals("AGE", basicList[0].getColumn().getName());
                assertNull(basicList[0].getTemporal());
                assertEquals("name", basicList[1].getName());
                assertTrue(basicList[1].isOptional());
                assertEquals("CUST_NAME", basicList[1].getColumn().getName());
                assertEquals(255, basicList[1].getColumn().getLength());
                assertFalse(basicList[1].getColumn().isNullable());
                assertNull(basicList[1].getTemporal());
                assertEquals("DATE", basicList[2].getTemporal());
                assertEquals("TIME", basicList[3].getTemporal());
                Version[] versionList = entity.getAttributes().getVersion();
                assertEquals(1, versionList.length);
                assertEquals("version", versionList[0].getName());
                assertEquals("VER", versionList[0].getColumn().getName());
                // test Employee
                entity = getEntityByName(entityList, "Employee");
                idList = entity.getAttributes().getId();
                assertEquals(1, idList.length);
                assertEquals("TIMESTAMP", idList[0].getTemporal());
                versionList = entity.getAttributes().getVersion();
                assertEquals(1, versionList.length);
                assertEquals("entryDate", versionList[0].getName());
                assertEquals("DATE", versionList[0].getTemporal());
                // test embeddables
                Embeddable[] embeddableList = entityMappings.getEmbeddable();
                assertEquals(1, embeddableList.length);
                // test Address
                Embeddable embeddable = getEmbeddableByClass(embeddableList, "foo.DateBasedId");
                basicList = embeddable.getAttributes().getBasic();
                assertEquals(2, basicList.length);
                assertEquals("date", basicList[0].getName());
                assertEquals("DATE", basicList[0].getTemporal());
                assertEquals("order", basicList[1].getName());
                assertEquals("ORDER_NUM", basicList[1].getColumn().getName());
                return expectedResult;
            }
        });
        assertSame(expectedResult, result);
    }
}
