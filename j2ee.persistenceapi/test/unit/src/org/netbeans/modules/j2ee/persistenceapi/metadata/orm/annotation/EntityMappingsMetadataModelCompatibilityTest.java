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

import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelCompatibilityTest;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelCompatibilityTest extends MetadataModelCompatibilityTest {

    private TestDelegate delegate;

    public EntityMappingsMetadataModelCompatibilityTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        delegate = new TestDelegate(getName());
        delegate.setUp();
    }

    protected void tearDown() throws Exception {
        delegate.tearDown();
        delegate = null;
    }

    protected MetadataModel<?> createModel(boolean ready) {
        return delegate.getModel(ready);
    }

    protected void makeReady(MetadataModel<?> model) {
        delegate.makeModelReady();
    }

    /**
     * To work around the lack of multiple inheritance. We would like
     * EntityMappingsMetadataModelCompatibilityTest
     * to be able to inherit from both EntityMappingsTestCase and
     * MetadataModelCompatibilityTest.
     */
    private static final class TestDelegate extends EntityMappingsTestCase {

        private MetadataModel<EntityMappingsMetadata> model;

        public TestDelegate(String name) {
            super(name);
        }

        public MetadataModel<EntityMappingsMetadata> getModel(boolean ready) {
            if (model == null) {
                try {
                    model = createModel();
                    if (!ready) {
                        startAndBlockClassPathScan();
                    }
                } catch (Exception e) {
                    fail();
                }
            }
            return model;
        }

        public void makeModelReady() {
            resumeClassPathScan();
        }
    }
}
