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

package org.netbeans.modules.j2ee.persistence.dd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceClassPathProvider;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.WeakListeners;

/**
 *
 * @author Marek Fukala, Andrei Badea
 */
public class PersistenceUtils {
    
    // TODO multiple mapping files
    
    /**
     * Maps persistence.xml files to instances of EntityMappingCache, which
     * cache MetadataUnits for persistence units.
     */
    private static final Map<FileObject, EntityMappingsCache> persistence2EMCache = new WeakHashMap<FileObject, EntityMappingsCache>();
    
    /**
     * Maps projects to MetadataUnit containing the classpath with entity
     * classes for those projects.
     */
    private static final WeakHashMap<Project, MetadataUnit> project2MUCache = new WeakHashMap<Project, MetadataUnit>();
    
    private PersistenceUtils() {
    }
    
    /**
     * Returns an EntityMappings instance containing the entity classes in
     * the given persistence unit of the given persistence scope.
     *
     * @return an EntityMapping instance or null if e.g. the given persistence
     *         scope does not contain a persistence.xml file or the
     *         given persistence unit.
     * @throws NullPointerException if <code>persistenceScope</code> or
     *         <code>persistenceUnitName</code> are null.
     *         IOException if an I/O exception occured, e.g. if the persistence.xml
     *         file could not be read.
     */
    public static EntityMappings getEntityMappings(PersistenceScope persistenceScope, String persistenceUnitName) throws IOException {
        if (persistenceScope == null) {
            throw new NullPointerException("The persistenceScope parameter cannot be null"); // NOI18N
        }
        if (persistenceUnitName == null) {
            throw new NullPointerException("The persistenceUnitName parameter cannot be null"); // NOI18N
        }
        
        EntityMappingsCache emCache = getEntityMappingsCache(persistenceScope);
        if (emCache != null) {
            return emCache.getEntityMappings(persistenceUnitName);
        }
        return null;
    }
    
    /**
     * Returns the EntityMappings instances containing the entity classes in
     * all persistence units of all persistence scopes in the given project.
     *
     * @return a list of EntityMappings instances; never null.
     * @throws NullPointerException if <code>project</code> is null.
     *         IOException if an I/O exception occured, e.g. if the persistence.xml
     *         file could not be read.
     */
    public static List<EntityMappings> getEntityMappings(Project project) throws IOException {
        if (project == null) {
            throw new NullPointerException("The project parameter cannot be null"); // NOI18N
        }
        
        List<EntityMappings> result = new ArrayList<EntityMappings>();
        for (PersistenceScope persistenceScope : getPersistenceScopes(project)) {
            EntityMappingsCache emCache = getEntityMappingsCache(persistenceScope);
            if (emCache != null) {
                result.addAll(emCache.getEntityMappings());
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Returns an EntityMappingsCache for the given persistence.xml file.
     * Not private because used in tests.
     *
     * @return an EntityMappingCache instance; can be null e.g. when
     *         the given persistence.xml file is not
     *         {@link FileObject#isValid valid}.
     */
    static EntityMappingsCache getEntityMappingsCache(PersistenceScope persistenceScope) throws IOException {
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml == null) {
            return null;
        }
        
        EntityMappingsCache emCache = null;
        synchronized (persistence2EMCache) {
            emCache = persistence2EMCache.get(persistenceXml);
            if (emCache == null && persistenceXml.isValid()) {
                emCache = EntityMappingsCache.create(persistenceScope);
                persistence2EMCache.put(persistenceXml, emCache);
            }
        }
        return emCache;
    }
    
    /**
     * Removes the given persistence.xml file from the cache of
     * EntityMappingCache instances.
     */
    private static void removeFromCache(FileObject persistenceXml) {
        synchronized (persistence2EMCache) {
            persistence2EMCache.remove(persistenceXml);
        }
    }
    
    /**
     * Returns the set of entity classes in the given persistence scope as
     * defined by the persistence.xml file of that persistence scope.
     *
     * @return a set of entity classes; never null.
     * @throws NullPointerException if <code>persistenceScope</code> is null.
     *         IOException if an I/O exception occured, e.g. if the persistence.xml
     *         file could not be read.
     */
    public static Set<Entity> getEntityClasses(PersistenceScope persistenceScope) throws IOException {
        if (persistenceScope == null) {
            throw new NullPointerException("The persistenceScope parameter cannot be null"); // NOI18N
        }
        
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml == null) {
            return Collections.emptySet();
        }
        
        Set<Entity> result = new HashSet<Entity>();
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXml);
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            EntityMappings entityMappings = getEntityMappings(persistenceScope, persistenceUnit.getName());
            if (entityMappings == null) {
                continue;
            }
            
            for (Entity entity : entityMappings.getEntity()) {
                result.add(entity);
            }
        }
        
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns the set of entity classes in the given project as defined by
     * the persistence units in the project's persistence.xml file(s). If
     * there are duplicate entity classes (according to their name), only
     * the first entity class will be contained in the returned set.
     *
     * @return a set of entity classes; never null.
     * @throws NullPointerException if <code>project</code> is null.
     *         IOException if an I/O exception occured, e.g. if the persistence.xml
     *         file could not be read.
     */
    public static Set<Entity> getEntityClasses(Project project) throws IOException {
        if (project == null) {
            throw new NullPointerException("The project parameter cannot be null"); // NOI18N
        }
        
        Set<Entity> result = new HashSet<Entity>();
        Set<String> entityNames = new HashSet<String>();
        for (PersistenceScope persistenceScope : getPersistenceScopes(project)) {
            // issue 79891: leaving out Entity instances with duplicate names
            for (Entity entity : getEntityClasses(persistenceScope)) {
                String entityName = entity.getName();
                if (!entityNames.contains(entityName)) {
                    result.add(entity);
                    entityNames.add(entityName);
                }
            }
        }
        
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns the set of entity classes in the given list of EntityMappings.
     * If there are duplicate entity classes (according to their name), only
     * the first entity class will be contained in the returned set.
     *
     * @return a set of entity classes; never null.
     * @throws NullPointerException if <code>project</code> is null.
     */
    public static Set<Entity> getEntityClasses(List<EntityMappings> entityMappingsList) {
        if (entityMappingsList == null) {
            throw new NullPointerException("The entityMappingsList parameter cannot be null"); // NOI18N
        }
        
        Set<Entity> result = new HashSet<Entity>();
        Set<String> entityNames = new HashSet<String>();
        for (EntityMappings em : entityMappingsList) {
            // issue 79891: leaving out Entity instances with duplicate names
            for (Entity entity : em.getEntity()) {
                String entityName = entity.getName();
                if (!entityNames.contains(entityName)) {
                    result.add(entity);
                    entityNames.add(entityName);
                }
            }
        }
        
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns an EntityMappings instance containing the entity classes in the given project
     * defined by annotations. The return value
     * will not contain entity mappings specified in ORM files. It may
     * overlap with entity mappings contained in the return value of
     * the {@link #getEntityMappings(PersistenceScope, String)}.
     *
     * @return an EntityMappings instance; never null.
     * @throws NullPointerException if <code>project</code> is null.
     */
    public static EntityMappings getAnnotationEntityMappings(Project project) {
        if (project == null) {
            throw new NullPointerException("The project parameter cannot be null"); // NOI18N
        }
        
        try {
            return ORMMetadata.getDefault().getRoot(getPersistenceMetadataUnit(project));
        } catch (IOException e) {
            // should not occur, that is why we are catching here
            // but anyway...
            return new org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.EntityMappings();
        }
    }
    
    /**
     * Returns the set of entity classes in the given project
     * defined by annotations. The return value
     * will not contain entity classes specified in ORM files. It may
     * overlap with entity classes contained in the return value of
     * the {@link #getEntityClasses(Project)}.
     *
     * @return a set of entity classes; never null.
     * @throws NullPointerException if <code>project</code> is null.
     */
    public static Set<Entity> getAnnotationEntityClasses(Project project) {
        Set<Entity> result = new HashSet<Entity>();
        for (Entity entity : getAnnotationEntityMappings(project).getEntity()) {
            result.add(entity);
        }
        return result;
    }
    
    /**
     * Returns a MetadataUnit that represents entity classes in a project as
     * specified by the {@org.netbeans.modules.j2ee.persistence.spi.PersistenceClassPathProvider}
     * interface.
     *
     * @param project project where metadata units should be found
     * @return a single metadata unit
     */
    private static MetadataUnit getPersistenceMetadataUnit(Project project) {
        MetadataUnit mu;
        synchronized (project2MUCache) {
            mu = project2MUCache.get(project);
            if (mu == null) {
                ClassPath classpath = null;
                PersistenceClassPathProvider provider = (PersistenceClassPathProvider)project.getLookup().lookup(PersistenceClassPathProvider.class);
                if (provider != null) {
                    classpath = provider.getClassPath();
                }
                mu = new WeakMetadataUnit(null, classpath);
                project2MUCache.put(project, mu);
            }
        }
        return mu;
    }
    
    /**
     * Returns the persistence unit(s) the given entity class belongs to. Since
     * an entity class can belong to any persistence unit, this returns all
     * persistence units in all persistence.xml files in the project which owns
     * the given entity class.
     *
     * @return an array of PersistenceUnit's; never null.
     * @throws NullPointerException if <code>sourceFile</code> is null.
     */
    public static PersistenceUnit[] getPersistenceUnits(FileObject sourceFile) throws IOException {
        if (sourceFile == null) {
            throw new NullPointerException("The sourceFile parameter cannot be null"); // NOI18N
        }
        
        Project project = FileOwnerQuery.getOwner(sourceFile);
        if (project == null) {
            return new PersistenceUnit[0];
        }
        
        List<PersistenceUnit> result = new ArrayList<PersistenceUnit>();
        for (PersistenceScope persistenceScope : getPersistenceScopes(project)) {
            Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
            for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                result.add(persistenceUnit);
            }
        }
        
        return (PersistenceUnit[])result.toArray(new PersistenceUnit[result.size()]);
    }
    
    /**
     * Returns the entity mappings corresponding to sibling entity classes
     * of the passed entity class.
     *
     * <p>This is not doable correctly through
     * PersistenceScope since the list of sibling entity classes is given
     * by the persistence unit(s) that the entity class is referenced in,
     * and we don't know which persistence unit to choose. Thus we resort
     * to looking just in the entity classes defined in annotations.</p>
     *
     * @return an instance of EntityMappings; can be null if e.g. the
     *         passed entity class is not owned by a project.
     * @throws NullPointerException if <code>sourceFile</code> is null.
     */
    public static EntityMappings getEntityMappings(FileObject sourceFile) {
        if (sourceFile == null) {
            throw new NullPointerException("The sourceFile parameter cannot be null"); // NOI18N
        }
        
        Project project = FileOwnerQuery.getOwner(sourceFile);
        if (project == null) {
            return null;
        }
        
        return getAnnotationEntityMappings(project);
    }
    
    /**
     * Sorts a set of entity classes using the entity names as the key.
     *
     * @return the list of sorted entities.
     * @throws NullPointerException if <code>entityClasses</code> is null.
     *         IOException if an I/O exception occured, e.g. if the persistence.xml
     *         file could not be read.
     */
    public static List<Entity> sortEntityClasses(Set<Entity> entityClasses) {
        if (entityClasses == null) {
            throw new NullPointerException("The entityClasses parameter cannot be null"); // NOI18N
        }
        List<Entity> entityList = new ArrayList(entityClasses);
        Collections.sort(entityList, new EntityComparator());
        return entityList;
    }
    
    
    /**
     * Searches the given entity mappings for the specified entity class.
     *
     * @param  className the Java class to search for.
     * @param  entityMappings the entity mappings to be searched.
     * @return the entity class or null if it could not be found.
     * @throws NullPointerException if <code>className</code> or
     *         <code>entityMappings</code> were null.
     */
    public static Entity getEntity(String className, EntityMappings entityMappings) {
        if (className == null) {
            throw new NullPointerException("The javaClass parameter cannot be null"); // NOI18N
        }
        if (entityMappings == null) {
            throw new NullPointerException("The entityMappings parameter cannot be null"); // NOI18N
        }
        
        for (Entity entity : entityMappings.getEntity()) {
            if (className.equals(entity.getClass2())) {
                return entity;
            }
        }
        return null;
    }
    
    /**
     * Returns an array containing all persistence scopes provided by the
     * given project. This is just an utility method which does:
     *
     * <pre>
     * PersistenceScopes.getPersistenceScopes(project).getPersistenceScopes();
     * </pre>
     *
     * <p>with all the necessary checks for null (returning an empty
     * array in this case).</p>
     *
     * @param  project the project to retrieve the persistence scopes from.
     * @return the list of persistence scopes provided by <code>project</code>;
     *         or an empty array if the project provides no persistence
     *         scopes; never null.
     * @throws NullPointerException if <code>project</code> was null.
     */
    public static PersistenceScope[] getPersistenceScopes(Project project) {
        if (project == null) {
            throw new NullPointerException("The project parameter cannot be null"); // NOI18N
        }
        
        PersistenceScopes persistenceScopes = PersistenceScopes.getPersistenceScopes(project);
        if (persistenceScopes != null) {
            return persistenceScopes.getPersistenceScopes();
        }
        return new PersistenceScope[0];
    }
    
    /**
     * Maintains a cache of metadata units for persistence units in a persistence scope.
     * Not private because used in tests.
     */
    static final class EntityMappingsCache implements PropertyChangeListener {
        
        private final WeakReference<PersistenceScope> persistenceScopeRef;
        
        private final Map<String, WeakMetadataUnit> metadataUnitCache = new HashMap<String, WeakMetadataUnit>();
        
        /**
         * Creates a new EntityMappingsCache. A factory method is needed in order
         * to avoid starting listening in the constructor, making the newly
         * created object available to other classes before the constructor
         * finished.
         */
        public static EntityMappingsCache create(PersistenceScope persistenceScope) {
            EntityMappingsCache result = new EntityMappingsCache(persistenceScope);
            result.startListening();
            return result;
        }
        
        private EntityMappingsCache(PersistenceScope persistenceScope) {
            persistenceScopeRef = new WeakReference<PersistenceScope>(persistenceScope);
        }
        
        private void startListening() {
            PUDataObject pudo = getPUDataObject();
            if (pudo != null) {
                pudo.addPropertyChangeListener(WeakListeners.propertyChange(this, pudo));
            }
        }
        
        public EntityMappings getEntityMappings(String persistenceUnitName) throws IOException {
            MetadataUnit mu = getMetadataUnit(persistenceUnitName);
            if (mu != null) {
                return ORMMetadata.getDefault().getRoot(mu);
            } else {
                return null;
            }
        }
        
        /**
         * Returns the list of EntityMappings (one for each persistence unit).
         */
        public List<EntityMappings> getEntityMappings() throws IOException {
            PUDataObject pudo = getPUDataObject();
            if (pudo == null) {
                return Collections.emptyList();
            }
            
            List<EntityMappings> entityMappingsList = new ArrayList<EntityMappings>();
            
            Persistence persistence = pudo.getPersistence();
            synchronized (this) {
                for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                    EntityMappings em = getEntityMappings(persistenceUnit.getName());
                    if (em != null) {
                        entityMappingsList.add(em);
                    }
                }
            }
            
            return Collections.unmodifiableList(entityMappingsList);
        }
        
        /**
         * Finds a metadata unit for a persistence unit, creating a new one if
         * not found in cache. Not private because of tests.
         */
        MetadataUnit getMetadataUnit(String persistenceUnitName) {
            PersistenceScope persistenceScope = persistenceScopeRef.get();
            if (persistenceScope == null) {
                return null;
            }
            
            WeakMetadataUnit metadataUnit = null;
            synchronized (this) {
                metadataUnit = metadataUnitCache.get(persistenceUnitName);
                if (metadataUnit == null) {
                    PUDataObject pudo = getPUDataObject();
                    if (pudo != null) {
                        metadataUnit = createMetadataUnit(pudo, persistenceScope.getClassPath(), persistenceUnitName);
                        if (metadataUnit != null) {
                            metadataUnitCache.put(persistenceUnitName, metadataUnit);
                        }
                    }
                }
            }
            
            return metadataUnit;
        }
        
        private WeakMetadataUnit createMetadataUnit(PUDataObject pudo, ClassPath classPath, String persistenceUnitName) {
            assert Thread.holdsLock(this);
            
            Persistence persistence = pudo.getPersistence();
            for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                if (persistenceUnitName.equals(unit.getName())) {
                    return new WeakMetadataUnit(getMappingFile(pudo), classPath);
                }
            }
            return null;
        }
        
        /**
         * Called when the persistence.xml file changed. Removes the metadata
         * units corresponding to removed persistence units and refreshes the
         * contents of remaining metadata units based on the contents of
         * persistence.xml.
         */
        private void refreshCache() {
            PUDataObject pudo = getPUDataObject();
            if (pudo == null) {
                return;
            }
            
            Persistence persistence = pudo.getPersistence();
            synchronized (this) {
                Set<String> persistenceUnitNames = new HashSet<String>();
                for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                    persistenceUnitNames.add(persistenceUnit.getName());
                }
                
                // remove persistence units no longer in persistence.xml
                for (Iterator<Map.Entry<String, WeakMetadataUnit>> i = metadataUnitCache.entrySet().iterator(); i.hasNext();) {
                    if (!persistenceUnitNames.contains(i.next().getKey())) {
                        i.remove();
                    }
                }
                
                // refresh the remaining persistence units
                for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                    // if (!metadataUnitCache.containsKey(persistenceUnit.get))
                    WeakMetadataUnit metadataUnit = metadataUnitCache.get(persistenceUnit.getName());
                    if (metadataUnit == null) {
                        // we don't cache a MU for this PU yet
                        continue;
                    }
                    
                    refreshMetadataUnit(metadataUnit, pudo);
                }
            }
        }
        
        private static void refreshMetadataUnit(WeakMetadataUnit metadataUnit, PUDataObject pudo) {
            metadataUnit.setDeploymentDescriptor(getMappingFile(pudo));
        }
        
        private static FileObject getMappingFile(PUDataObject pudo) {
            // XXX for now there can only be one deployment descriptor in MetadataUnit
            // using META-INF/orm.xml for that and ignoring mapping-file elements in the persistence unit
            
            FileObject persistenceXml = pudo.getPrimaryFile();
            FileObject ormXml = null;
            if (persistenceXml != null) {
                FileObject metaInf = persistenceXml.getParent();
                if (metaInf != null) {
                    ormXml = metaInf.getFileObject("orm.xml"); // NOI18N
                }
            }
            return ormXml;
        }
        
        PUDataObject getPUDataObject() {
            PersistenceScope persistenceScope = persistenceScopeRef.get();
            FileObject persistenceXml = null;
            if (persistenceScope != null) {
                persistenceXml = persistenceScope.getPersistenceXml();
            }
            
            if (persistenceXml == null) {
                return null;
            }
            
            try {
                return (PUDataObject)DataObject.find(persistenceXml);
            } catch (DataObjectNotFoundException e) {
                return null;
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                removeFromCache(((DataObject)evt.getSource()).getPrimaryFile());
            } else {
                refreshCache();
            }
        }
    }
    
    /**
     * Compares entity classes lexicographically by fully qualified names.
     */
    private static final class EntityComparator implements Comparator<Entity> {
        
        public int compare(Entity o1, Entity o2) {
            String name1 = o1.getClass2();
            String name2 = o2.getClass2();
            if (name1 == null) {
                return name2 == null ? 0 : -1;
            } else {
                return name2 == null ? 1 : name1.compareTo(name2);
            }
        }
    }
}
