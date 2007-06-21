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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

/**
 *
 * @author Andrei Badea
 */
public class ManyToManyImplTest extends EntityMappingsTestCase {

    public ManyToManyImplTest(String testName) {
        super(testName);
    }

    public void testManyToOne() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   private int id;" +
                "   @ManyToMany(cascade = CascadeType.ALL)" +
                "   private List<bar.Project> projectList;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "bar/Project.java",
                "package bar;" +
                "import java.util.*;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Project {" +
                "   @Id()" +
                "   private int id;" +
                "   @ManyToMany(cascade = CascadeType.ALL, mappedBy = \"projectList\")" +
                "   private List<foo.Employee> employeeList;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity customer = getEntityByName(metadata.getRoot().getEntity(), "Employee");
                ManyToMany manyToMany = customer.getAttributes().getManyToMany(0);
                assertEquals("projectList", manyToMany.getName());
                assertEquals("bar.Project", manyToMany.getTargetEntity());
                assertNotNull(manyToMany.getCascade().getCascadeAll());
                assertEquals("LAZY", manyToMany.getFetch());
                Entity project = getEntityByName(metadata.getRoot().getEntity(), "Project");
                manyToMany = project.getAttributes().getManyToMany(0);
                assertEquals("employeeList", manyToMany.getName());
                assertEquals("foo.Employee", manyToMany.getTargetEntity());
                assertNotNull(manyToMany.getCascade().getCascadeAll());
                assertEquals("LAZY", manyToMany.getFetch());
                return null;
            }
        });
    }
}
