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


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.text.Collator;
import java.util.*;

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
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.compapp.projects.base.IcanproProjectType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Chris Webster
 */
public class IcanproProjectProperties {
    
    public static final String J2EE_1_4 = "1.4";
    public static final String J2EE_1_3 = "1.3";
    // Special properties of the project
    public static final String EJB_PROJECT_NAME = "j2ee.icanpro.name";
    public static final String JAVA_PLATFORM = "platform.active";
    public static final String J2EE_PLATFORM = "j2ee.platform";
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root";
    public static final String BUILD_FILE = "buildfile";
    public static final String DIST_DIR = "dist.dir";
    public static final String DIST_JAR = "dist.jar";
    public static final String JAVAC_CLASSPATH = "javac.classpath";
    public static final String DEBUG_CLASSPATH = "debug.classpath";    
    public static final String WSDL_CLASSPATH = "wsdl.classpath";

    public static final String JAR_NAME = "jar.name";
    public static final String JAR_COMPRESS = "jar.compress";

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";
    public static final String JAVAC_SOURCE = "javac.source";
    public static final String JAVAC_DEBUG = "javac.debug";
    public static final String JAVAC_DEPRECATION = "javac.deprecation";
    public static final String JAVAC_TARGET = "javac.target";
    public static final String JAVAC_ARGS = "javac.compilerargs";
    public static final String SRC_DIR = "src.dir";
    public static final String META_INF = "meta.inf";
    public static final String RESOURCE_DIR = "resource.dir";
    public static final String BUILD_DIR = "build.dir";
    public static final String BUILD_GENERATED_DIR = "build.generated.dir";
    public static final String BUILD_CLASSES_DIR = "build.classes.dir";
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes";

    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir";

    //================== Start of IcanPro =====================================//
    //FIXME? REPACKAGING
    /** @deprecated Use JBI_SE_TYPE instead. */
    public static final String JBI_SETYPE_PREFIX = "com.sun.jbi.ui.devtool.jbi.setype.prefix";
    public static final String JBI_SE_TYPE = "com.sun.jbi.ui.devtool.jbi.setype.prefix"; // todo: jbi.se.type
    /** @deprecated This will be removed soon because there is no model data backing it up. */
    public static final String ASSEMBLY_UNIT_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.assembly-unit";
    /** @deprecated This will be removed soon because there is no model data backing it up. */
    public static final String ASSEMBLY_UNIT_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.assembly-unit";
    /** @deprecated This will be removed soon because there is no model data backing it up. */
    public static final String APPLICATION_SUB_ASSEMBLY_ALIAS = "com.sun.jbi.ui.devtool.jbi.alias.application-sub-assembly";
    /** @deprecated Use SERVICE_UNIT_DESCRIPTION instead. */
    public static final String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly";    
    public static final String SERVICE_UNIT_DESCRIPTION = "com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly"; // todo: jbi.service-unit.description

    public static final String JBI_COMPONENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.component.conf.file";
    public static final String JBI_COMPONENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.component.conf.root";
    public static final String JBI_DEPLOYMENT_CONF_FILE = "com.sun.jbi.ui.devtool.jbi.deployment.conf.file";
    public static final String JBI_DEPLOYMENT_CONF_ROOT = "com.sun.jbi.ui.devtool.jbi.deployment.conf.root";
    public static final String DISPLAY_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.displayName";
    public static final String HOST_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.hostName";
    public static final String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.administrationPort";
    public static final String DOMAIN_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.domain";
    public static final String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpMonitorOn";
    public static final String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.httpPortNumber";
    public static final String LOCATION_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.location";
    public static final String PASSWORD_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.password";
    public static final String URL_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.url";
    public static final String USER_NAME_PROPERTY_KEY = "com.sun.jbi.ui.devtool.appserver.instance.userName";
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N

    public static final String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file";
    public static final String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost";

    public static final String BC_DEPLOYMENT_JAR = "bcdeployment.jar";
    public static final String SE_DEPLOYMENT_JAR = "sedeployment.jar";
    //================== End of IcanPro =======================================//

    // Shortcuts
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;

    private static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private static final PathParser PATH_PARSER = new PathParser();
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    private static final CharsetParser CHARSET_PARSER = new CharsetParser();

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
        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( WSDL_CLASSPATH, PROJECT, STRING_PARSER ),

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
        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
        new PropertyDescriptor(SOURCE_ENCODING, PROJECT, CHARSET_PARSER),

        //================== Start of IcanPro =====================================//
        new PropertyDescriptor( JBI_SETYPE_PREFIX, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( ASSEMBLY_UNIT_ALIAS, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( ASSEMBLY_UNIT_DESCRIPTION, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( APPLICATION_SUB_ASSEMBLY_ALIAS, PROJECT, STRING_PARSER ),
//        new PropertyDescriptor( APPLICATION_SUB_ASSEMBLY_DESCRIPTION, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( SERVICE_UNIT_DESCRIPTION, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JBI_COMPONENT_CONF_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JBI_DEPLOYMENT_CONF_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JBI_COMPONENT_CONF_FILE, PROJECT, STRING_PARSER ),

        new PropertyDescriptor( BC_DEPLOYMENT_JAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( SE_DEPLOYMENT_JAR, PROJECT, STRING_PARSER ),

        new PropertyDescriptor( JBI_DEPLOYMENT_CONF_FILE, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( DISPLAY_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( HOST_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( ADMINISTRATION_PORT_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( DOMAIN_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( HTTP_MONITOR_ON_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( HTTP_PORT_NUMBER_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( LOCATION_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( PASSWORD_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( URL_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( USER_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JBI_REGISTRY_COMPONENT_FILE_KEY, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JBI_REGISTRY_BROKER_HOST_KEY, PRIVATE, STRING_PARSER ),
        //================== End of IcanPro =======================================//
    };


    // Private fields ----------------------------------------------------------

    private Project project;
    private HashMap properties;
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;

    public IcanproProjectProperties(Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        this.project = project;
        this.properties = new HashMap();
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        read();
    }

    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */
    static String getAntPropertyName( String property ) {
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
        if (pi == null) {
            PropertyDescriptor pd = null;
            for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
                pd = PROPERTY_DESCRIPTORS[i];
                if (pd.name.compareTo(propertyName) == 0) {
                    break;
                }
                pd = null;
            }

            if (pd == null) return;
            // todo: assuming the new prop value is string...
            pi = new PropertyInfo( pd, (String) value, (String) value );
            properties.put( pd.name, pi );
        }
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
        if (pi == null) return null;

        return pi.getValue();
    }
    
    public boolean isModified( String propertyName ) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        if (pi == null) return false;

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
    
    public Project getProject() {
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
            }
            else {
                // Standard properties
                String raw = ((EditableProperties)eProps.get( pd.dest )).getProperty( pd.name );
                String eval = antProjectHelper.getStandardPropertyEvaluator ().getProperty ( pd.name );
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
                            }
                        }
                    }

                    String val = adjustWsdlPathProperty((EditableProperties)eProps.get( PROJECT ));

                    // Store the property changes into the project
                    antProjectHelper.putProperties( PROJECT, (EditableProperties)eProps.get( PROJECT ) );
                    antProjectHelper.putProperties( PRIVATE, (EditableProperties)eProps.get( PRIVATE ) );
                    ProjectManager.getDefault ().saveProject (project);
                    
                    // Persist encoding for future projects
                    Charset charset = (Charset) get(SOURCE_ENCODING);
                    if (charset != null) {
                        try {
                            FileEncodingQuery.setDefaultEncoding(charset);
                        } catch (UnsupportedCharsetException e) {
                            //When the encoding is not supported by JVM do not set it as default
                        }
                    }
                    
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

        // todo: the debug class path not clean when items are removed in javac
        String allPaths[] = { JAVAC_CLASSPATH }; //,  DEBUG_CLASSPATH };
        
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
            refHelper.destroyReference( vcpi.getRaw() );
        }
    }

    private String calculatePathDiff(String from, String to) {
        String fval = antProjectHelper.getStandardPropertyEvaluator().evaluate( from );
        String fto = antProjectHelper.getStandardPropertyEvaluator().evaluate( to );

        String diff = "";
        StringTokenizer st = new StringTokenizer(fval, "/");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            diff += "/.." ;
        }

        return diff + "/" + fto ;
    }

    private String adjustWsdlPathProperty(EditableProperties ep) {
        String val = "";
        String classpath = ep.getProperty(IcanproProjectProperties.JAVAC_CLASSPATH);
        if (classpath != null) {
            // todo: the following code only works for icanpro projects.
            String pathDiff = calculatePathDiff(
                    ep.getProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR),
                    ep.getProperty(IcanproProjectProperties.SRC_DIR)
            );
            String[] classPathElement = classpath.split(File.pathSeparator);
            for (int i = 0; i < classPathElement.length; i++) {
                val += ((i>0) ? File.pathSeparator : "") + classPathElement[i] + pathDiff;
            }
        }
        ep.setProperty(IcanproProjectProperties.WSDL_CLASSPATH, val);
        return val;
    }

    private class PropertyInfo {
        
        private PropertyDescriptor propertyDesciptor;
        private String rawValue;
        private String evaluatedValue;
        private Object value;
        private Object newValue;
        private String newValueEncoded;

        public PropertyInfo( PropertyDescriptor propertyDesciptor, String rawValue, String evaluatedValue ) {
            if (WSDL_CLASSPATH.equals( propertyDesciptor.name )) {
                String val = adjustWsdlPathProperty(antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH));

                this.propertyDesciptor = propertyDesciptor;
                this.rawValue = val;
                this.evaluatedValue = antProjectHelper.getStandardPropertyEvaluator().evaluate( val );
                this.value = propertyDesciptor.parser.decode( val, antProjectHelper, refHelper );
                this.newValue = value;
            } else {
                this.propertyDesciptor = propertyDesciptor;
                this.rawValue = rawValue;
                this.evaluatedValue = evaluatedValue;
                this.value = propertyDesciptor.parser.decode( rawValue, antProjectHelper, refHelper );
                this.newValue = null;
            }
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {            
            if ( isModified() ) {
                newValueEncoded = propertyDesciptor.parser.encode( newValue, antProjectHelper, refHelper);                
            } else {
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
        
        public abstract Object decode( String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
        
        public abstract String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
        
    }
    
    private static class StringParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return raw;
        }        
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return (String)value;
        }
        
    }
    
    private static class BooleanParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            
            if ( raw != null ) {
               String lowecaseRaw = raw.toLowerCase();
               
               if ( lowecaseRaw.equals( "true") || 
                    lowecaseRaw.equals( "yes") || 
                    lowecaseRaw.equals( "enabled") )
                   return Boolean.TRUE;                   
            }
            
            return Boolean.FALSE;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return ((Boolean)value).booleanValue() ? "true" : "false"; // NOI18N
        }
        
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {                    
            return ((Boolean)super.decode( raw, antProjectHelper, refHelper )).booleanValue() ? Boolean.FALSE : Boolean.TRUE;           
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper, refHelper );
        }
        
    }
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        { JAVAC_CLASSPATH, NbBundle.getMessage( IcanproProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) },
        { BUILD_CLASSES_DIR, NbBundle.getMessage( IcanproProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) }
    };
    
    private static class PlatformParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();            
            for( int i = 0; i < platforms.length; i++ ) {
                String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name");
                if ( normalizedName != null && normalizedName.equals( raw ) ) {
                    return platforms[i].getDisplayName();
                }
            }

            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName(); 
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
                    new Specification ("j2se",null));
            if (platforms.length == 0)
                return null;
            else
                return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
        }
    }
    
    
    private static class CharsetParser extends PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            if (raw == null) {
                raw = Charset.defaultCharset().name();
            }
            return new Charset(raw, new String[0]) {
                public boolean contains(Charset cs) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public CharsetDecoder newDecoder() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public CharsetEncoder newEncoder() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
            };
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return ((Charset)value).name();
        }
    }

     private static class PathParser extends PropertyParser {
         public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
             
             if (raw == null) {
                 return new ArrayList();
             }

             EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
             String classpath = ep.getProperty(IcanproProjectProperties.JAVAC_CLASSPATH);

             if (classpath == null) {
                 return new ArrayList();
             }
             String[] classPathElement = classpath.split(File.pathSeparator);
             List cpItems = new ArrayList();
             List manifestItems = librariesInDeployment(antProjectHelper);
             for (int i = 0; i < classPathElement.length; i++) {
                 String file = classPathElement[i];
                 String propertyName = getAntPropertyName(file);
                 boolean inDeployment = manifestItems.contains(propertyName);
                 VisualClassPathItem cpItem;
                 
                 // First try to find out whether the item is well known classpath
                 // in the J2SE project type
                 int wellKnownPathIndex = -1;
                 for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
                     if ( WELL_KNOWN_PATHS[j][0].equals(propertyName))  {
                         wellKnownPathIndex = j;
                         break;
                     }
                 }
                 
                 if ( wellKnownPathIndex != - 1 ) {
                     cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_CLASSPATH, file, WELL_KNOWN_PATHS[wellKnownPathIndex][1], inDeployment );
                 }
                 else if ( file.startsWith( LIBRARY_PREFIX ) ) {
                     // Library from library manager
                     String eval = file.substring( LIBRARY_PREFIX.length(), file.lastIndexOf('.') ); //NOI18N
                     Library lib = LibraryManager.getDefault().getLibrary(eval);
                     if (lib != null) {
                         cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, inDeployment );
                     }
                     else {
                         //Invalid library. The lbirary was probably removed from system.
                         cpItem = null;
                     }
                 }
                 else {
                     Object os[] = refHelper.findArtifactAndLocation( file );
                     if ((os != null) && (os.length > 0) ) {
                         AntArtifact artifact = (AntArtifact) os[0];
                         // Sub project artifact
                         String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(file);
                         cpItem = new VisualClassPathItem( artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval, inDeployment);
                     }
                     else {
                         // Standalone jar or property
                         String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(file);
                         cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, inDeployment );
                     }
                 }
                 if (cpItem!=null) {
                     cpItems.add( cpItem );
                 }
             }
             return cpItems;
         }
        
        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            
            StringBuffer sb = new StringBuffer();
            Element data = antProjectHelper.getPrimaryConfigurationData(true);
            org.w3c.dom.Document doc = data.getOwnerDocument();
            NodeList libs = data.getElementsByTagNameNS(IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
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
                            String reference = refHelper.createForeignFileReference(file, IcanproConstants.ARTIFACT_TYPE_JAR);
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
                        String reference = refHelper.addReference( aa, null );
                        library_tag_value = reference;
                        break;
                    case VisualClassPathItem.TYPE_CLASSPATH:
                        library_tag_value = vcpi.getRaw();
                        break;
                }
                sb.append(library_tag_value);
                sb.append (File.pathSeparator);
                if (vcpi.isInDeployment().booleanValue()) {
                    Element library = doc.createElementNS(IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
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
        NodeList libs = data.getElementsByTagNameNS (IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
        List cpItems = new ArrayList( libs.getLength () );
        for (int i = 0; i < libs.getLength (); i++) {
            Element library = (Element) libs.item (i);
            cpItems.add(findText (library));
        }
        return cpItems;
    }
    
//    private static List readJavacClasspath (AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
//        EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        String classpath = ep.getProperty(IcanproProjectProperties.JAVAC_CLASSPATH);
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
//        NodeList libs = data.getElementsByTagNameNS (IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
//        for (int i = 0; i < libs.getLength(); i++) {
//            Node n = libs.item(i); 
//            n.getParentNode().removeChild(n);
//        }
//        Element dataElement = (Element) doc.getElementsByTagNameNS (IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "data").item (0); //NOI18N
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
//            Element library = doc.createElementNS (IcanproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"); //NOI18N
//            library.appendChild (doc.createTextNode (getAntPropertyName(library_tag_value)));
//            dataElement.appendChild(library);
//        }
//        EditableProperties ep = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        ep.setProperty(IcanproProjectProperties.JAVAC_CLASSPATH, libraries);
//        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//        antProjectHelper.putPrimaryConfigurationData (data, true);
//    }
}
