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

package org.netbeans.modules.j2ee.persistence.spi.support;

import java.io.File;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelHelper {

    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private File persistenceXml;

    private MetadataModel<EntityMappingsMetadata> model;

    public static EntityMappingsMetadataModelHelper create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        return new EntityMappingsMetadataModelHelper(bootPath, compilePath, sourcePath);
    }

    private EntityMappingsMetadataModelHelper(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
    }

    public synchronized void changePersistenceXml(File newPersistenceXml) {
        persistenceXml = newPersistenceXml;
    }

    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String puName) {
        File persistenceXml;
        synchronized (this) {
            persistenceXml = this.persistenceXml;
        }
        if (persistenceXml == null || FileUtil.toFileObject(persistenceXml) == null) {
            return null;
        }
        // XXX trivial implementation which is not affected by the contents of
        // the persistence unit (i.e., the list of orm.xml files and the list
        // of entity classes)
        return getDefaultEntityMappingsModel(false);
    }
    
    public MetadataModel<EntityMappingsMetadata> getDefaultEntityMappingsModel(boolean withDeps) {
        synchronized (this) {
            if (model == null) {
                model = EntityMappingsMetadataModelFactory.createMetadataModel(bootPath, compilePath, sourcePath);
            }
            return model;
        }
    }
}
