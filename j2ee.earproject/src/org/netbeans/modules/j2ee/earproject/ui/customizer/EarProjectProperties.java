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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.io.File;
//import java.io.IOException;
//import java.text.Collator;
//import java.util.ArrayList;
//import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
//
//import org.openide.ErrorManager;
//import org.openide.util.MutexException;
//import org.openide.util.Mutex;
//import org.openide.util.NbBundle;
//
//import org.netbeans.api.java.platform.JavaPlatform;
//import org.netbeans.api.java.platform.JavaPlatformManager;
//import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
//import org.netbeans.api.project.ProjectManager;
//import org.netbeans.api.project.ProjectUtils;
//import org.netbeans.api.project.libraries.LibraryManager;
//import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
//import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
//import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
//import org.netbeans.spi.project.support.ant.EditableProperties;
//import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
//
import org.netbeans.api.java.project.JavaProjectConstants;
////import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
//
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;

import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ProjectEar;
import org.netbeans.modules.j2ee.dd.api.application.*;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

import org.openide.nodes.Node;

import org.netbeans.modules.web.api.webmodule.WebModule;
//
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.Text;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import java.util.Arrays;

import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 */
public class EarProjectProperties extends ArchiveProjectProperties {
    
    private EarProject earProject;

    /**
     * Holds value of property bogus.
     */
    private String bogus;

    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport (this);
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    
//    // Special properties of the project
//    public static final String WEB_PROJECT_NAME = "j2ee.ejbjarproject.name";
//    public static final String JAVA_PLATFORM = "platform.active";
//    public static final String J2EE_PLATFORM = "j2ee.platform";
//    
//    // Properties stored in the PROJECT.PROPERTIES    
//    /** root of external web module sources (full path), ".." if the sources are within project folder */
//    public static final String SOURCE_ROOT = "source.root";
//    public static final String BUILD_FILE = "buildfile";
//    public static final String LIBRARIES_DIR = "lib.dir";
//    public static final String DIST_DIR = "dist.dir";
//    public static final String DIST_WAR = "dist.jar";
//    public static final String JAVAC_CLASSPATH = "javac.classpath";
//    public static final String DEBUG_CLASSPATH = "debug.classpath";    
//
//    public static final String WAR_NAME = "war.name";
//    public static final String WAR_COMPRESS = "jar.compress";
//    public static final String WAR_CONTENT_ADDITIONAL = "war.content.additional";
//
//    public static final String LAUNCH_URL_RELATIVE = "client.urlPart";
//    public static final String LAUNCH_URL_FULL = "launch.url.full";
//    public static final String DISPLAY_BROWSER = "display.browser";
//    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";
//    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";
//    public static final String JAVAC_SOURCE = "javac.source";
//    public static final String JAVAC_DEBUG = "javac.debug";
//    public static final String JAVAC_DEPRECATION = "javac.deprecation";
//    public static final String JAVAC_TARGET = "javac.target";
//    public static final String SRC_DIR = "src.dir";
//    public static final String WEB_DOCBASE_DIR = "web.docbase.dir";
//    public static final String META_INF = "meta.inf";
//    public static final String BUILD_DIR = "build.dir";
//    public static final String BUILD_WEB_DIR = "build.web.dir";
//    public static final String BUILD_GENERATED_DIR = "build.generated.dir";
//    public static final String BUILD_CLASSES_DIR = "build.classes.dir";
//    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";
//    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir";
//    public static final String NO_DEPENDENCIES="no.dependencies";
//    
//    public static final String JAVADOC_PRIVATE="javadoc.private";
//    public static final String JAVADOC_NO_TREE="javadoc.notree";
//    public static final String JAVADOC_USE="javadoc.use";
//    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar";
//    public static final String JAVADOC_NO_INDEX="javadoc.noindex";
//    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex";
//    public static final String JAVADOC_AUTHOR="javadoc.author";
//    public static final String JAVADOC_VERSION="javadoc.version";
//    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle";
//    public static final String JAVADOC_ENCODING="javadoc.encoding";
//    
//    public static final String JAVADOC_PREVIEW="javadoc.preview";
//    
//    public static final String COMPILE_JSPS = "compile.jsps";
//    
//    // Properties stored in the PRIVATE.PROPERTIES
//    public static final String JSPC_CLASSPATH = "jspc.classpath";
//    
//    
//    // Shortcuts 
//    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
//    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
//    
//    private static final PropertyParser STRING_PARSER = new StringParser();
//    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
//    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
//    private static final PropertyParser PATH_PARSER = new PathParser();
//    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
//    
//    // Info about the property destination
//    private PropertyDescriptor PROPERTY_DESCRIPTORS[] = {
//        new PropertyDescriptor( WEB_PROJECT_NAME, null, STRING_PARSER ),
//        new PropertyDescriptor( J2EE_PLATFORM, PROJECT, STRING_PARSER ),
//                
//        new PropertyDescriptor( SOURCE_ROOT, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( BUILD_FILE, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( LIBRARIES_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( DIST_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( DIST_WAR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, PATH_PARSER ),
//        new PropertyDescriptor( JSPC_CLASSPATH, PRIVATE, PATH_PARSER ),
//        new PropertyDescriptor( COMPILE_JSPS, PROJECT, BOOLEAN_PARSER ),
//        //new PropertyDescriptor( JSP_COMPILER_CLASSPATH, PRIVATE, PATH_PARSER ),
//        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, PATH_PARSER ),
//
//        new PropertyDescriptor( WAR_NAME, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( WAR_COMPRESS, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( WAR_CONTENT_ADDITIONAL, PROJECT, PATH_PARSER ),
//        
//        new PropertyDescriptor( LAUNCH_URL_RELATIVE, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( LAUNCH_URL_FULL, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( DISPLAY_BROWSER, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( J2EE_SERVER_TYPE, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER ),
//        new PropertyDescriptor( JAVAC_SOURCE, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER ),       
//        new PropertyDescriptor( JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVAC_TARGET, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( SRC_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( BUILD_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( BUILD_CLASSES_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( WEB_DOCBASE_DIR, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( NO_DEPENDENCIES, PROJECT, INVERSE_BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
//        
//        new PropertyDescriptor( JAVADOC_PRIVATE, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_NO_TREE, PROJECT, INVERSE_BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_USE, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_NO_NAVBAR, PROJECT, INVERSE_BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_NO_INDEX, PROJECT, INVERSE_BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_SPLIT_INDEX, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_AUTHOR, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_VERSION, PROJECT, BOOLEAN_PARSER ),
//        new PropertyDescriptor( JAVADOC_WINDOW_TITLE, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( JAVADOC_ENCODING, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( JAVADOC_PREVIEW, PROJECT, BOOLEAN_PARSER ),
//    };
//    
//    
//    // Private fields ----------------------------------------------------------
//    
//    private Project project;
//    private HashMap properties;    
//    private AntProjectHelper antProjectHelper;
//    private ReferenceHelper refHelper;
    
    public EarProjectProperties(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, AntBasedProjectType abpt) {
        super(project,antProjectHelper,refHelper, abpt);
        earProject = (EarProject) project;
        //antProjectHelper.addAntProjectListener(this);
//        this.project = project;
//        this.properties = new HashMap();
//        this.antProjectHelper = antProjectHelper;
//        this.refHelper = refHelper;
//        read();                                
    }
    
//    /** XXX to be deleted when introduced in AntPropertyHeleper API
//     */    
//    static String getAntPropertyName( String property ) {
//        if ( property != null && 
//             property.startsWith( "${" ) && // NOI18N
//             property.endsWith( "}" ) ) { // NOI18N
//            return property.substring( 2, property.length() - 1 ); 
//        }
//        else {
//            return property;
//        }
//    }
//    
//    public void put( String propertyName, Object value ) {
//        super.put(propertyName, value);
//        if (JAR_CONTENT_ADDITIONAL.equals(propertyName)) {
//            if (value instanceof List)
//                updateApplicationXml((List)value);
//            else
//                assert false; //  "Illegal argument";
//        }
//    }
//        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
//        if (JAVAC_CLASSPATH.equals (propertyName)) {
//            assert value instanceof List : "Wrong format of property " + propertyName; //NOI18N
//            writeJavacClasspath ((List) value, antProjectHelper, refHelper);
//        } else if (WAR_CONTENT_ADDITIONAL.equals (propertyName)) {
//            assert value instanceof List : "Wrong format of property " + propertyName; //NOI18N
//            writeWarIncludes ((List) value, antProjectHelper, refHelper);
//        }
//        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
//        pi.setValue( value );
//        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
//            put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
//        }
//    }
//    
//    public Object get( String propertyName ) {
//        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
//        if (JAVAC_CLASSPATH.equals (propertyName)) {
//            return readJavacClasspath (antProjectHelper, refHelper);
//        } else if (WAR_CONTENT_ADDITIONAL.equals (propertyName)) {
//            return readWarIncludes(antProjectHelper, refHelper);
//        }
//
//        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
//        return pi.getValue();
//    }
//    
//    public boolean isModified( String propertyName ) {
//        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
//        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
//        return pi.isModified();
//    }
//    
//    public List getSortedSubprojectsList() {
//             
//        ArrayList subprojects = new ArrayList( 5 );
//        addSubprojects( project, subprojects ); // Find the projects recursively
//         
//        // Replace projects in the list with formated names
//        for ( int i = 0; i < subprojects.size(); i++ ) {
//            Project p = (Project)subprojects.get( i );           
//            subprojects.set(i, ProjectUtils.getInformation(p).getDisplayName());
//        }
//        
//        // Sort the list
//        Collections.sort( subprojects, Collator.getInstance() );
//        
//        return subprojects;
//    }
//    
//    public Project getProject() {
//        return project;
//    }
//    
//    /** Gets all subprojects recursively
//     */
//    private void addSubprojects( Project project, List result ) {
//        
//        SubprojectProvider spp = (SubprojectProvider)project.getLookup().lookup( SubprojectProvider.class );
//        
//        if ( spp == null ) {
//            return;
//        }
//        
//        for( Iterator/*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext(); ) {
//            Project sp = (Project)it.next(); 
//            if ( !result.contains( sp ) ) {
//                result.add( sp );
//            }
//            addSubprojects( sp, result );            
//        }
//        
//    }
//
//    /** Reads all the properties of the project and converts them to objects
//     * suitable for usage in the GUI controls.
//     */    
//    private void read() {
//        
//        // Read the properties from the project        
//        HashMap eProps = new HashMap( 2 );
//        eProps.put( PROJECT, antProjectHelper.getProperties( PROJECT ) ); 
//        eProps.put( PRIVATE, antProjectHelper.getProperties( PRIVATE ) );
//   
//        // Initialize the property map with objects
//        for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
//            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
//            if ( pd.dest == null ) {
//                // Specialy handled properties
//                if ( WEB_PROJECT_NAME.equals( pd.name ) ) {
//                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
//                    properties.put( pd.name, new PropertyInfo( pd, projectName, projectName ) );            
//                }
//            }
//            else {
//                // Standard properties
//                String raw = ((EditableProperties)eProps.get( pd.dest )).getProperty( pd.name );
//                String eval = antProjectHelper.getStandardPropertyEvaluator ().getProperty ( pd.name );
//                properties.put( pd.name, new PropertyInfo( pd, raw, eval ) );            
//            }
//        }
//    }
//  
    
    private boolean notUpdating = true;
    /** Transforms all the Objects from GUI controls into String Ant 
     * properties and stores them in the project
     */    
    public void store() {
        try {
        notUpdating = false;
            super.store();
            notUpdating = true;
            //configurationXmlChanged(null);
        }
        finally {
            notUpdating = true;
        }
    }
//        // add the data from the project properties to the application.xml file
//        
//        try {
////            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
////                public Object run() throws IOException {
//            PropertyInfo pi = null;
//            Object o = properties.get(ArchiveProjectProperties.JAR_CONTENT_ADDITIONAL);
//            if (null != o && o instanceof PropertyInfo) {
//                pi = (PropertyInfo) o;
//            }
//                    resolveProjectDependencies();
//                    
//                    // Some properties need special handling e.g. if the 
//                    // property changes the project.xml files                   
//                    for( Iterator it = properties.values().iterator(); it.hasNext(); ) {
//                        PropertyInfo pi = (PropertyInfo)it.next();
//                        PropertyDescriptor pd = pi.getPropertyDescriptor();
//                        pi.encode();
//                        String newValueEncoded = pi.getNewValueEncoded();
//                        if( pd.dest == null && newValueEncoded != null ) {
//                            // Specialy handled properties
//                            if (WEB_PROJECT_NAME.equals(pd.name)) {
//                                String newName = newValueEncoded;
//                                assert false : "No support yet for changing name of J2SEProject; cf. J2SEProject.setName";
//                            }
//                        }   
//                        if ( JAVA_PLATFORM.equals( pd.name) && newValueEncoded != null ) {
//                            setPlatform( pi.getNewValueEncoded().equals(
//                                    JavaPlatformManager.getDefault().getDefaultPlatform().getProperties().get("platform.ant.name")));
//                        }
//                    }
//                    
//                    // Reread the properties. It may have changed when
//                    // e.g. when setting references to another projects
//                    HashMap eProps = new HashMap( 2 );
//                    eProps.put( PROJECT, antProjectHelper.getProperties( PROJECT ) ); 
//                    eProps.put( PRIVATE, antProjectHelper.getProperties( PRIVATE ) );
//        
//                     
//                    // Set the changed properties
//                    for( Iterator it = properties.values().iterator(); it.hasNext(); ) {
//                        PropertyInfo pi = (PropertyInfo)it.next();
//                        PropertyDescriptor pd = pi.getPropertyDescriptor();                        
//                        String newValueEncoded = pi.getNewValueEncoded();
//                        if ( newValueEncoded != null ) {                            
//                            if ( pd.dest != null ) {
//                                // Standard properties
//                                ((EditableProperties)eProps.get( pd.dest )).setProperty( pd.name, newValueEncoded );
//                            }
//                        }
//                    }
//                    
//                    // Store the property changes into the project
//                    antProjectHelper.putProperties( PROJECT, (EditableProperties)eProps.get( PROJECT ) );
//                    antProjectHelper.putProperties( PRIVATE, (EditableProperties)eProps.get( PRIVATE ) );
//                    ProjectManager.getDefault ().saveProject (project);
//                    return null;
//                }
//            });
//        } 
//        catch (MutexException e) {
//            ErrorManager.getDefault().notify((IOException)e.getException());
//        }
//        
//    }
//    
//    private void setPlatform( boolean isDefault ) {
//        
//        Element pcd = antProjectHelper.getPrimaryConfigurationData( true );
//
//        NodeList sps = pcd.getElementsByTagName( "explicit-platform" );
//        
//        if ( isDefault && sps.getLength() > 0 ) {
//            pcd.removeChild( sps.item( 0 ) );
//        }
//        else if ( !isDefault && sps.getLength() == 0 ) {
//            pcd.appendChild( pcd.getOwnerDocument().createElement( "explicit-platform" ) );
//        }
//         
//        antProjectHelper.putPrimaryConfigurationData( pcd, true );
//        
//    }
//    
//    /** Finds out what are new and removed project dependencies and 
//     * applyes the info to the project
//     */
//    private void resolveProjectDependencies() {
//    
//        String allPaths[] = { JAVAC_CLASSPATH,  DEBUG_CLASSPATH };
//        
//        // Create a set of old and new artifacts.
//        Set oldArtifacts = new HashSet();
//        Set newArtifacts = new HashSet();
//        for ( int i = 0; i < allPaths.length; i++ ) {            
//            PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );
//
//            // Get original artifacts
//            List oldList = (List)pi.getOldValue();
//            if ( oldList != null ) {
//                for( Iterator it = oldList.iterator(); it.hasNext(); ) {
//                    VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//                    if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ) {
//                        oldArtifacts.add( vcpi );
//                    }
//                }
//            }
//            
//            // Get artifacts after the edit
//            List newList = (List)pi.getValue();
//            if ( newList != null ) {
//                for( Iterator it = newList.iterator(); it.hasNext(); ) {
//                    VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//                    if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ) {
//                        newArtifacts.add( vcpi );
//                    }
//                }
//            }
//                        
//        }
//                
//        // Create set of removed artifacts and remove them
//        Set removed = new HashSet( oldArtifacts );
//        removed.removeAll( newArtifacts );
//        for( Iterator it = removed.iterator(); it.hasNext(); ) {
//            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//            refHelper.destroyForeignFileReference( vcpi.getRaw() );
//        }
//                
//        // Create set of newly added artifacts and add them
//        /*
//        Set added = new HashSet( newArtifacts );
//        added.removeAll( oldArtifacts );
//        for( Iterator it = added.iterator(); it.hasNext(); ) {
//            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//            refHelper.destroyForeignFileReference( vcpi.getRaw() );
//        }
//        */
//                
//    }
//        
//    private class PropertyInfo {
//        
//        private PropertyDescriptor propertyDesciptor;
//        private String rawValue;
//        private String evaluatedValue;
//        private Object value;
//        private Object newValue;
//        private String newValueEncoded;
//        
//        public PropertyInfo( PropertyDescriptor propertyDesciptor, String rawValue, String evaluatedValue ) {
//            this.propertyDesciptor = propertyDesciptor;
//            this.rawValue = rawValue;
//            this.evaluatedValue = evaluatedValue;
//            this.value = propertyDesciptor.parser.decode( rawValue, antProjectHelper, refHelper );
//            this.newValue = null;
//        }
//        
//        public PropertyDescriptor getPropertyDescriptor() {
//            return propertyDesciptor;
//        }
//        
//        public void encode() {            
//            if ( isModified() ) {
//                newValueEncoded = propertyDesciptor.parser.encode( newValue, antProjectHelper, refHelper);                
//            }
//            else {
//                newValueEncoded = null;
//            }
//        }
//
//        public Object getValue() {
//            return isModified() ? newValue : value; 
//        }
//        
//        public void setValue( Object value ) {
//            newValue = value;
//        }
//        
//        public String getNewValueEncoded() {
//            return newValueEncoded;
//        }
//        
//        public boolean isModified() {
//            return newValue != null;
//        }
//        
//        public Object getOldValue() {
//            return value;
//        }
//    }
//    
//    private static class PropertyDescriptor {
//        
//        final PropertyParser parser;
//        final String name;
//        final String dest;
//        
//        PropertyDescriptor( String name, String dest, PropertyParser parser ) {
//            this.name = name;
//            this.dest = dest;
//            this.parser = parser;
//        }
//        
//    }
//    
//    
//    private static abstract class PropertyParser {
//        
//        public abstract Object decode( String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
//        
//        public abstract String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
//        
//    }
//    
//    private static class StringParser extends PropertyParser {
//        
//        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            return raw;
//        }        
//        
//        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            return (String)value;
//        }
//        
//    }
//    
//    private static class BooleanParser extends PropertyParser {
//        
//        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            
//            if ( raw != null ) {
//               String lowecaseRaw = raw.toLowerCase();
//               
//               if ( lowecaseRaw.equals( "true") || 
//                    lowecaseRaw.equals( "yes") || 
//                    lowecaseRaw.equals( "enabled") )
//                   return Boolean.TRUE;                   
//            }
//            
//            return Boolean.FALSE;
//        }
//        
//        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            return ((Boolean)value).booleanValue() ? "true" : "false"; // NOI18N
//        }
//        
//    }
//    
//    private static class InverseBooleanParser extends BooleanParser {
//        
//        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {                    
//            return ((Boolean)super.decode( raw, antProjectHelper, refHelper )).booleanValue() ? Boolean.FALSE : Boolean.TRUE;           
//        }
//        
//        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper, refHelper );
//        }
//        
//    }
//    
//    // XXX Define in the LibraryManager
//    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
//    // Contains well known paths in the J2SEProject
//    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
//        { JAVAC_CLASSPATH, NbBundle.getMessage( EarProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) },
//        { BUILD_CLASSES_DIR, NbBundle.getMessage( EarProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) }
//    };
//    
//    private static class PathParser extends PropertyParser {
//        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            
//            if (raw == null) {
//                return new ArrayList ();
//            }
//            
//            String pe[] = PropertyUtils.tokenizePath( raw );
//            List cpItems = new ArrayList( pe.length );
//            for( int i = 0; i < pe.length; i++ ) {
//                VisualClassPathItem cpItem;
//                
//                // First try to find out whether the item is well known classpath
//                // in the J2SE project type
//                int wellKnownPathIndex = -1;
//                for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
//                    if ( WELL_KNOWN_PATHS[j][0].equals( getAntPropertyName( pe[i] ) ) )  {
//                        wellKnownPathIndex = j;
//                        break;
//                    }
//                }
//                
//                if ( wellKnownPathIndex != - 1 ) {
//                    cpItem = new VisualClassPathItem( pe[i], VisualClassPathItem.TYPE_CLASSPATH, pe[i], WELL_KNOWN_PATHS[wellKnownPathIndex][1], VisualClassPathItem.PATH_IN_WAR_NONE );
//                }                
//                else if ( pe[i].startsWith( LIBRARY_PREFIX ) ) {
//                    // Library from library manager
//                    //String eval = antProjectHelper.evaluate( getAntPropertyName( pe[i] ) );
//                    String eval = pe[i].substring( LIBRARY_PREFIX.length(), pe[i].lastIndexOf('.') ); //NOI18N
//                    Library lib = LibraryManager.getDefault().getLibrary (eval);
//                    if (lib != null) {
//                        cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, pe[i], eval, VisualClassPathItem.PATH_IN_WAR_NONE );
//                    }
//                    else {
//                        //Invalid library. The lbirary was probably removed from system.
//                        cpItem = null;
//                    }
//                }
//                else {
//                    AntArtifact artifact = refHelper.getForeignFileReferenceAsArtifact( pe[i] );                     
//                    if ( artifact != null ) {
//                        // Sub project artifact
//                        String eval = antProjectHelper.getStandardPropertyEvaluator ().evaluate (pe[i]);
//                        cpItem = new VisualClassPathItem( artifact, VisualClassPathItem.TYPE_ARTIFACT, pe[i], eval, VisualClassPathItem.PATH_IN_WAR_NONE );
//                    }
//                    else {
//                        // Standalone jar or property
//                        String eval = antProjectHelper.getStandardPropertyEvaluator ().evaluate (pe[i]);
//                        String[] tokenizedPath = PropertyUtils.tokenizePath( raw );                                                
//                        cpItem = new VisualClassPathItem( tokenizedPath, VisualClassPathItem.TYPE_JAR, pe[i], eval, VisualClassPathItem.PATH_IN_WAR_NONE );
//                    }
//                }
//                if (cpItem!=null) {
//                    cpItems.add( cpItem );
//                }
//            }            
//            return cpItems;
//        }
//        
//        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//            
//            StringBuffer sb = new StringBuffer();
//                        
//            for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
//                
//                VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//                
//                //do not add applet libraries to classpath
//                if (VisualClassPathItem.PATH_IN_WAR_APPLET.equals (vcpi.getPathInWAR ()))
//                    continue;
//                
//                switch( vcpi.getType() ) {
//                                        
//                    case VisualClassPathItem.TYPE_JAR:
//                        String raw = vcpi.getRaw();
//                        
//                        if ( raw == null ) {
//                            // New file
//                            File file = (File)vcpi.getObject();
//                            String reference = refHelper.createForeignFileReference(file, JavaProjectConstants.ARTIFACT_TYPE_JAR);
//                            sb.append(reference);
//                        }
//                        else {
//                            // Existing property
//                            sb.append( raw );
//                        }
//                                                
//                        break;
//                    case VisualClassPathItem.TYPE_LIBRARY:
//                        sb.append(vcpi.getRaw());
//                        break;    
//                    case VisualClassPathItem.TYPE_ARTIFACT:
//                        AntArtifact aa = (AntArtifact)vcpi.getObject();
//                        String reference = refHelper.createForeignFileReference( aa );
//                        sb.append( reference );
//                        break;
//                    case VisualClassPathItem.TYPE_CLASSPATH:
//                        sb.append( vcpi.getRaw() );
//                        break;
//                }
//                
//                if ( it.hasNext() ) {
//                    sb.append( File.pathSeparatorChar );
//                }
//            }
//            
//            return sb.toString();
//        }
//    }
//    
//    /**
//     * Extract nested text from an element.
//     * Currently does not handle coalescing text nodes, CDATA sections, etc.
//     * @param parent a parent element
//     * @return the nested text, or null if none was found
//     */
//    public static String findText(Element parent) {
//        NodeList l = parent.getChildNodes();
//        for (int i = 0; i < l.getLength(); i++) {
//            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
//                Text text = (Text)l.item(i);
//                return text.getNodeValue();
//            }
//        }
//        return null;
//    }
//    
//    private static class PlatformParser extends PropertyParser {
//        
//        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
//            
//            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();            
//            for( int i = 0; i < platforms.length; i++ ) {
//                String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name");
//                if ( normalizedName != null && normalizedName.equals( raw ) ) {
//                    return platforms[i].getDisplayName();
//                }
//            }
//
//            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName(); 
//        }
//        
//        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
//            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
//                    new Specification ("j2se",null));
//            if (platforms.length == 0)
//                return null;
//            else
//                return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
//        }
//        
//    }
//    
//    private static List readJavacClasspath (AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        Element data = antProjectHelper.getPrimaryConfigurationData (true);
//        Element webModuleLibs = (Element) data.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries").item (0); //NOI18N
//        NodeList ch = webModuleLibs.getChildNodes ();
//        List cpItems = new ArrayList( ch.getLength () );
//        for (int i = 0; i < ch.getLength (); i++) {
//            if (ch.item (i).getNodeType () != Node.ELEMENT_NODE) continue;
//            Element library = (Element) ch.item (i);
//            Element webFile = (Element) library.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file").item (0); //NOI18N
//            String file = findText (webFile);
//            NodeList pathInWarList = library.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "path-in-war"); //NOI18N
//            String pathInWar = VisualClassPathItem.PATH_IN_WAR_NONE;
//            if (pathInWarList.getLength () > 0) {
//                pathInWar = findText ((Element) pathInWarList.item (0));
//            }
//            VisualClassPathItem cpItem;
//
//            // First try to find out whether the item is well known classpath
//            // in the J2SE project type
//            int wellKnownPathIndex = -1;
//            for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
//                if ( WELL_KNOWN_PATHS[j][0].equals( getAntPropertyName( file ) ) )  {
//                    wellKnownPathIndex = j;
//                    break;
//                }
//            }
//
//            if ( wellKnownPathIndex != - 1 ) {
//                cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_CLASSPATH, file, WELL_KNOWN_PATHS[wellKnownPathIndex][1], pathInWar );
//            }                
//            else if ( file.startsWith( LIBRARY_PREFIX ) ) {
//                // Library from library manager
//                //String eval = antProjectHelper.evaluate( getAntPropertyName( file ) );
//                String eval = file.substring( LIBRARY_PREFIX.length(), file.lastIndexOf('.') ); //NOI18N
//                Library lib = LibraryManager.getDefault().getLibrary (eval);
//                if (lib != null) {
//                    cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, pathInWar );
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
//                    cpItem = new VisualClassPathItem( artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval, pathInWar );
//                }
//                else {
//                    // Standalone jar or property
//                    String eval = antProjectHelper.getStandardPropertyEvaluator ().evaluate (file);
//                    cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, pathInWar );
//                }
//            }
//            if (cpItem!=null) {
//                cpItems.add( cpItem );
//            }
//        }
//
//        return cpItems;
//    }
//
//    private static List readWarIncludes(AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        Element data = antProjectHelper.getPrimaryConfigurationData(true);
//        Element webModuleLibs = (Element) data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries").item (0); //NOI18N
//        
//        //prevent NPE thrown from older projects
//        if (webModuleLibs == null)
//            return null;
//        
//        NodeList ch = webModuleLibs.getChildNodes();
//        List warAddItems = new ArrayList(ch.getLength());
//        for (int i = 0; i < ch.getLength(); i++) {
//            if (ch.item(i).getNodeType() != Node.ELEMENT_NODE)
//                continue;
//            
//            Element library = (Element) ch.item(i);
//            Element webFile = (Element) library.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file").item (0); //NOI18N
//            String file = findText(webFile);
//            NodeList pathInWarList = library.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "path-in-war"); //NOI18N
//            String pathInWar = VisualClassPathItem.PATH_IN_WAR_NONE;
//            if (pathInWarList.getLength() > 0)
//                pathInWar = findText((Element) pathInWarList.item(0));
//            
//            VisualClassPathItem cpItem;
//
//            if (file.startsWith(LIBRARY_PREFIX)) {
//                // Library from library manager
//                //String eval = antProjectHelper.evaluate( getAntPropertyName( file ) );
//                String eval = file.substring(LIBRARY_PREFIX.length(), file.lastIndexOf('.')); //NOI18N
//                Library lib = LibraryManager.getDefault().getLibrary(eval);
//                if (lib != null)
//                    cpItem = new VisualClassPathItem(lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, pathInWar);
//                else
//                    //Invalid library. The lbirary was probably removed from system.
//                    cpItem = null;
//            } else {
//                AntArtifact artifact = refHelper.getForeignFileReferenceAsArtifact(file);                     
//                if (artifact != null) {
//                    // Sub project artifact
//                    String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(file);
//                    cpItem = new VisualClassPathItem(artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval, pathInWar);
//                } else {
//                    // Standalone jar or property
//                    String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(file);
//                    cpItem = new VisualClassPathItem(file, VisualClassPathItem.TYPE_JAR, file, eval, pathInWar);
//                }
//            }
//            if (cpItem != null)
//                warAddItems.add(cpItem);
//        }
//
//        return warAddItems;
//    }
//
//    private static void writeJavacClasspath ( List value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        Element data = antProjectHelper.getPrimaryConfigurationData (true);
//        org.w3c.dom.Document doc = data.getOwnerDocument ();
//        Element webModuleLibs = (Element) data.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries").item (0); //NOI18N
//        while (webModuleLibs.hasChildNodes ()) {
//            webModuleLibs.removeChild (webModuleLibs.getChildNodes ().item (0));
//        }
//
//        for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
//
//            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
//
//            String library_tag_value = "";
//
//            //TODO: prevent NPE from CustomizerCompile - need to investigate
//            if (vcpi == null)
//                return;
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
//
//            Element library = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
//            webModuleLibs.appendChild (library);
//            Element webFile = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
//            library.appendChild (webFile);
//            webFile.appendChild (doc.createTextNode (library_tag_value));
//
//            if (vcpi.getPathInWAR () != VisualClassPathItem.PATH_IN_WAR_NONE) {
//                Element pathInWar = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "path-in-war"); //NOI18N
//                pathInWar.appendChild (doc.createTextNode (vcpi.getPathInWAR ()));
//                library.appendChild (pathInWar);
//            }
//        }
//        antProjectHelper.putPrimaryConfigurationData (data, true);
//    }
//    
//    private void updateApplicationXml(List value) { // , AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
//        Element data = antProjectHelper.getPrimaryConfigurationData(true);
//        org.w3c.dom.Document doc = data.getOwnerDocument();
//        Element webModuleLibs = (Element) data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries").item (0); //NOI18N
//        
//        //prevent NPE thrown from older projects
//        if (webModuleLibs == null) {
//            webModuleLibs = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries"); //NOI18N
//            data.appendChild(webModuleLibs);
//        }
//        
//        while (webModuleLibs.hasChildNodes())
//            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));

//        for (Iterator it = value.iterator(); it.hasNext();) {
//            VisualClassPathItem vcpi = (VisualClassPathItem) it.next();
//            String library_tag_value = "";
//
//            //TODO: prevent NPE from CustomizerCompile - need to investigate
//            if (vcpi == null)
//                return;
//
//            switch( vcpi.getType() ) {
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
//            }
//
//            Element library = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
//            webModuleLibs.appendChild (library);
//            Element webFile = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
//            library.appendChild (webFile);
//            webFile.appendChild (doc.createTextNode (library_tag_value));
//
//            if (vcpi.getPathInWAR () != VisualClassPathItem.PATH_IN_WAR_NONE) {
//                Element pathInWar = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "path-in-war"); //NOI18N
//                pathInWar.appendChild (doc.createTextNode (vcpi.getPathInWAR ()));
//                library.appendChild (pathInWar);
//            }
//        }
//        antProjectHelper.putPrimaryConfigurationData (data, true);
        
//    public void updateApplicationXml() {
    protected void updateContentDependency(Set deleted, Set added) {     
        Application app = null;
        try {
            app = DDProvider.getDefault ().getDDRoot (earProject.getAppModule().getDeploymentDescriptor ());
            //kids.add(new Node[] { new LogicalViewNode(app) });
        }
        catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
        }
//        if (null != app)  change from S1
//            return;
        if (null != app) {
//            PropertyInfo pi = null;
//            Object o = properties.get(JAR_CONTENT_ADDITIONAL);
//            if (null != o && o instanceof PropertyInfo) {
//                pi = (PropertyInfo) o;
//                if (pi.isModified()) {
//                    List old = (List) pi.getOldValue();
//                    List newV = (List) pi.getValue();
//                    Set oldItems = new HashSet();
//                    Set deleted = new HashSet();
//                    if (null != old) {
//                        deleted.addAll(old);
//                        oldItems.addAll(old);
//                    }
//                    Set added = new HashSet();
//                    if (null != newV)
//                        added.addAll(newV);
//                    deleted.removeAll(added);
//                    added.removeAll(oldItems);
                                        
                    // delete the old entries out of the application
                    Iterator iter = deleted.iterator();
                    while (iter.hasNext()) {
                        VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                        removeItemFromAppDD(app,vcpi);
                    }
//                    List newV = (List) pi.getValue();
                    // add the new stuff "back"
                    iter = added.iterator();
                    while (iter.hasNext()) {
                        VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                        addItemToAppDD(app,vcpi);
                    }
                    //pi = null;
                //}
            //}
            try {
                app.write(earProject.getAppModule().getDeploymentDescriptor ());
            }
            catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
            }
            
        }
    }
    
    private void removeItemFromAppDD(Application dd, VisualClassPathItem vcpi) {
        String path = vcpi.getCompletePathInArchive();
        Module m = searchForModule(dd,path);
        if (null != m) {
            dd.removeModule(m);
            setClientModuleUri("");
            Object obj = vcpi.getObject();
            AntArtifact aa;
            Project p;
            if (obj instanceof AntArtifact) {
                aa = (AntArtifact) obj;
                p = aa.getProject();
            } else {
                return;
            }
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            //AppDDSegmentProvider seg = (AppDDSegmentProvider) p.getLookup().lookup(AppDDSegmentProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm)
                    earProject.getAppModule().removeModuleProvider(jmp,path);
            }
                return;
            }
        }
    
    private Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getConnector();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getJava();
            if (null != val && val.equals(path))
                return mods[i];
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w)
                val = w.getWebUri();
            if (null != val && val.equals(path))
                return mods[i];
        }
        return null;
    }
    
    private void addItemToAppDD(Application dd, VisualClassPathItem vcpi) {
        Object obj = vcpi.getObject();
        AntArtifact aa;
        Project p;
        String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
        Module mod = null;
        if (obj instanceof AntArtifact) {
            mod = getModFromAntArtifact((AntArtifact) obj, dd, path);
        }
        else if (obj instanceof File) {
            mod = getModFromFile((File) obj, dd, path);
        }
        if (mod != null && mod.getWeb() != null)
            replaceEmptyClientModuleUri(path);
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod)
            dd.addModule(mod);
    }
    
    
    private Module getModFromAntArtifact(AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            //AppDDSegmentProvider seg = (AppDDSegmentProvider) p.getLookup().lookup(AppDDSegmentProvider.class);
            if (null != jmp) {
//                String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
                jmp.setServerInstanceID(earProject.getServerInstanceID());
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    earProject.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean("Module");
                if (jm.getModuleType() == J2eeModule.EJB) {
                    mod.setEjb(path); // NOI18N
                }
                else if (jm.getModuleType() == J2eeModule.WAR) {
                    Web w = (Web) mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    org.openide.filesystems.FileObject tmp = aa.getScriptFile();
                    if (null != tmp)
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    WebModule wm = null;
                    if (null != tmp)
                        wm = (WebModule) WebModule.getWebModule(tmp);
                    if (null != wm) {
                        w.setContextRoot(wm.getContextPath());
                    }
                    else {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot(path.substring(0,endex));
                    }
                     mod.setWeb(w);
                }
                else if (jm.getModuleType() == J2eeModule.CONN) {
                    mod.setConnector(path);
                }
                else if (jm.getModuleType() == J2eeModule.CLIENT) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
        }
        return mod;
    }
    
    private void setClientModuleUri(String newVal) {
        put(EarProjectProperties.CLIENT_MODULE_URI,newVal);        
    }
    
    private void replaceEmptyClientModuleUri(String path) {
        // set the context path if it is not set...
        Object foo = get(EarProjectProperties.CLIENT_MODULE_URI);
        if (null == foo) {
            setClientModuleUri(path);
        }
        if (foo instanceof String) {
            String bar = (String) foo;
            if (bar.length() < 1) {
                setClientModuleUri(path);
            }
        }
        
    }
    
    private Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile((File) f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); //NOI18N
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    Web w = (Web) mod.newWeb(); 
                    w.setWebUri(path);
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot("/"+path.substring(0,endex)); // NOI18N
                    mod.setWeb(w);
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application.xml"); //NOI18N
                if (null != ddf) {
                    return null;
                }
            }
            catch (ClassNotFoundException cnfe) {
                org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
            }
            catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
            }
            finally {
                try {
                    if (null != jar)
                        jar.close();
                }
                catch (java.io.IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }

    //    private String computePath(VisualClassPathItem vcpi) {
//        String full = vcpi.getEvaluated();
//        int lastSlash = full.lastIndexOf('/');
//        String trimmed = null;
//        if (lastSlash != -1)
//            trimmed = full.substring(lastSlash+1);
//        else
//            trimmed = full;put
//        String path = vcpi.getPathInWAR();
//        if (null != path && path.length() > 1)
//            return path+"/"+trimmed;
//        else
//            return trimmed;
//    }
    
    /**
     * Called when a change was made to a properties file that might be shared with Ant.
     * <p class="nonnormative">
     * Note: normally you would not use this event to detect property changes.
     * Use the property change listener from {@link PropertyEvaluator} instead to find
     * changes in the interpreted values of Ant properties, possibly coming from multiple
     * properties files.
     * </p>
     * @param ev an event with details of the change
     */
    public void propertiesChanged(AntProjectEvent ev) {
        
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
    public Map getModuleMap() {
        Map mods = new HashMap();
        Object o = properties.get(JAR_CONTENT_ADDITIONAL);
        if (null != o && o instanceof PropertyInfo) {
            PropertyInfo pi = (PropertyInfo) o;
            List newV = (List) pi.getValue();
            
            Iterator iter = newV.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
                Object obj = vcpi.getObject();
                AntArtifact aa;
                Project p;
                if (obj instanceof AntArtifact) {
                    aa = (AntArtifact) obj;
                    p = aa.getProject();
                } else {
                    continue;
                }
                J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
                //AppDDSegmentProvider seg = (AppDDSegmentProvider) p.getLookup().lookup(AppDDSegmentProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        mods.put(path, jmp);
                    }
                }
            }
        }
        return mods; // earProject.getAppModule().setModules(mods);
    }


    public void addJ2eeSubprojects(Project[] moduleProjects) {
            List artifactList = new ArrayList();
            for (int i = 0; i < moduleProjects.length; i++) {
                AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType( 
                    moduleProjects[i], 
                    EjbProjectConstants.ARTIFACT_TYPE_EJBJAR_EAR_ARCHIVE ); //the artifact type is the some for both ejb and war projects
                if (null != artifacts)
                    artifactList.addAll(Arrays.asList(artifacts));
                
//                artifacts = AntArtifactQuery.findArtifactsByType( 
//                    moduleProjects[i], 
//                    WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE );
//                if (null != artifacts)
//                    artifactList.addAll(Arrays.asList(artifacts));
                
            }
            // create the vcpis
            List newVCPIs = new ArrayList();
            Iterator iter = artifactList.iterator();
            while (iter.hasNext()) {
                AntArtifact art = (AntArtifact) iter.next();
                VisualClassPathItem vcpi = VisualClassPathItem.create(art,VisualClassPathItem.PATH_IN_WAR_APPLET);
                    //new VisualClassPathItem(art, VisualClassPathItem.TYPE_ARTIFACT, null, art.getArtifactLocation().toString(), VisualClassPathItem.PATH_IN_WAR_APPLET);
                vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
                newVCPIs.add(vcpi);
            }
            Object t = get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
            if (!(t instanceof List)) {
                assert false : "jar content isn't a List???";
                return;
            }
            List vcpis = (List) t;
            newVCPIs.addAll(vcpis);
            put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newVCPIs);
            //epp.updateApplicationXml();
            store();
                try {
                    org.netbeans.api.project.ProjectManager.getDefault().saveProject(getProject());
                }
                catch ( java.io.IOException ex ) {
                    org.openide.ErrorManager.getDefault().notify( ex );
                }
    }
    
    String[] getWebUris() {
        Application app = null;
        try {
            app = DDProvider.getDefault ().getDDRoot (earProject.getAppModule().getDeploymentDescriptor ());
            //kids.add(new Node[] { new LogicalViewNode(app) });
        }
        catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
        }
        Module mods[] = app.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        ArrayList retList = new ArrayList();
        for (int i = 0; i < len; i++) {
            Web w = mods[i].getWeb();
            if (null != w) {
                retList.add(w.getWebUri());
            }
        }
        return (String[]) retList.toArray(new String[retList.size()]);
        
    }
    
    // XXX - remove this method after completing 54179
    private  boolean projectClosed() {
        Project[] projects = org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(earProject))
                return false;
        }
        return true;
    }
    
}
