/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.jpa.jpql.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.spi.IJPAVersion;
import org.eclipse.persistence.jpa.jpql.spi.IManagedType;
import org.eclipse.persistence.jpa.jpql.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.spi.IPlatform;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;

/**
 *
 * @author sp153251
 */
public class ManagedTypeProvider implements IManagedTypeProvider {

    private final Project project;
    private Map<String, IManagedType> managedTypes;
    private ITypeRepository typeRepository;
    private final EntityMappings mappings;
    private boolean valid = true;//used to conrol long tasks, if not valid long tasks should be either terminated or goes short way

    public ManagedTypeProvider(Project project, EntityMappingsMetadata metaData) {
        this.project = project;
        this.mappings = metaData.getRoot();
    }
    
    public ManagedTypeProvider(Project project, EntityMappings mappings) {
        this.project = project;
        this.mappings = mappings;
    }
    
    @Override
    public Iterable<IEntity> abstractSchemaTypes() {
        initializeManagedTypes();
        Collection<IEntity> abstractSchemaTypes = null;
        ManagedTypeVisitor visitor = new ManagedTypeVisitor();
        for (IManagedType managedType : managedTypes.values()) {
            managedType.accept(visitor);
        }
        abstractSchemaTypes = visitor.getEntities();
        return Collections.unmodifiableCollection(abstractSchemaTypes);
    }

    @Override
    public IManagedType getManagedType(IType itype) {
        initializeManagedTypes();
        for (IManagedType mt : managedTypes.values()) {
            if (isValid() && mt.getType().equals(itype)) {
                return mt;
            }
        }
        return null;
    }

    @Override
    public IManagedType getManagedType(String name) {
        initializeManagedTypes();
        return managedTypes.get(name);
    }

    @Override
    public IPlatform getPlatform() {
        return IPlatform.JAVA;//TODO, at first step always java?
    }

    @Override
    public ITypeRepository getTypeRepository() {
        if (typeRepository == null) {
            typeRepository = new TypeRepository(project, this);
        }
        return typeRepository;
    }

    @Override
    public IJPAVersion getVersion() {
        String version = PersistenceUtils.getJPAVersion(project);
        if(version == null || version.startsWith("1"))return IJPAVersion.VERSION_2_0;
        else return IJPAVersion.VERSION_1_0;
    }

    @Override
    public Iterable<IManagedType> managedTypes() {
        initializeManagedTypes();
        return Collections.unmodifiableCollection(managedTypes.values());
    }
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * make model invalid and it shoul case processing to stop, minimize etc.
     * results with SPI may not be consider valid if provider isn't valid
     */
    public void invalidate() {
        valid = false;
        //TODO: may have sense to clean stored data
    }

    private void initializeManagedTypes() {
        if (managedTypes == null) {
            managedTypes = new HashMap<String, IManagedType>();
            //TODO: not only entities but mapped superclasses and embeddable?
            for (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity persistentType : mappings.getEntity()) {

                if (persistentType != null) {
                    String name = persistentType.getName();

                    if (managedTypes.containsKey(name)) {
                        continue;
                    }

                    managedTypes.put(name, new Entity(persistentType, this));
                }
            }
            for (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable persistentType : mappings.getEmbeddable()) {

                if (persistentType != null) {
                    String name = persistentType.getClass2();

                    if (managedTypes.containsKey(name)) {
                        continue;
                    }

                    managedTypes.put(name, new Embeddable(persistentType, this));
                }
            }
            for (org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass persistentType : mappings.getMappedSuperclass()) {

                if (persistentType != null) {
                    String name = persistentType.getClass2();

                    if (managedTypes.containsKey(name)) {
                        continue;
                    }

                    managedTypes.put(name, new MappedSuperclass(persistentType, this));
                }
            }
        }
    }
}
