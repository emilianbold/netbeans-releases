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

package org.netbeans.modules.j2ee.persistence.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Properties;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property;
import org.netbeans.modules.j2ee.persistence.unit.*;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Not final, more or less just space for all provider specific properties handling.
 * Some kind of provider plugins needs to be done in this area
 * @author Martin Adamek
 */
public class ProviderUtil {
    
    // known providers
    public static final Provider HIBERNATE_PROVIDER = new HibernateProvider();
    public static final Provider TOPLINK_PROVIDER = new ToplinkProvider();
    public static final Provider KODO_PROVIDER = new KodoProvider();
    public static final Provider DEFAULT_PROVIDER = new DefaultProvider();
    
    /**
     * All known provider implementations.
     */
    private static final Provider[] PROVIDERS = new Provider[]{HIBERNATE_PROVIDER, TOPLINK_PROVIDER, KODO_PROVIDER};
    
    private ProviderUtil() {
    }
    
    /**
     * @return the provider that given library represents or null if
     *  there was no matching provider.
     */
    public static Provider getProvider(Library library){
        
        for (int i = 0; i < PROVIDERS.length; i++) {
            if (org.netbeans.modules.j2ee.common.Util.containsClass(library, PROVIDERS[i].getProviderClass())){
                return PROVIDERS[i];
            }
        }
        return null;
    }
    
    /**
     * @return the provider that given providerClass represents or null
     *  if there was no matching provider.
     */
    public static Provider getProvider(String providerClass, Project project){
        
        if (null == providerClass || "".equals(providerClass.trim())){
            return getContainerManagedProvider(project);
        }
        
        for (int i = 0; i < PROVIDERS.length; i++) {
            if (PROVIDERS[i].getProviderClass().equals(providerClass.trim())){
                return PROVIDERS[i];
            }
        }
        return DEFAULT_PROVIDER;
        
    }
    
    
    /**
     * @return the default container managed provider for given project. If no specific
     * provider can be resolved, returns {@link DefaultProvider}, never null.
     * @throws NullPointerException if the given project was null.
     */
    public static Provider getContainerManagedProvider(Project project){
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceId = j2eeModuleProvider != null ? j2eeModuleProvider.getServerInstanceID() : null;
        if (serverInstanceId == null) {
            return DEFAULT_PROVIDER;
        }
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
        if (platform == null) {
            return DEFAULT_PROVIDER;
        }
        if (platform.getSupportedSpecVersions(j2eeModuleProvider.getJ2eeModule().getModuleType()).contains(J2eeModule.JAVA_EE_5)) {
            if ("J2EE".equals(j2eeModuleProvider.getServerID())) {
                // should be GlassFish
                return TOPLINK_PROVIDER;
            } else if ("JBoss4".equals(j2eeModuleProvider.getServerID())){
                return HIBERNATE_PROVIDER;
            }
        }
        return DEFAULT_PROVIDER;
    }
    
    public static DatabaseConnection getConnection(PersistenceUnit pu) {
        if (pu == null || pu.getProperties() == null){
            return null;
        }
        
        String url = null;
        String driver = null;
        String username = null;
        Property[] properties = pu.getProperties().getProperty2();
        Provider provider = getProvider(pu);
        
        for (int i = 0; i < properties.length; i++) {
            String key = properties[i].getName();
            if (key == null){
                continue;
            }
            if (key.equals(provider.getJdbcUrl())) {
                url = properties[i].getValue();
            } else if (key.equals(provider.getJdbcDriver())) {
                driver = properties[i].getValue();
            } else if (key.equals(provider.getJdbcUsername())) {
                username = properties[i].getValue();
            }
        }
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        
        for (int i = 0; i < connections.length; i++) {
            DatabaseConnection c = connections[i];
            // password is problematic, when it is returned?
            if (c.getDatabaseURL().equals(url) &&
                    c.getDriverClass().equals(driver) &&
                    c.getUser().equals(username)) {
                return c;
            }
        }
        return null;
    }
    
    public static String getDatasourceName(PersistenceUnit pu) {
        String datasourceName = pu.getJtaDataSource();
        if (datasourceName == null) {
            datasourceName = pu.getNonJtaDataSource();
        }
        return datasourceName;
    }
    
    /**
     * @return the library in which given persistence unit's provider
     * is defined, or null none could be found.
     */
    public static Library getLibrary(PersistenceUnit pu) {
        return getLibrary(getProvider(pu));
    }
    
    /**
     * @return the library in which given provider
     * is defined, or null none could be found.
     */
    public static Library getLibrary(Provider provider){
        List libraries = createLibraries();
        if (provider != null) {
            for (Iterator it = libraries.iterator(); it.hasNext();) {
                Library library = (Library) it.next();
                if (provider.getProviderClass().equals(extractProvider(library))) {
                    return library;
                }
            }
        }
        return null;
        
    }
    
    /**
     * Sets given table generation strategy for given persistence unit.
     * @param persistenceUnit
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code>
     * @project the project of the given persistence unit
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Project project) {
        String providerClass = persistenceUnit.getProvider();
        Provider provider = ProviderUtil.getProvider(providerClass, project);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }
    
    /**
     * Sets given table generation strategy for given persistence unit.
     * @param persistenceUnit
     * @param tableGenerationStrategy the strategy to set, see constants in <code>Provider</code>
     * @provider the provider whose table generation property will be used.
     */
    public static void setTableGeneration(PersistenceUnit persistenceUnit, String tableGenerationStrategy, Provider provider){
        Property tableGenerationProperty = provider.getTableGenerationProperty(tableGenerationStrategy);
        Properties properties = persistenceUnit.getProperties();
        if (properties == null) {
            properties = persistenceUnit.newProperties();
            persistenceUnit.setProperties(properties);
        }
        
        Property existing = getProperty(properties.getProperty2(), provider.getTableGenerationPropertyName());
        
        if (existing != null && tableGenerationProperty == null){
            properties.removeProperty2(existing);
        } else if (existing != null && tableGenerationProperty != null){
            existing.setValue(tableGenerationProperty.getValue());
        } else if (tableGenerationProperty != null){
            properties.addProperty2(tableGenerationProperty);
        }
        
    }
    
    /**
     * Sets provider, connection and table generation strategy to given persistence unit. Note
     * that given persistence unit's possibly existing provider's properties are not preserved
     * with the exception of the database connection properties. In other words, you have to explicitly set for
     * example table generation strategy for persistence unit after changing provider.
     * @param persistenceUnit
     * @param provider the provider to set.
     * @connection the connection to set.
     * @tableGenerationStrategy the table generation strategy to set.
     */
    public static void setProvider(PersistenceUnit persistenceUnit, Provider provider,
            DatabaseConnection connection, String tableGenerationStrategy){
        
        removeProviderProperties(persistenceUnit);
        persistenceUnit.setProvider(provider.getProviderClass());
        setDatabaseConnection(persistenceUnit, connection);
        setTableGeneration(persistenceUnit, tableGenerationStrategy, provider);
    }
    
    
    /**
     * Removes provider specific properties from given persistence unit. More
     * specifically, removes properties of persistence unit's current provider,
     * should be called before setting new provider for persistence unit.
     */
    public static void removeProviderProperties(PersistenceUnit persistenceUnit){
        Provider old = getProvider(persistenceUnit);
        Property[] properties = getProperties(persistenceUnit);
        
        if (old != null){
            for (int i = 0; i < properties.length; i++) {
                Property each = properties[i];
                if (old.getPropertyNames().contains(each.getName())){
                    persistenceUnit.getProperties().removeProperty2(each);
                }
            }
        }
        persistenceUnit.setProvider(null);
        
    }
    
    
    public static PersistenceUnit buildPersistenceUnit(String name, Provider provider, DatabaseConnection connection) {
        PersistenceUnit persistenceUnit = new PersistenceUnit();
        persistenceUnit.setName(name);
        persistenceUnit.setProvider(provider.getProviderClass());
        Properties properties = persistenceUnit.newProperties();
        Map connectionProperties = provider.getConnectionPropertiesMap(connection);
        for (Iterator it = connectionProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) connectionProperties.get(propertyName));
            properties.addProperty2(property);
        }
        
        Map defaultProperties = provider.getDefaultVendorSpecificProperties();
        for (Iterator it = defaultProperties.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            Property property = properties.newProperty();
            property.setName(propertyName);
            property.setValue((String) defaultProperties.get(propertyName));
            properties.addProperty2(property);
        }
        
        persistenceUnit.setProperties(properties);
        return persistenceUnit;
    }
    
    
    /**
     * Sets properties of given connection to given persistence unit.
     */
    public static void setDatabaseConnection(PersistenceUnit persistenceUnit, DatabaseConnection connection){
        
        Provider provider = getProvider(persistenceUnit);
        Property[] properties = getProperties(persistenceUnit);
        
        Map<String, String> propertiesMap = provider.getConnectionPropertiesMap(connection);
        
        for (String name : propertiesMap.keySet()) {
            Property property = getProperty(properties, name);
            if (property == null){
                
                if (persistenceUnit.getProperties() == null){
                    persistenceUnit.setProperties(persistenceUnit.newProperties());
                }
                
                property = persistenceUnit.getProperties().newProperty();
                property.setName(name);
                persistenceUnit.getProperties().addProperty2(property);
            }
            
            String value = propertiesMap.get(name);
            // value must be present (setting null would cause
            // value attribute to not be present)
            if (value == null){
                value = "";
            }
            property.setValue(value);
        }
    }
    
    /**
     * Gets properties of given persistence unit. If properties of
     * given unit was null, will return an empty array.
     * @return array of properties, empty if given units properties was null.
     */
    public static Property[] getProperties(PersistenceUnit persistenceUnit){
        if (persistenceUnit.getProperties() != null){
            return persistenceUnit.getProperties().getProperty2();
        }
        return new Property[0];
    }
    /**
     * @return Property from given properties whose name matches given propertyName
     * or null if given properties didn't contain property with matching name.
     */
    private static Property getProperty(Property[] properties, String propertyName){
        
        if (null == properties){
            return null;
        }
        
        for (int i = 0; i < properties.length; i++) {
            Property each = properties[i];
            if (each.getName() != null && each.getName().equals(propertyName)){
                return each;
            }
        }
        
        return null;
    }
    
    /**
     * @return Property from given persistence unit whose name matches given propertyName
     * or null if given persistence unit didn't contain property with matching name.
     */
    public static Property getProperty(PersistenceUnit persistenceUnit, String propertyName){
        if (persistenceUnit.getProperties() == null){
            return null;
        }
        return getProperty(persistenceUnit.getProperties().getProperty2(), propertyName);
    }
    
    /**
     * @return provider of given persistence unit. In case that no specific
     * provider can be resolved <code>DEFAULT_PROVIDER</code> will be returned.
     */
    public static Provider getProvider(PersistenceUnit persistenceUnit){
        for (int i = 0; i < PROVIDERS.length; i++) {
            if (PROVIDERS[i].getProviderClass().equals(persistenceUnit.getProvider())){
                return PROVIDERS[i];
            }
        }
        return DEFAULT_PROVIDER;
    }
    
    /**
     *@return true if the given puDataObject is not null and its document is
     * parseable, false otherwise.
     */
    public static boolean isValid(PUDataObject puDataObject){
        return null == puDataObject ? false : puDataObject.parseDocument();
    }
    
    /**
     * @return persistence units specified in the given puDataObject.
     */
    public static PersistenceUnit[] getPersistenceUnits(PUDataObject puDataObject){
        if (puDataObject.getPersistence() == null){
            return new PersistenceUnit[0];
        }
        return puDataObject.getPersistence().getPersistenceUnit();
    }
    
    /**
     * Renames given managed class in given persistence unit.
     * @param persistenceUnit the unit that contains the class to be renamed.
     * @param newName the new name of the class.
     * @param oldName the name of the class to be renamed.
     * @param dataObject
     *
     */
    public static void renameManagedClass(PersistenceUnit persistenceUnit, String newName,
            String oldName, PUDataObject dataObject){
        
        dataObject.removeClass(persistenceUnit, oldName);
        dataObject.addClass(persistenceUnit, newName);
        
    }
    
    /**
     * Removes given managed class from given persistence unit.
     * @param persistenceUnit the persistence unit from which the given class
     * is to be removed.
     * @param clazz fully qualified name of the class to be removed.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void removeManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject){
        
        dataObject.removeClass(persistenceUnit, clazz);
    }
    
    /**
     * Adds given managed class to given persistence unit.
     * @param persistenceUnit the persistence unit to which the given class
     * is to be added.
     * @param clazz fully qualified name of the class to be added.
     * @param dataObject the data object representing persistence.xml.
     */
    public static void addManagedClass(PersistenceUnit persistenceUnit, String clazz,
            PUDataObject dataObject){
        
        dataObject.addClass(persistenceUnit, clazz);
    }
    
    
    /**
     * Adds the given <code>persistenceUnit</code> to the <code>PUDataObject<code>
     *  of the given <code>project</code> and saves it.
     * @param persistenceUnit the unit to be added
     * @param project the project to which the unit is to be added.
     * @throws InvalidPersistenceXmlException if the given project has an invalid persistence.xml file.
     * 
     */
    public static void addPersistenceUnit(PersistenceUnit persistenceUnit, Project project) throws InvalidPersistenceXmlException{
        PUDataObject pud = getPUDataObject(project);
        pud.addPersistenceUnit(persistenceUnit);
        pud.save();
    }
    
    /**
     *Gets the <code>PUDataObject</code> associated with the given <code>fo</code>.
     *@param fo the file object thas has an associated <code>PUDataObject</code>. Must
     * not be null.
     *@return <code>PUDataObject</code> associated with given <code>fo</code>; never null.
     *@throws IllegalArgumentException if the given <code>fo</code> is null.
     *@throws InvalidPersistenceXmlException if the given file object represents
     * an invalid persistence.xml file.
     */
    public static PUDataObject getPUDataObject(FileObject fo) throws InvalidPersistenceXmlException{
        if (fo == null){
            throw new IllegalArgumentException("Called PUDataObject#getPUDataObject with null FileObject param"); //NO18N
        }
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        if (!(dataObject instanceof PUDataObject)){
            throw new InvalidPersistenceXmlException(FileUtil.getFileDisplayName(fo));
        }
        return (PUDataObject) dataObject;
    }
    
    /**
     * Gets the PUDataObject associated with the given <code>project</code>. If there
     * was no PUDataObject (i.e. no persistence.xml) in the project, a new one
     * will be created. Use
     * {@link #getDDFile} for testing whether a project has a persistence.xml file.
     *@param project the project whose PUDataObject is to be get.
     *@return <code>PUDataObject</code> associated with the given project; never null.
     * @throws InvalidPersistenceXmlException if the given <code>project</code> had an existing
     * invalid persitence.xml file.
     */
    public static PUDataObject getPUDataObject(Project project) throws InvalidPersistenceXmlException{
        FileObject puFileObject = getDDFile(project);
        if (puFileObject == null) {
            puFileObject = createPersistenceDDFile(project);
        }
        return getPUDataObject(puFileObject);
    }
    
    /**
     * Creates a new FileObject representing file that defines
     * persistence units (<tt>persistence.xml</tt>). <i>Todo: move somewhere else?</i>
     * @return FileObject representing <tt>persistence.xml</tt>.
     */
    private static FileObject createPersistenceDDFile(Project project){
        final FileObject[] dd = new FileObject[1];
        try {
            final FileObject persistenceLocation = PersistenceLocation.createLocation(project);
            // must create the file using AtomicAction, see #72058
            persistenceLocation.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() {
                    try {
                        dd[0] = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(
                                "org-netbeans-modules-j2ee-persistence/persistence-1.0.xml"), persistenceLocation, "persistence"); //NOI18N
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return dd[0];
    }
    
    /**
     * Checks whether given project has a persistence.xml that contains at least one
     * persistence unit.
     * @project the project
     * @return true if given project has a persistence.xml containing
     * at least one persitence unit, false otherwise.
     * @throws InvalidPersistenceXmlException if the given <code>project</code> has an 
     *  invalid persistence.xml file.
     */
    public static boolean persistenceExists(Project project) throws InvalidPersistenceXmlException{
        if (getDDFile(project) == null){
            return false;
        }
        PUDataObject pud = getPUDataObject(project);
        return pud.getPersistence().getPersistenceUnit().length > 0;
    }
    /**
     * @return persistence.xml descriptor of first MetadataUnit found on project or null if none found
     */
    public static FileObject getDDFile(Project project){
        PersistenceScope[] persistenceScopes = PersistenceUtils.getPersistenceScopes(project);
        for (int i = 0; i < persistenceScopes.length; i++) {
            return persistenceScopes[i].getPersistenceXml();
        }
        return null;
    }
    
    public static List<Library> createLibraries() {
        List<Library> providerLibs = new ArrayList<Library>();
        Library[] libs = LibraryManager.getDefault().getLibraries();
        for (int i = 0; i < libs.length; i++) {
            if (org.netbeans.modules.j2ee.common.Util.containsClass(libs[i], "javax.persistence.EntityManager") && (extractProvider(libs[i]) != null)) {
                providerLibs.add(libs[i]);
            }
        }
        Collections.sort(providerLibs, new Comparator() {
            public int compare(Object o1, Object o2) {
                assert (o1 instanceof Library) && (o2 instanceof Library);
                String name1 = ((Library)o1).getDisplayName();
                String name2 = ((Library)o2).getDisplayName();
                return name1.compareToIgnoreCase(name2);
            }
        });
        return providerLibs;
    }
    
    /**
     * @return list of providers that are defined in the IDE's libraries.
     */
    public static List<Provider> getProvidersFromLibraries() {
        List<Provider> providerLibs = new ArrayList<Provider>();
        Library[] libs = LibraryManager.getDefault().getLibraries();
        for (int i = 0; i < libs.length; i++) {
            if (org.netbeans.modules.j2ee.common.Util.containsClass(libs[i], "javax.persistence.EntityManager") && (extractProvider(libs[i]) != null)) {
                providerLibs.add(getProvider(libs[i]));
            }
        }
        Collections.sort(providerLibs, new Comparator() {
            public int compare(Object o1, Object o2) {
                String name1 = ((Provider)o1).getDisplayName();
                String name2 = ((Provider)o2).getDisplayName();
                return name1.compareToIgnoreCase(name2);
            }
        });
        
        return providerLibs;
    }
    
    /**
     * @return the first library in the IDE's libraries which contains
     * a persistence provider.
     */
    public static Library getFirstProviderLibrary() {
        List<Library> libraries = createLibraries();
        if (libraries.size() > 0) {
            return libraries.iterator().next();
        }
        return null;
    }
    
    private static String extractProvider(Library library) {
        for (int i = 0; i < PROVIDERS.length; i++) {
            if (org.netbeans.modules.j2ee.common.Util.containsClass(library, PROVIDERS[i].getProviderClass())){
                return PROVIDERS[i].getProviderClass();
            }
        }
        return null;
    }
    
    
    
    
    /** Returns array of providers known to the IDE.
     */
    public static Provider[] getAllProviders() {
        return new Provider[]{DEFAULT_PROVIDER, TOPLINK_PROVIDER, HIBERNATE_PROVIDER, KODO_PROVIDER};
    }
    
    /**
     * Makes given persistence unit portable if possible, i.e. removes provider class from it.
     * A persistence unit may be made portable if it uses container's default provider, it doesn't
     * specicify any properties and it is not defined in Java SE environment.
     * @param project the project in which the given persistence unit is defined. Must not be null.
     * @param persistenceUnit the persistence unit to be made portable. Must not be null.
     * @return true if given persistence unit could be made portable, false otherwise.
     * @throws NullPointerException if either project or persistenceUnit was null.
     */
    public static boolean makePortableIfPossible(Project project, PersistenceUnit persistenceUnit){
        
        if (Util.isJavaSE(project)){
            return false;
        }
        
        Provider defaultProvider = getContainerManagedProvider(project);
        
        if (defaultProvider.getProviderClass().equals(persistenceUnit.getProvider())
                && persistenceUnit.getProperties().sizeProperty2() == 0){
            
            persistenceUnit.setProvider(null);
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks whether the given <code>project</code>'s target server is present.
     *
     * @param project the project whose target server's presence is checked; must not be null.
     * @return true if the given <code>project</code> has its target server present or
     *  if the project does not need a target server (i.e. it is not a J2EE project), false otherwise.
     * @throws NullPointerException if the given <code>project</code> was null.
     */
    public static boolean isValidServerInstanceOrNone(Project project){
        if (project == null){
            throw new NullPointerException("Passed null project paramater to ProviderUtil#isValidServerInstanceOrNone");
        }
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            // not a J2EE project
            return true;
        }
        return org.netbeans.modules.j2ee.common.Util.isValidServerInstance(j2eeModuleProvider);
    }
}
