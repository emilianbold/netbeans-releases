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
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelImpl implements MetadataModelImplementation<EntityMappingsMetadata> {

    private final AnnotationModelHelper helper;
    private final EntityMappingsImpl root;
    private final EntityMappingsMetadata metadata;

    public EntityMappingsMetadataModelImpl(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        ClasspathInfo cpi = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        helper = AnnotationModelHelper.create(cpi);
        root = new EntityMappingsImpl(helper);
        metadata = new EntityMappingsMetadataImpl(root);
    }

    public <R> R runReadAction(final MetadataModelAction<EntityMappingsMetadata, R> action) throws IOException {
        return helper.userActionTask(new Callable<R>() {
            public R call () throws Exception {
                return action.run(metadata);
            }
        });
    }
}
