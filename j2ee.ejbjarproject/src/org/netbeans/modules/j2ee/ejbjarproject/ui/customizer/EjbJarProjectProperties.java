/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.openide.ErrorManager;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Chris Webster
 */
public class EjbJarProjectProperties {
    
    public static final String J2EE_1_4 = "1.4";
    public static final String J2EE_1_3 = "1.3";
    // Special properties of the project
    public static final String EJB_PROJECT_NAME = "j2ee.ejbjarproject.name";
    public static final String JAVA_PLATFORM = "platform.active";
    public static final String J2EE_PLATFORM = "j2ee.platform";
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root";
    public static final String BUILD_FILE = "buildfile";
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir";
    public static final String DIST_JAR = "dist.jar";
    public static final String DIST_EAR_JAR = "dist.ear.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath";
    public static final String DEBUG_CLASSPATH = "debug.classpath";    

    public static final String JAR_NAME = "jar.name";
    public static final String JAR_COMPRESS = "jar.compress";

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source";
    public static final String JAVAC_DEBUG = "javac.debug";
    public static final String JAVAC_DEPRECATION = "javac.deprecation";
    public static final String JAVAC_TARGET = "javac.target";
    public static final String JAVAC_ARGS = "javac.compilerargs";
    public static final String SRC_DIR = "src.dir";
    public static final String TEST_SRC_DIR = "test.src.dir"; // NOI18N
    public static final String META_INF = "meta.inf";
    public static final String RESOURCE_DIR = "resource.dir";
    public static final String BUILD_DIR = "build.dir";
    public static final String BUILD_GENERATED_DIR = "build.generated.dir";
    public static final String BUILD_CLASSES_DIR = "build.classes.dir";
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir";
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";
    
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir";
    public static final String JAVADOC_PRIVATE="javadoc.private";
    public static final String JAVADOC_NO_TREE="javadoc.notree";
    public static final String JAVADOC_USE="javadoc.use";
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar";
    public static final String JAVADOC_NO_INDEX="javadoc.noindex";
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex";
    public static final String JAVADOC_AUTHOR="javadoc.author";
    public static final String JAVADOC_VERSION="javadoc.version";
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle";
    public static final String JAVADOC_ENCODING="javadoc.encoding";
    public static final String JAVADOC_PREVIEW="javadoc.preview";

     // SOURCE ROOTS
    public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    public static final String TEST_ROOTS = "__virtual_test_roots__";   //NOI18N
    
    // Shortcuts 
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    
    private static final String TAG_EJB_MODULE__ADDITIONAL_LIBRARIES = "ejb-module-additional-libraries"; //NOI18N

    private static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private static final PathParser PATH_PARSER = new PathParser();
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    private static final SourceRootsParser SOURCE_ROOTS_PARSER = new SourceRootsParser();
    
    // Info about the property destination
    // XXX only properties which are visually set should be described here
    // XXX refactor this list
    private PropertyDescriptor PROPERTY_DESCRIPTORS[] = {
        new PropertyDescriptor( EJB_PROJECT_NAME, null, STRING_PARSER ),
        new PropertyDescriptor( J2EE_PLATFORM, PROJECT, STRING_PARSER ),
                
        new PropertyDescriptor( SOURCE_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_FILE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_EAR_JAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, PATH_PARSER ),

        new PropertyDescriptor( JAR_NAME, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAR_COMPRESS, PROJECT, BOOLEAN_PARSER ),
        
        new PropertyDescriptor( J2EE_SERVER_TYPE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_SOURCE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER ),       
        new PropertyDescriptor( JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVAC_TARGET, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_ARGS, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( SRC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( RESOURCE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_EAR_CLASSES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
        
        new PropertyDescriptor( JAVADOC_PRIVATE, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_TREE, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_USE, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_NAVBAR, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_INDEX, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_SPLIT_INDEX, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_AUTHOR, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_VERSION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_WINDOW_TITLE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVADOC_ENCODING, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVADOC_PREVIEW, PROJECT, BOOLEAN_PARSER ),
                
        new PropertyDescriptor( SOURCE_ROOTS, null, SOURCE_ROOTS_PARSER),
        new PropertyDescriptor( TEST_ROOTS, null, SOURCE_ROOTS_PARSER),
    };
    
    
    // Private fields ----------------------------------------------------------
    
    private Project project;
    private HashMap properties;    
    private AntProjectHelper antProjectHelper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;

    public EjbJarProjectProperties(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        this.project = project;
        this.properties = new HashMap();
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        this.evaluator = antProjectHelper.getStandardPropertyEvaluator();
        read();                                
    }
    
    static boolean isAntProperty (String string) {
        return string != null && string.startsWith( "${" ) && string.endsWith( "}" ); //NOI18N
    }

    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */    
    public static String getAntPropertyName( String property ) {
        if ( property != null && 
             property.startsWith( "${" ) && // NOI18N
             property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 ); 
        }
        else {
            return property;
        }
    }
    
    public void put( String propertyName, Object value ) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
//        if (JAVAC_CLASSPATH.equals (propertyName)) {
//            assert value instanceof List : "Wrong format of property " + propertyName; //NOI18N
//            writeJavacClasspath ((List) value, antProjectHelper, refHelper);
//        } 
        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        pi.setValue( value );
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }
    
    public Object get( String propertyName ) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
//        if (JAVAC_CLASSPATH.equals (propertyName)) {
//            return readJavacClasspath (antProjectHelper, refHelper);
//        } 

        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        return pi.getValue();
    }
    
    public boolean isModified( String propertyName ) {
        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        return pi.isModified();
    }
    
    public List getSortedSubprojectsList() {
             
        ArrayList subprojects = new ArrayList( 5 );
        addSubprojects( project, subprojects ); // Find the projects recursively
         
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            Project p = (Project)subprojects.get( i );           
            subprojects.set(i, ProjectUtils.getInformation(p).getDisplayName());
        }
        
        // Sort the list
        Collections.sort( subprojects, Collator.getInstance() );
        
        return subprojects;
    }
    
    Project getProject() {
        return project;
    }
    
    /** Gets all subprojects recursively
     */
    private void addSubprojects( Project project, List result ) {
        
        SubprojectProvider spp = (SubprojectProvider)project.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator/*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project sp = (Project)it.next(); 
            if ( !result.contains( sp ) ) {
                result.add( sp );
                addSubprojects( sp, result ); 
            }
        }
    }

    /** Reads all the properties of the project and converts them to objects
     * suitable for usage in the GUI controls.
     */    
    private void read() {
        
        // Read the properties from the project        
        HashMap eProps = new HashMap( 2 );
        eProps.put( PROJECT, antProjectHelper.getProperties( PROJECT ) ); 
        eProps.put( PRIVATE, antProjectHelper.getProperties( PRIVATE ) );
   
        // Initialize the property map with objects
        for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
            if ( pd.dest == null ) {
                // Specialy handled properties
                if ( EJB_PROJECT_NAME.equals( pd.name ) ) {
                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
                    properties.put( pd.name, new PropertyInfo( pd, projectName, projectName ) );            
                }
                else if (SOURCE_ROOTS.equals(pd.name) || TEST_ROOTS.equals(pd.name)) {
                    properties.put (pd.name, new PropertyInfo(pd, pd.name, null));
                }
            }
            else {
                // Standard properties
                String raw = ((EditableProperties)eProps.get( pd.dest )).getProperty( pd.name );
                if ( pd.dest == PRIVATE && raw == null ) {
                    // Can still be found in the project properties
                    raw = ((EditableProperties)eProps.get( PROJECT )).getProperty( pd.name );
                }                
                String eval = evaluator.getProperty(pd.name);
                properties.put( pd.name, new PropertyInfo( pd, raw, eval ) );            
            }
        }
    }
    
    /** Transforms all the Objects from GUI controls into String Ant 
     * properties and stores them in the project
     */    
    public void store() {
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
          
                    resolveProjectDependencies();
                    
                    // Some properties need special handling e.g. if the 
                    // property changes the project.xml files                   
                    for( Iterator it = properties.values().iterator(); it.hasNext(); ) {
                        PropertyInfo pi = (PropertyInfo)it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        pi.encode();
                        String newValueEncoded = pi.getNewValueEncoded();
                        if( pd.dest == null && newValueEncoded != null ) {
                            // Specialy handled properties
                            if (EJB_PROJECT_NAME.equals(pd.name)) {
                                assert false : "No support yet for changing name of EJBProject; cf. EJBProject.setName";
                            }
                            else if ( SOURCE_ROOTS.equals( pd.name ) || TEST_ROOTS.equals( pd.name )) {
                                SourceRoots roots = null;
                                if (SOURCE_ROOTS.equals(pi.rawValue)) {
                                    roots = ((EjbJarProject)project).getSourceRoots();
                                }
                                else if (TEST_ROOTS.equals(pi.rawValue)) {
                                    roots = ((EjbJarProject)project).getTestSourceRoots();
                                }
                                if (roots != null) {
                                    Vector data = ((DefaultTableModel)pi.newValue).getDataVector();
                                    URL[] rootURLs = new URL[data.size()];
                                    String []rootLabels = new String[data.size()];
                                    for (int i=0; i<data.size();i++) {
                                        rootURLs[i] = ((File)((Vector)data.elementAt(i)).elementAt(0)).toURI().toURL();
                                        rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
                                    }
                                    roots.putRoots(rootURLs,rootLabels);
                                }
                            }
                        }   
                        if ( JAVA_PLATFORM.equals( pd.name) && newValueEncoded != null ) {
                            setPlatform( pi.getNewValueEncoded().equals(
                                    JavaPlatformManager.getDefault().getDefaultPlatform().getProperties().get("platform.ant.name")));
                        }
                    }
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    HashMap eProps = new HashMap( 2 );
                    eProps.put( PROJECT, antProjectHelper.getProperties( PROJECT ) ); 
                    eProps.put( PRIVATE, antProjectHelper.getProperties( PRIVATE ) );
        
                     
                    // Set the changed properties
                    for( Iterator it = properties.values().iterator(); it.hasNext(); ) {
                        PropertyInfo pi = (PropertyInfo)it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();                        
                        String newValueEncoded = pi.getNewValueEncoded();
                        if ( newValueEncoded != null ) {                            
                            if ( pd.dest != null ) {
                                // Standard properties
                                ((EditableProperties)eProps.get( pd.dest )).setProperty( pd.name, newValueEncoded );
                                
                                // Standard properties
                                EditableProperties ep = (EditableProperties) eProps.get(pd.dest);
                                if (J2EE_SERVER_INSTANCE.equals(pd.name)) {
                                        // update j2ee.platform.classpath
                                        String oldServInstID = ep.getProperty(J2EE_SERVER_INSTANCE);
                                        if (oldServInstID != null) {
                                            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
                                            if (oldJ2eePlatform != null) {
                                                ((EjbJarProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
                                            }
                                        }
                                        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newValueEncoded);
                                        ((EjbJarProject)project).registerJ2eePlatformListener(j2eePlatform);
                                        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
                                        ep.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);
                                        
                                        // update j2ee.platform.wscompile.classpath
                                        if (j2eePlatform.isToolSupported(WebServicesConstants.WSCOMPILE)) {
                                            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(WebServicesConstants.WSCOMPILE);
                                            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                                                    Utils.toClasspathString(wsClasspath));
                                        } else {
                                            ep.remove(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
                                        }
                                }
                                ep.setProperty(pd.name, newValueEncoded);
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    antProjectHelper.putProperties( PROJECT, (EditableProperties)eProps.get( PROJECT ) );
                    antProjectHelper.putProperties( PRIVATE, (EditableProperties)eProps.get( PRIVATE ) );
                    ProjectManager.getDefault ().saveProject (project);
                    return null;
                }
            });
        } 
        catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        
    }
    
    private void setPlatform( boolean isDefault ) {
        
        Element pcd = antProjectHelper.getPrimaryConfigurationData( true );

        NodeList sps = pcd.getElementsByTagName( "explicit-platform" );
        
        if ( isDefault && sps.getLength() > 0 ) {
            pcd.removeChild( sps.item( 0 ) );
        }
        else if ( !isDefault && sps.getLength() == 0 ) {
            pcd.appendChild( pcd.getOwnerDocument().createElement( "explicit-platform" ) );
        }
         
        antProjectHelper.putPrimaryConfigurationData( pcd, true );
        
    }
    
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
    
        String allPaths[] = { JAVAC_CLASSPATH,  DEBUG_CLASSPATH };
        
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        Set newArtifacts = new HashSet();
        for ( int i = 0; i < allPaths.length; i++ ) {            
            PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );

            // Get original artifacts
            List oldList = (List)pi.getOldValue();
            if ( oldList != null ) {
                for( Iterator it = oldList.iterator(); it.hasNext(); ) {
                    VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
                    if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ) {
                        oldArtifacts.add( vcpi );
                    }
                }
            }
            
            // Get artifacts after the edit
            List newList = (List)pi.getValue();
            if ( newList != null ) {
                for( Iterator it = newList.iterator(); it.hasNext(); ) {
                    VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
                    if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ) {
                        newArtifacts.add( vcpi );
                    }
                }
            }
        }
                
        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            refHelper.destroyForeignFileReference( vcpi.getRaw() );
        }
    }
        
    private class PropertyInfo {
        
        private PropertyDescriptor propertyDesciptor;
        private String rawValue;
        private String evaluatedValue;
        private Object value;
        private Object newValue;
        private String newValueEncoded;
        
        public PropertyInfo( PropertyDescriptor propertyDesciptor, String rawValue, String evaluatedValue ) {
            this.propertyDesciptor = propertyDesciptor;
            this.rawValue = rawValue;
            this.evaluatedValue = evaluatedValue;
            this.value = propertyDesciptor.parser.decode( rawValue, project, antProjectHelper, evaluator, refHelper );
            this.newValue = null;
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {            
            if ( isModified() ) {
                newValueEncoded = propertyDesciptor.parser.encode( newValue, project, antProjectHelper, refHelper);                
            }
            else {
                newValueEncoded = null;
            }
        }

        public Object getValue() {
            return isModified() ? newValue : value; 
        }
        
        public void setValue( Object value ) {
            newValue = value;
        }
        
        public String getNewValueEncoded() {
            return newValueEncoded;
        }
        
        public boolean isModified() {
            return newValue != null;
        }
        
        public Object getOldValue() {
            return value;
        }
    }
    
    private static class PropertyDescriptor {
        
        final PropertyParser parser;
        final String name;
        final String dest;
        
        PropertyDescriptor( String name, String dest, PropertyParser parser ) {
            this.name = name;
            this.dest = dest;
            this.parser = parser;
        }
        
    }
    
    
    private static abstract class PropertyParser {
        
        public abstract Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper);
        
        public abstract String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper);
        
    }
    
    private static class StringParser extends PropertyParser {
        
        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            return raw;
        }        
        
        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            return (String)value;
        }
        
    }
    
    private static class BooleanParser extends PropertyParser {
        
        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            
            if ( raw != null ) {
               String lowecaseRaw = raw.toLowerCase();
               
               if ( lowecaseRaw.equals( "true") || // NOI18N
                    lowecaseRaw.equals( "yes") || // NOI18N
                    lowecaseRaw.equals( "enabled") ) // NOI18N
                   return Boolean.TRUE;                   
            }
            
            return Boolean.FALSE;
        }
        
        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            return ((Boolean)value).booleanValue() ? "true" : "false"; // NOI18N
        }
        
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        
        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            return ((Boolean)super.decode(raw, project, antProjectHelper, evaluator, refHelper)).booleanValue() ? Boolean.FALSE : Boolean.TRUE;
        }
        
        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, project, antProjectHelper, refHelper );
        }
        
    }
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        { JAVAC_CLASSPATH, NbBundle.getMessage( EjbJarProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) },
        { BUILD_CLASSES_DIR, NbBundle.getMessage( EjbJarProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) }
    };
    
    private static class PlatformParser extends PropertyParser {
        
        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();            
            for( int i = 0; i < platforms.length; i++ ) {
                String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name");
                if ( normalizedName != null && normalizedName.equals( raw ) ) {
                    return platforms[i].getDisplayName();
                }
            }

            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName(); 
        }
        
        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
                    new Specification ("j2se",null));
            if (platforms.length == 0)
                return null;
            else
                return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
        }
    }
    
     public static class PathParser extends PropertyParser {
        
         private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            
            String pe[] = PropertyUtils.tokenizePath( raw == null ? "": raw ); // NOI18N
            List cpItems = new ArrayList( pe.length );
            List manifestItems = librariesInDeployment(antProjectHelper);
            for( int i = 0; i < pe.length; i++ ) {
                VisualClassPathItem cpItem;
                boolean inDeployment = manifestItems.contains(getAntPropertyName(pe[i]));
                
                // First try to find out whether the item is well known classpath
                // in the J2SE project type
                int wellKnownPathIndex = -1;
                for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
                    if ( WELL_KNOWN_PATHS[j][0].equals( getAntPropertyName( pe[i] ) ) )  {
                        wellKnownPathIndex = j;
                        break;
                    }
                }
                
                if ( wellKnownPathIndex != - 1 ) {
                    cpItem = new VisualClassPathItem(pe[i], VisualClassPathItem.TYPE_CLASSPATH, pe[i], WELL_KNOWN_PATHS[wellKnownPathIndex][1], true );
                } else if ( pe[i].startsWith( LIBRARY_PREFIX ) ) {
                    // Library from library manager
                    //String eval = antProjectHelper.evaluate( getAntPropertyName( pe[i] ) );
                    String eval = pe[i].substring( LIBRARY_PREFIX.length(), pe[i].lastIndexOf('.') ); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary (eval);
                    if (lib != null) {
                        cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, pe[i], eval, inDeployment );
                    } else {
                        cpItem = new VisualClassPathItem(null, VisualClassPathItem.TYPE_LIBRARY, pe[i], eval, inDeployment);
                    }
                } else if (pe[i].startsWith(ANT_ARTIFACT_PREFIX)) {
                    AntArtifact artifact = refHelper.getForeignFileReferenceAsArtifact(pe[i]);
                    if ( artifact != null ) {
                        // Sub project artifact
                        String eval = artifact.getArtifactLocation().toString();
                        cpItem = new VisualClassPathItem( artifact, VisualClassPathItem.TYPE_ARTIFACT, pe[i], eval, inDeployment );
                    } else {
                        cpItem = new VisualClassPathItem(null, VisualClassPathItem.TYPE_ARTIFACT, pe[i], null, inDeployment);
                    }
                } else {
                    // Standalone jar or property
                    String eval;
                    if (isAntProperty (pe[i])) {
                        eval = evaluator.getProperty(getAntPropertyName(pe[i]));
                    }
                    else {
                        eval = pe[i];
                    }                    
                    File f = null;
                    if (eval != null) {
                        f = antProjectHelper.resolveFile(eval);
                    }                    
                    cpItem = new VisualClassPathItem( f, VisualClassPathItem.TYPE_JAR, pe[i], eval, inDeployment );
                }
                if (cpItem!=null) {
                    cpItems.add( cpItem );
                }
            }
            
            return cpItems;
        }
        

         public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            
            StringBuffer sb = new StringBuffer();
            Element data = antProjectHelper.getPrimaryConfigurationData(true);
            org.w3c.dom.Document doc = data.getOwnerDocument();
            NodeList libs = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
            for (int i = 0; i < libs.getLength(); i++) {
                Node n = libs.item(i);
                n.getParentNode().removeChild(n);
            }
            for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
                VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
                String library_tag_value = "";
                
                switch( vcpi.getType() ) {
                    
                    case VisualClassPathItem.TYPE_JAR:
                        String raw = vcpi.getRaw();
                        
                        if ( raw == null ) {
                            // New file
                            File file = (File)vcpi.getObject();
                            // pass null as expected artifact type to always get file reference
                            String reference = refHelper.createForeignFileReference(file, null);
                            library_tag_value = reference;
                        }
                        else {
                            // Existing property
                            library_tag_value = raw;
                        }
                        
                        break;
                    case VisualClassPathItem.TYPE_LIBRARY:
                        library_tag_value = vcpi.getRaw();
                        break;
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        AntArtifact aa = (AntArtifact)vcpi.getObject();
                        String reference = refHelper.createForeignFileReference( aa );
                        library_tag_value = reference;
                        break;
                    case VisualClassPathItem.TYPE_CLASSPATH:
                        library_tag_value = vcpi.getRaw();
                        break;
                }
                sb.append(library_tag_value);
                sb.append (File.pathSeparator);
                if (vcpi.isInDeployment().booleanValue()) {
                    Element library = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
                    library.appendChild(doc.createTextNode(getAntPropertyName(library_tag_value)));
                    data.appendChild(library);
                }
            }
            if (sb.length()>0) {
                sb.deleteCharAt(sb.length()-1);
            }
            antProjectHelper.putPrimaryConfigurationData(data, true);
            return sb.toString();
        }
     }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    private static List librariesInDeployment(AntProjectHelper helper) {
        Element data = helper.getPrimaryConfigurationData (true);
        NodeList libs = data.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
        List cpItems = new ArrayList( libs.getLength () );
        for (int i = 0; i < libs.getLength (); i++) {
            Element library = (Element) libs.item (i);
            cpItems.add(findText (library));
        }
        return cpItems;
    }
    
//    private static List readJavacClasspath (AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        String classpath = ep.getProperty(EjbJarProjectProperties.JAVAC_CLASSPATH);
//        String[] classPathElement = classpath.split(File.pathSeparator); 
//        List cpItems = new ArrayList();
//        List manifestItems = librariesInDeployment(antProjectHelper); 
//        for (int i = 0; i < classPathElement.length; i++) {
//            String file = classPathElement[i];
//            String propertyName = getAntPropertyName(file);
//            boolean inDeployment = cpItems.contains(propertyName);
//            VisualClassPathItem cpItem;
//
//            // First try to find out whether the item is well known classpath
//            // in the J2SE project type
//            int wellKnownPathIndex = -1;
//            for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
//                if ( WELL_KNOWN_PATHS[j][0].equals(propertyName))  {
//                    wellKnownPathIndex = j;
//                    break;
//                }
//            }
//
//            if ( wellKnownPathIndex != - 1 ) {
//                cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_CLASSPATH, file, WELL_KNOWN_PATHS[wellKnownPathIndex][1], inDeployment );
//            }                
//            else if ( file.startsWith( LIBRARY_PREFIX ) ) {
//                // Library from library manager
//                String eval = file.substring( LIBRARY_PREFIX.length(), file.lastIndexOf('.') ); //NOI18N
//                Library lib = LibraryManager.getDefault().getLibrary (eval);
//                if (lib != null) {
//                    cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, inDeployment );
//                }
//                else {
//                    //Invalid library. The lbirary was probably removed from system.
//                    cpItem = null;
//                }
//            }
//            else {
//                AntArtifact artifact = refHelper.getForeignFileReferenceAsArtifact( file );                     
//                if ( artifact != null ) {
//                    // Sub project artifact
//                    String eval = antProjectHelper.getStandardPropertyEvaluator ().evaluate (file);
//                    cpItem = new VisualClassPathItem( artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval, inDeployment);
//                }
//                else {
//                    // Standalone jar or property
//                    String eval = antProjectHelper.getStandardPropertyEvaluator ().evaluate (file);
//                    cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, inDeployment );
//                }
//            }
//            if (cpItem!=null) {
//                cpItems.add( cpItem );
//            }
//        }
//
//        return cpItems;
//    }

//    private static void writeJavacClasspath ( List value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        String libraries = "";
//        Element data = antProjectHelper.getPrimaryConfigurationData (true);
//        org.w3c.dom.Document doc = data.getOwnerDocument ();
//        NodeList libs = data.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
//        for (int i = 0; i < libs.getLength(); i++) {
//            Node n = libs.item(i); 
//            n.getParentNode().removeChild(n);
//        }
//        Element dataElement = (Element) doc.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "data").item (0); //NOI18N
//        for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
//            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//            String library_tag_value = "";
//
//            switch( vcpi.getType() ) {
//
//                case VisualClassPathItem.TYPE_JAR:
//                    String raw = vcpi.getRaw();
//
//                    if ( raw == null ) {
//                        // New file
//                        File file = (File)vcpi.getObject();
//                        String reference = refHelper.createForeignFileReference(file, JavaProjectConstants.ARTIFACT_TYPE_JAR);
//                        library_tag_value = reference;
//                    }
//                    else {
//                        // Existing property
//                        library_tag_value = raw;
//                    }
//
//                    break;
//                case VisualClassPathItem.TYPE_LIBRARY:
//                    library_tag_value = vcpi.getRaw();
//                    break;    
//                case VisualClassPathItem.TYPE_ARTIFACT:
//                    AntArtifact aa = (AntArtifact)vcpi.getObject();
//                    String reference = refHelper.createForeignFileReference( aa );
//                    library_tag_value = reference;
//                    break;
//                case VisualClassPathItem.TYPE_CLASSPATH:
//                    library_tag_value = vcpi.getRaw();
//                    break;
//            }
//            libraries += (libraries.length()>0?File.pathSeparator:"")+library_tag_value;
//            Element library = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
//            library.appendChild (doc.createTextNode (getAntPropertyName(library_tag_value)));
//            dataElement.appendChild(library);
//        }
//        EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        ep.setProperty(EjbJarProjectProperties.JAVAC_CLASSPATH, libraries);
//        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//        antProjectHelper.putPrimaryConfigurationData (data, true);
//    }
    
    private static class SourceRootsParser extends PropertyParser {

        public SourceRootsParser () {
        }

        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            SourceRoots roots = null;
            if (SOURCE_ROOTS.equals(raw)) {
                roots = ((EjbJarProject)project).getSourceRoots();
            }
            else if (TEST_ROOTS.equals(raw)) {
                roots = ((EjbJarProject)project).getTestSourceRoots();
            }
            else {
                return null;
            }
            String[] rootLabels = roots.getRootNames();
            URL[] rootURLs = roots.getRootURLs();
            Object[][] data = new Object[rootURLs.length] [2];
            for (int i=0; i< rootURLs.length; i++) {
                data[i][0] = new File (URI.create (rootURLs[i].toExternalForm()));
                data[i][1] = rootLabels[i] == null ? "" : rootLabels[i];    //NOI18N
            }
            return VisualSourceRootsSupport.createSourceModel(data);
        }

        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            return "true";   //NOI18N
        }
    }

}
