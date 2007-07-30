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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.PropertyParser;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 *
 * @author Petr Hrebejk, Adam Sotona
 */
public class J2MEProjectProperties implements ProjectProperties {
    
    
    
    // Special properties of the project
    public static final String J2ME_PROJECT_NAME = "j2me.project.name"; //NOI18N
    
    public static final String PROP_CONFIGURATIONS = "configurations"; //NOI18N
    
    public static final String CONFIG_PREFIX = "configs."; //NOI18N
    
    private static final String LIBS="${libs.";

    // Info about the property destination
    private final Set<PropertyDescriptor> PROPERTY_DESCRIPTORS = new HashSet<PropertyDescriptor>();
    
    private void initPropertyDescriptors() {
        for (ProjectPropertiesDescriptor p : Lookup.getDefault().lookup(new Lookup.Template<ProjectPropertiesDescriptor>(ProjectPropertiesDescriptor.class)).allInstances() ) {
            PROPERTY_DESCRIPTORS.addAll(p.getPropertyDescriptors());
        }
        for (DeploymentPlugin p : Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class)).allInstances() ) {
            final Iterator it2 = p.getProjectPropertyDefaultValues().entrySet().iterator();
            while (it2.hasNext()) {
                final Map.Entry en = (Map.Entry)it2.next();
                final Object v = en.getValue();
                final PropertyParser  par = v instanceof Boolean ? DefaultPropertyParsers.BOOLEAN_PARSER : 
                                            v instanceof Integer ? DefaultPropertyParsers.INTEGER_PARSER : 
                                            v instanceof String ? DefaultPropertyParsers.STRING_PARSER : 
                                            v instanceof File ? DefaultPropertyParsers.FILE_REFERENCE_PARSER : null;
                if (par != null) PROPERTY_DESCRIPTORS.add(new PropertyDescriptor((String)en.getKey(), true, par, v.toString()));
            }
        }
    }
    
    // Private fields ----------------------------------------------------------
    
    private Project project;                
    
    protected ReferenceHelper refHelper;
    protected AntProjectHelper antProjectHelper;
    protected HashMap<String,PropertyInfo> properties;
    protected ProjectConfigurationsHelper configHelper;
    protected ProjectConfiguration devConfigs[]; 
    
    public J2MEProjectProperties( Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, ProjectConfigurationsHelper configHelper) {
        this.project = project;
        this.properties = new HashMap<String,PropertyInfo>();
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        this.configHelper = configHelper;
        initPropertyDescriptors();
        read();
    }
    
    public synchronized void setActiveConfiguration(final ProjectConfiguration cfg) {
        put(DefaultPropertiesDescriptor.CONFIG_ACTIVE, cfg == null || configHelper.getDefaultConfiguration().equals(cfg)? "" : cfg.getDisplayName()); //NOI18N
    }
    
    public synchronized ProjectConfiguration getActiveConfiguration() {
        final String cfg = (String)get(DefaultPropertiesDescriptor.CONFIG_ACTIVE);
        if (devConfigs == null || cfg == null || cfg.length() == 0) return configHelper.getDefaultConfiguration();
        for (int i=0; i<devConfigs.length; i++)
            if (cfg.equals(devConfigs[i].getDisplayName())) return devConfigs[i];
        return configHelper.getDefaultConfiguration();
    }
    
    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */
    static String getAntPropertyName( final String property ) {
        if ( property != null &&
                property.startsWith( "${" ) && // NOI18N
                property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 );
        } 
        return property;
    }
    
    AntProjectHelper getHelper() {
        return antProjectHelper;
    }
    
    private PropertyDescriptor findPropertyDescriptor(final String propertyName) {
        for (PropertyDescriptor pd : PROPERTY_DESCRIPTORS ) {
            if (pd.getName().equals(propertyName) || (propertyName.startsWith(CONFIG_PREFIX) && propertyName.endsWith('.' + pd.getName())))
                return pd;
        }
        return null;
    }
    
    public Object put( final String propertyName, final Object value ) {
        PropertyInfo pi = properties.get( propertyName );
        if (pi == null) {
            // new configuration property clone appeard
            final PropertyDescriptor pd = findPropertyDescriptor(propertyName);
            assert pd != null : "Unknown property " + propertyName; // NOI18N
            // not necessary to init when it is goint to be changed just now
            pi = new PropertyInfo(pd.clone(propertyName), null);
            properties.put(propertyName, pi);
        }
        final Object oldVal = pi.getValue();
        pi.setValue( value );
        return oldVal;
    }
    
    void putPropertyRawValue( final Object propertyName, final String rawValue) {
        PropertyInfo pi = properties.get( propertyName );
        if (pi == null) {
            // new configuration property clone appeard
            final PropertyDescriptor pd = findPropertyDescriptor((String)propertyName);
            assert pd != null : "Unknown property " + propertyName; // NOI18N
            // not necessary to init when it is goint to be changed just now
            pi = new PropertyInfo(pd.clone((String)propertyName), null);
            properties.put((String)propertyName, pi);
        }
        pi.setRawValue( rawValue );
    }
    
    public Object get(final Object propertyName) {
        final PropertyInfo pi = properties.get( propertyName );
        return pi==null? null : pi.getValue();
    }
    
    public String getPropertyRawValue(final Object propertyName) {
        final PropertyInfo pi = properties.get( propertyName );
        return pi==null? null : pi.getRawValue();
    }
    
    public boolean isModified( final String propertyName ) {
        final PropertyInfo pi = properties.get( propertyName );
        assert pi != null : "Unknown property " + propertyName; // NOI18N
        return pi.isModified();
    }
    
    public ProjectConfiguration[] getConfigurations() {
        return devConfigs;
    }
    
    final public synchronized void setConfigurations(final ProjectConfiguration[] configurations) {
        this.devConfigs = configurations;
    }
    
    public List<String> getAllIdentifiers() {
        final ArrayList<String> l = new ArrayList<String>();
        for (int i=0; i<devConfigs.length; i++) {
            l.add(devConfigs[i].getDisplayName());
            final Map<String,Object> abs = (Map<String,Object>)get(configHelper.getDefaultConfiguration().equals(devConfigs[i]) ? DefaultPropertiesDescriptor.ABILITIES : CONFIG_PREFIX + devConfigs[i].getDisplayName() + '.' + DefaultPropertiesDescriptor.ABILITIES);
            if (abs != null) l.addAll(abs.keySet());
        }
        return l;
    }
    
    public FileObject getProjectDirectory() {
        return project.getProjectDirectory();
    }
    
    public FileObject getSourceRoot() {
        return antProjectHelper.resolveFileObject(antProjectHelper.getStandardPropertyEvaluator().getProperty("src.dir")); //NOI18N
    }
    
    /** Reads all the properties of the project and converts them to objects
     * suitable for usage in the GUI controls.
     */
    private void read() {
        // Read the properties from the project
        EditableProperties sharedProps = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProps = antProjectHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final ProjectConfiguration cfgs[] = configHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
        final ProjectConfiguration confs[] = new ProjectConfiguration[cfgs.length];
        System.arraycopy(cfgs, 0, confs, 0, cfgs.length);
        setConfigurations(confs);
        // Initialize the property map with objects
        properties.put(J2ME_PROJECT_NAME, new PropertyInfo(new PropertyDescriptor(J2ME_PROJECT_NAME, true, DefaultPropertyParsers.STRING_PARSER), ProjectUtils.getInformation(project).getDisplayName()));
        for (PropertyDescriptor pd:PROPERTY_DESCRIPTORS) {
            EditableProperties ep = pd.isShared() ? sharedProps : privateProps;
            String raw = ep.getProperty( pd.getName());
            properties.put( pd.getName(), new PropertyInfo( pd, raw == null ? pd.getDefaultValue() : raw));
            for (int j=0; j<devConfigs.length; j++) {
                final PropertyDescriptor clone = pd.clone(CONFIG_PREFIX + devConfigs[j].getDisplayName() + '.' + pd.getName());
                raw = ep.getProperty(clone.getName());
                if (raw != null) {
                    properties.put(clone.getName(), new PropertyInfo(clone, raw));
                }
            }
        }
    }
    
    /** Transforms all the Objects from GUI controls into String Ant
     * properties and stores them in the project
     */
    public void store() {
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                public Object run()  {
                    
                    resolveProjectDependencies();
                    
                    // Some properties need special handling e.g. if the
                    // property changes the project.xml files
                    for(final PropertyInfo pi:properties.values()) {
                        pi.encode();
                    }
                    
                    final ProjectConfiguration configs[] = configHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
                    final HashSet<ProjectConfiguration> newConfigs = new HashSet<ProjectConfiguration>(Arrays.asList(devConfigs));
                    for (int i=0; i<configs.length; i++) {
                        if (!newConfigs.remove(configs[i])) {
                            configHelper.removeConfiguration(configs[i]);
                        }
                    }
                    for (ProjectConfiguration cfg:newConfigs) {
                        configHelper.addConfiguration(cfg.getDisplayName());
                    }
                    
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    EditableProperties sharedProps = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = antProjectHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

                    // Set the changed properties
                    for( final PropertyInfo pi:properties.values()) {
                        final PropertyDescriptor pd = pi.getPropertyDescriptor();
                        if (pd != null && pi.isModified()) {
                            final String newValueEncoded = pi.getNewValueEncoded();
                            if ( newValueEncoded != null ) {
                                (pd.isShared() ? sharedProps : privateProps).setProperty( pd.getName(), newValueEncoded );
                            } else {
                                // remove property
                                (pd.isShared() ? sharedProps : privateProps).remove(pd.getName());
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, sharedProps);
                    antProjectHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    
                    // Invoke this method to perform cyclic dependencies check and potentionally show warning dilalog
                    CustomizerGeneral cg = new CustomizerGeneral();
                    cg.initValues(J2MEProjectProperties.this, null);
                    cg.getSortedSubprojectsList();
                    
                    //storing global default encoding by dcurrent project (see issue #97855)
                    String enc = sharedProps.getProperty(DefaultPropertiesDescriptor.JAVAC_ENCODING);
                    if (enc != null) FileEncodingQuery.setDefaultEncoding(Charset.forName(enc));
                    
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify(e.getException());
        }
        
    }
    
    /** Finds out what are new and removed project dependencies and
     * applyes the info to the project
     */
    protected void resolveProjectDependencies() {
        // Create a set of old and new artifacts.
        final Set<String> oldReferences = new HashSet<String>();
        final Set<String> newReferences = new HashSet<String>();
        for (PropertyInfo pi:properties.values()) {
            if (pi != null) {
                if (pi.getPropertyDescriptor().getPropertyParser() == DefaultPropertyParsers.PATH_PARSER) {
                    // Get original artifacts
                    final List oldList = (List)pi.getOldValue();
                    if ( oldList != null ) {
                        final Iterator it = oldList.iterator();
                        while (it.hasNext()) oldReferences.add(((VisualClassPathItem)it.next()).getRawText());
                    }
                    
                    // Get artifacts after the edit
                    final List newList = (List)pi.getValue();
                    if ( newList != null ) {
                        final Iterator it = newList.iterator();
                        while (it.hasNext()) newReferences.add(((VisualClassPathItem)it.next()).getRawText());
                    }
                } else if (pi.getPropertyDescriptor().getPropertyParser() == DefaultPropertyParsers.FILE_REFERENCE_PARSER) {
                    oldReferences.add(pi.getOldRawValue());
                    newReferences.add(pi.getRawValue());
                }
            }
        }
        
        // Create set of removed artifacts and remove them
        final Set<String> removed = new HashSet<String>( oldReferences );
        removed.removeAll( newReferences );
        final Set<String> added = new HashSet<String>(newReferences);
        added.removeAll(oldReferences);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for ( String reference : removed ) {
            if (reference != null && !reference.startsWith(LIBS)) { //NOI18N
                refHelper.destroyReference(reference);
            }
        }
        
        // 2. now read project.properties and modify rest
        final EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        boolean changed = false;
        
        for( final String reference:removed) {
            if (reference != null && reference.startsWith(LIBS)) { //NOI18N
                // remove helper property pointing to library jar if there is any
                ep.remove(reference.substring(2, reference.length()-1));
                changed = true;
            }
        }
        final File projDir = FileUtil.toFile(antProjectHelper.getProjectDirectory());
        for( String reference:added ) {
            if (reference != null && reference.startsWith(LIBS)) { //NOI18N
                // add property to project.properties pointing to relativized
                // library jar(s) if possible
                reference = reference.substring(2, reference.length()-1);
                final String value = relativizeLibraryClasspath(reference, projDir);
                if (value != null) {
                    ep.setProperty(reference, value);
                    ep.setComment(reference, new String[]{
                        NbBundle.getMessage(J2MEProjectProperties.class, "DESC_J2MEProps_CommentLine1", reference), //NOI18N
                        NbBundle.getMessage(J2MEProjectProperties.class, "DESC_J2MEProps_CommentLine2")}, false); //NOI18N
                    changed = true;
                }
            }
        }
        if (changed) {
            antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        
    }
    
    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param property library property name ala "libs.someLib.classpath"
     * @param projectDir project dir for relativization
     * @return relativized library classpath or null if some jar is not collocated
     */
    private String relativizeLibraryClasspath(final String property, final File projectDir) {
        final String value = PropertyUtils.getGlobalProperties().getProperty(property);
        if (value == null)
            return null;
        final String[] paths = PropertyUtils.tokenizePath(value);
        final StringBuffer sb = new StringBuffer();
        for (int i=0; i<paths.length; i++) {
            final File f = antProjectHelper.resolveFile(paths[i]);
            if (CollocationQuery.areCollocated(f, projectDir)) {
                sb.append(PropertyUtils.relativizeFile(projectDir, f));
            } else {
                return null;
            }
            if (i+1<paths.length) {
                sb.append(File.pathSeparatorChar);
            }
        }
        if (sb.length() == 0) {
            return null;
        } 
        return sb.toString();
    }
    
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    public boolean containsKey(final Object key) {
        return properties.containsKey(key);
    }
    
    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }
    
    public Set entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }
    
    
    public boolean isEmpty() {
        return properties.isEmpty();
    }
    
    public Set<String> keySet() {
        return Collections.unmodifiableSet(properties.keySet());
    }
    
    public void putAll(@SuppressWarnings("unused")
	final Map t) {
        throw new UnsupportedOperationException();
    }
    
    public Object remove(final Object key) {
        return containsKey(key) ? put((String)key, null) : null;
    }
    
    public int size() {
        return properties.size();
    }
    
    public Collection values() {
        return Collections.unmodifiableCollection(properties.values());
    }
    
    private class PropertyInfo {
        
        final protected PropertyDescriptor propertyDesciptor;
        final private String rawValue;
        final private Object value;
        private Object newValue;
        private String newValueEncoded;
        private boolean modified;
        
        public PropertyInfo( PropertyDescriptor propertyDesciptor, String rawValue) {
            this.propertyDesciptor = propertyDesciptor;
            this.rawValue = rawValue;
            this.value = rawValue==null ? null : propertyDesciptor.getPropertyParser().decode( rawValue, antProjectHelper, refHelper );
            this.newValue = null;
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {
            if ( isModified() && newValue != null ) {
                newValueEncoded = propertyDesciptor.getPropertyParser().encode( newValue, antProjectHelper, refHelper);
            } else {
                newValueEncoded = null;
            }
        }
        
        public Object getValue() {
            return isModified() ? newValue : value;
        }
        
        public String getRawValue() {
            encode();
            return isModified() ? newValueEncoded : rawValue;
        }
        
        public void setValue( final Object value ) {
            newValue = value;
            modified = true;
        }
        
        public void setRawValue(final String rawValue) {
            setValue(rawValue==null ? null : propertyDesciptor.getPropertyParser().decode( rawValue, antProjectHelper, refHelper ));
        }
        
        public String getNewValueEncoded() {
            return newValueEncoded;
        }
        
        public boolean isModified() {
            return modified;
        }
        
        public Object getOldValue() {
            return value;
        }
        
        public String getOldRawValue() {
            return rawValue;
        }
    }
}
