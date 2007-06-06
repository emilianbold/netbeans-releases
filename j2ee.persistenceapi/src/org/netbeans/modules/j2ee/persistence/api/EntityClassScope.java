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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.api;

import java.util.Iterator;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistenceapi.EntityClassScopeAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Describes an entity class scope, which is basically a bunch of related
 * entity classes on a classpath.
 *
 * @author Andrei Badea
 * @since 1.3
 */
public final class EntityClassScope {

    private static final Lookup.Result<EntityClassScopeProvider> providers =
            Lookup.getDefault().lookupResult(EntityClassScopeProvider.class);

    private final EntityClassScopeImplementation impl;

    static {
        EntityClassScopeAccessor.DEFAULT = new EntityClassScopeAccessor() {
            public EntityClassScope createEntityClassScope(EntityClassScopeImplementation impl) {
                return new EntityClassScope(impl);
            }
        };
    }

    /**
     * Returns the entity class scope for the given file (the entity classes
     * surrounding the given file).
     *
     * @param  fo the file for which to find the entity class scope; cannot be null.
     *
     * @return the entity class scope for the given file or null if there is no
     *         entity class scope.
     *
     * @throws NullPointerException if the fo parameter was null.
     */
    public static EntityClassScope getEntityClassScope(FileObject fo) {
        if (fo == null) {
            throw new NullPointerException("Passed null to EntityClassScope.getEntityClassScope(FileObject)"); // NOI18N
        }
        for (EntityClassScopeProvider provider : providers.allInstances()) {
            EntityClassScope entityClassScope = provider.findEntityClassScope(fo);
            if (entityClassScope != null) {
                return entityClassScope;
            }
        }
        return null;
    }

    private EntityClassScope(EntityClassScopeImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the metadata model describing the entity classes in this 
     * entity class scope.
     * 
     * @param  withDeps <code>true</code> if the returned model needs to contain
     *         both the entity classes defined in Java sources and those defined
     *         on the compilation classpath of those sources, <code>false</code>
     *         if the model should only contain the entity classes defined
     *         in Java sources.
     * 
     * @return an entity class model; never null.
     */
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
        return impl.getEntityMappingsModel(withDeps);
    }
}
