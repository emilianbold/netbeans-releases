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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.persistence.spi.PersistenceProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsTestCase extends JavaSourceTestCase {

    public EntityMappingsTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        URL root = FileUtil.getArchiveRoot(PersistenceProvider.class.getProtectionDomain().getCodeSource().getLocation());
        addCompileRoots(Collections.singletonList(root));
    }

    /**
     * Used to allow other classes in this package which are not subclasses
     * of this one to call tearDown() directly.
     */
    protected void tearDown() {
        super.tearDown();
    }

    protected MetadataModel<EntityMappingsMetadata> createModel() throws IOException, InterruptedException {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        return EntityMappingsMetadataModelFactory.createMetadataModel(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE));
    }

    protected static Entity getEntityByName(Entity[] entityList, String name) {
        for (Entity entity : entityList) {
            if (name.equals(entity.getName())) {
                return entity;
            }
        }
        return null;
    }
}
