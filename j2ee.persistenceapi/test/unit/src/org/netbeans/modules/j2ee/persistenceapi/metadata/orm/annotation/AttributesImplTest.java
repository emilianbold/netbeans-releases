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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class AttributesImplTest extends EntityMappingsTestCase {

    public AttributesImplTest(String testName) {
        super(testName);
    }

    public void testId() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   @Column(name = \"CUST_ID\")" +
                "   private int id;" +
                "   @Id()" +
                "   private int id2" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity entity = metadata.getRoot().getEntity(0);
                Id[] idList = entity.getAttributes().getId();
                assertEquals(2, idList.length);
                assertEquals("id", idList[0].getName());
                assertEquals("CUST_ID", idList[0].getColumn().getName());
                assertEquals("id2", idList[1].getName());
                assertEquals("ID2", idList[1].getColumn().getName());
                return null;
            }
        });
    }
}
