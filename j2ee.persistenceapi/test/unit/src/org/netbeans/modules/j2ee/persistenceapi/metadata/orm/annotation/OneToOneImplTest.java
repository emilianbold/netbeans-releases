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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 *
 * @author Andrei Badea
 */
public class OneToOneImplTest extends EntityMappingsTestCase {

    public OneToOneImplTest(String testName) {
        super(testName);
    }

    public void testOneToOne() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   private int id;" +
                "   @OneToOne()" +
                "   private bar.Address address;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "bar/Address.java",
                "package bar;" +
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Address {" +
                "   @Id()" +
                "   private int id;" +
                "   @OneToOne(cascade = CascadeType.ALL, mappedBy = \"address\", optional = false)" +
                "   private foo.Customer customer;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity address = getEntityByName(metadata.getRoot().getEntity(), "Address");
                OneToOne oneToOne = address.getAttributes().getOneToOne(0);
                assertEquals("customer", oneToOne.getName());
                assertEquals("address", oneToOne.getMappedBy());
                assertEquals("foo.Customer", oneToOne.getTargetEntity());
                assertNotNull(oneToOne.getCascade().getCascadeAll());
                assertFalse(oneToOne.isOptional());
                assertEquals("EAGER", oneToOne.getFetch());
                return null;
            }
        });
    }
}
