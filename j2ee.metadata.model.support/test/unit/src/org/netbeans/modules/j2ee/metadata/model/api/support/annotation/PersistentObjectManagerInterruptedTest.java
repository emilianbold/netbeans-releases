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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerInterruptedTest extends PersistenceTestCase {
    
    public PersistentObjectManagerInterruptedTest(String name) {
        super(name);
    }
    
    public void testInterrupted() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                // first checking that the manager does not (for any reason) initialize temporarily
                ObjectProviderImpl provider = new ObjectProviderImpl(false);
                PersistentObjectManager<PersistentObject> manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertFalse(manager.temporary);
                // now checking that the manager initializes temporarily when ObjectProvider.createInitialObjects throws InterruptedException
                provider = new ObjectProviderImpl(true);
                manager = helper.createPersistentObjectManager(provider);
                manager.getObjects();
                assertTrue(manager.temporary);
            }
        });
    }
    
    private static final class ObjectProviderImpl implements ObjectProvider<PersistentObject> {
        
        private final boolean interruptible;

        public ObjectProviderImpl(boolean interruptible) {
            this.interruptible = interruptible;
        }

        public List<PersistentObject> createInitialObjects() throws InterruptedException {
            if (interruptible) {
                throw new InterruptedException();
            } else {
                return Collections.emptyList();
            }
        }

        public List<PersistentObject> createObjects(TypeElement type) {
            throw new UnsupportedOperationException();
        }

        public boolean modifyObjects(TypeElement type, List<PersistentObject> objects) {
            throw new UnsupportedOperationException();
        }
    }
}
