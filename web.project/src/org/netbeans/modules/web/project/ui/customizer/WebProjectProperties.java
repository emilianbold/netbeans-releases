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

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.*;

import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.web.project.SourceRoots;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
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
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.WebProjectType;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Document;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 */
public class WebProjectProperties {
    
    // Special properties of the project
    public static final String WEB_PROJECT_NAME = "web.project.name"; //NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; //NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; //NOI18N
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_WAR = "dist.war"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String JSPCOMPILATION_CLASSPATH = "jspcompilation.classpath";     //NOI18N

    public static final String WAR_NAME = "war.name"; //NOI18N
    public static final String WAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String WAR_CONTENT_ADDITIONAL = "war.content.additional"; //NOI18N

    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String CONTEXT_PATH = "context.path"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String SRC_DIR = "src.dir"; //NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; //NOI18N
    public static final String CONF_DIR = "conf.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_WEB_DIR = "build.web.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String BUILD_WEB_EXCLUDES = "build.web.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N

    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; //NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; //NOI18N
    public static final String JAVADOC_USE="javadoc.use"; //NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; //NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; //NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; //NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; //NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; //NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; //NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; //NOI18N
    
    public static final String JAVADOC_PREVIEW="javadoc.preview"; //NOI18N
    
    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    
    // SOURCE ROOTS
    public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    public static final String TEST_ROOTS = "__virtual_test_roots__";   //NOI18N

    // Shortcuts 
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    
    private static final String PLATFORM_ANT_NAME = "platform.ant.name"; // NOI18N

    private static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    private static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N

    static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private static final PropertyParser PATH_PARSER = new PathParser();
    private static final PropertyParser JAVAC_CLASSPATH_PARSER = new PathParser(TAG_WEB_MODULE_LIBRARIES);
    private static final PropertyParser WAR_CONTENT_ADDITIONAL_PARSER = new PathParser(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    private static final SourceRootsParser SOURCE_ROOTS_PARSER = new SourceRootsParser();

    // Info about the property destination
    private PropertyDescriptor PROPERTY_DESCRIPTORS[] = {
        new PropertyDescriptor( WEB_PROJECT_NAME, null, STRING_PARSER ),
        new PropertyDescriptor( J2EE_PLATFORM, PROJECT, STRING_PARSER ),
                
        new PropertyDescriptor( SOURCE_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_FILE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( LIBRARIES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_WAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, JAVAC_CLASSPATH_PARSER ),
        new PropertyDescriptor( COMPILE_JSPS, PROJECT, BOOLEAN_PARSER ),
        //new PropertyDescriptor( JSP_COMPILER_CLASSPATH, PRIVATE, PATH_PARSER ),
        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( JSPCOMPILATION_CLASSPATH, PROJECT, PATH_PARSER ),

        new PropertyDescriptor( WAR_NAME, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( WAR_COMPRESS, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( WAR_CONTENT_ADDITIONAL, PROJECT, WAR_CONTENT_ADDITIONAL_PARSER ),
        
        new PropertyDescriptor( LAUNCH_URL_RELATIVE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DISPLAY_BROWSER, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_TYPE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_SOURCE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER ),       
        new PropertyDescriptor( JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVAC_COMPILER_ARG, PROJECT, STRING_PARSER),
        new PropertyDescriptor( JAVAC_TARGET, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( SRC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( TEST_SRC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( WEB_DOCBASE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( RESOURCE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( NO_DEPENDENCIES, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
        
        new PropertyDescriptor( JAVAC_TEST_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( RUN_TEST_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( BUILD_TEST_CLASSES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_TEST_RESULTS_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DEBUG_TEST_CLASSPATH, PROJECT, PATH_PARSER ),
                
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
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;

    public WebProjectProperties( Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper ) {
        this.project = project;
        this.properties = new HashMap();
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
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
    
     static boolean isAntProperty (String string) {
        return string != null && string.startsWith( "${" ) && string.endsWith( "}" ); //NOI18N
    }
    
   public void put( String propertyName, Object value ) {
        PropertyInfo pi = getPropertyInfo(propertyName);
        pi.setValue( value );
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }
    
    public Object get(String propertyName) {
        PropertyInfo pi = getPropertyInfo(propertyName);
        return pi == null ? null : pi.getValue();
    }

    private PropertyInfo getPropertyInfo(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        return (PropertyInfo) properties.get(propertyName);
    }

    public String getEncodedProperty(String propertyName) {
        PropertyInfo pi = getPropertyInfo(propertyName);
        if (pi == null) {
            return null;
        }
        pi.encode();
        return pi.getNewValueEncoded();
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
            }
            addSubprojects( sp, result );            
        }
        
    }

    /** Reads all the properties of the project and converts them to objects
     * suitable for usage in the GUI controls.
     */    
    private void read() {
        
        // Read the properties from the project        
        HashMap eProps = new HashMap( 2 );
        eProps.put( PROJECT, updateHelper.getProperties( PROJECT ) );
        eProps.put( PRIVATE, updateHelper.getProperties( PRIVATE ) );
   
        // Initialize the property map with objects
        for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
            if ( pd.dest == null ) {
                // Specialy handled properties
                if ( WEB_PROJECT_NAME.equals( pd.name ) ) {
                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
                    properties.put( pd.name, new PropertyInfo( pd, projectName, projectName ) );
                } else if (SOURCE_ROOTS.equals(pd.name) || TEST_ROOTS.equals(pd.name)) {
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

    void initProperty(final String propertyName, final PropertyInfo propertyInfo) {
        properties.put(propertyName, propertyInfo);
    }

    /** Transforms all the Objects from GUI controls into String Ant
     * properties and stores them in the project
     */    
    public void store() {

        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {

                    resolveProjectDependencies();

                    Boolean defaultPlatform = null;
                    
                    // Some properties need special handling e.g. if the 
                    // property changes the project.xml files                   
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        pi.encode();
                        String newValueEncoded = pi.getNewValueEncoded();
                        
                        if(pd.saver != null) {
                            pd.saver.save(pi);
                        }
                        
                        if (pd.dest == null && newValueEncoded != null) {
                            // Specialy handled properties
                            if (WEB_PROJECT_NAME.equals(pd.name)) {
                                String newName = newValueEncoded;
                                assert false : "No support yet for changing name of J2SEProject; cf. J2SEProject.setName";  //NOI18N
                            } else if ( SOURCE_ROOTS.equals( pd.name ) || TEST_ROOTS.equals( pd.name )) {
                                SourceRoots roots = null;
                                if (SOURCE_ROOTS.equals(pi.rawValue)) {
                                    roots = ((WebProject)project).getSourceRoots();
                                }
                                else if (TEST_ROOTS.equals(pi.rawValue)) {
                                    roots = ((WebProject)project).getTestSourceRoots();
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
                            defaultPlatform = Boolean.valueOf(pi.getNewValueEncoded().equals(
                                    JavaPlatformManager.getDefault().getDefaultPlatform().getProperties().get("platform.ant.name"))); // NOI18N
                             setPlatform(defaultPlatform.booleanValue(), pi.getNewValueEncoded());
                        }
                    }
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    HashMap eProps = new HashMap(2);
                    eProps.put( PROJECT, updateHelper.getProperties( PROJECT ) );
                    eProps.put( PRIVATE, updateHelper.getProperties( PRIVATE ) );        
                     
                    // Set the changed properties
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        String newValueEncoded = pi.getNewValueEncoded();
                        if (newValueEncoded != null) {
                            if (pd.dest != null) {
                                // Standard properties
                                EditableProperties ep = (EditableProperties) eProps.get(pd.dest);
                                if (PATH_PARSER.equals(pd.parser)) {
                                    // XXX: perhaps PATH_PARSER could return List of paths so that
                                    // tokenizing could be omitted here:
                                    String[] items = PropertyUtils.tokenizePath(newValueEncoded);
                                    for (int i = 0; i < items.length - 1; i++) {
                                        items[i] += File.pathSeparatorChar;
                                    }
                                    ep.setProperty(pd.name, items);
                                } else {
                                    
                                    // update javac.source and javac.target
                                    if (JAVA_PLATFORM.equals(pd.name)) {
                                        assert defaultPlatform != null;
                                        updateSourceLevel(defaultPlatform.booleanValue(), newValueEncoded, ep);
                                    }
                                    if (J2EE_SERVER_INSTANCE.equals(pd.name)) {
                                        // update j2ee.platform.classpath
                                        String oldServInstID = ep.getProperty(J2EE_SERVER_INSTANCE);
                                        if (oldServInstID != null) {
                                            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
                                            if (oldJ2eePlatform != null) {
                                                ((WebProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
                                            }
                                        }
                                        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newValueEncoded);
                                        ((WebProject)project).registerJ2eePlatformListener(j2eePlatform);
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
                                    
                                    if (NO_DEPENDENCIES.equals(pd.name) && newValueEncoded.equals("false")) { // NOI18N
                                        ep.remove(pd.name);
                                        continue;
                                    }
                                    
//                                    if (RUN_WORK_DIR.equals(pd.name) && newValueEncoded.equals("")) { // NOI18N
//                                        ep.remove(pd.name);
//                                        continue;
//                                    }
                                    
                                    ep.setProperty( pd.name, newValueEncoded );
                                }
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    updateHelper.putProperties( PROJECT, (EditableProperties)eProps.get( PROJECT ) );
                    updateHelper.putProperties( PRIVATE, (EditableProperties)eProps.get( PRIVATE ) );
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    private void updateSourceLevel(boolean defaultPlatform, String platform, EditableProperties ep) {
        if (defaultPlatform) {
            ep.setProperty(JAVAC_SOURCE, "${default.javac.source}"); //NOI18N
            ep.setProperty(JAVAC_TARGET, "${default.javac.target}"); //NOI18N
        } else {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for( int i = 0; i < platforms.length; i++ ) {
                Specification spec = platforms[i].getSpecification();
                if (!("j2se".equalsIgnoreCase(spec.getName()))) { // NOI18N
                    continue;
                }
                if (platform.equals(platforms[i].getProperties().get("platform.ant.name"))) { //NOI18N
                    String ver = platforms[i].getSpecification().getVersion().toString();
                    ep.setProperty(JAVAC_SOURCE, ver);
                    ep.setProperty(JAVAC_TARGET, ver);
                    return;
                }
            }
            // The platformName does not exist. Perhaps this is project with broken references?
            // Do not update target and source because nothing is known about the platformName.
        }
    }
    
    private final SpecificationVersion JDKSpec13 = new SpecificationVersion("1.3"); // NOI18N
    
    private void setPlatform(boolean isDefault, String platformAntID) {
        Element pcd = updateHelper.getPrimaryConfigurationData( true );
        NodeList sps = pcd.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); // NOI18N
//        NodeList sps = pcd.getElementsByTagName( "explicit-platform" ); // NOI18N
        if (isDefault && sps.getLength() > 0) {
            pcd.removeChild(sps.item(0));
        } else if (!isDefault) {
            Element el;
            if (sps.getLength() == 0) {
                el = pcd.getOwnerDocument().createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); // NOI18N
//                el = pcd.getOwnerDocument().createElement("explicit-platform"); // NOI18N
                pcd.appendChild(el);
            } else {
                el = (Element)sps.item(0);
            }
            boolean explicitSource = true;
            JavaPlatform platform = Utils.findJavaPlatform(platformAntID);
            if ((platform != null && platform.getSpecification().getVersion().compareTo(JDKSpec13) <= 0) || platform == null) {
                explicitSource = false;
            }
            el.setAttribute("explicit-source-supported", explicitSource ? "true" : "false"); // NOI18N
        }
        updateHelper.putPrimaryConfigurationData(pcd, true);
    }
    
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
    
        String allPaths[] = { JAVAC_CLASSPATH,  DEBUG_CLASSPATH, WAR_CONTENT_ADDITIONAL, DEBUG_TEST_CLASSPATH, JAVAC_TEST_CLASSPATH};

        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        Set newArtifacts = new HashSet();
        for ( int i = 0; i < allPaths.length; i++ ) {            
            PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );

            // Get original artifacts
            List oldList = (List)pi.getOldValue();
            if ( oldList != null ) {
                oldArtifacts.addAll(oldList);
            }
            
            // Get artifacts after the edit
            List newList = (List)pi.getValue();
            if ( newList != null ) {
                newArtifacts.addAll(newList);
            }
                        
        }

        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set added = new HashSet(newArtifacts);
        added.removeAll(oldArtifacts);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ||
                    vcpi.getType() == VisualClassPathItem.TYPE_JAR ) {
                refHelper.destroyForeignFileReference(vcpi.getRaw());
            }
//            if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ||
//                 vcpi.getType() == VisualClassPathItem.TYPE_JAR ) {
//                     boolean used = false; // now check if the file reference isn't used anymore
//                     for (int i=0; i < allPaths.length; i++) {
//                        PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );
//                        List values = (List)pi.getValue();
//                        if (values == null) break;
//                        for (Iterator v = values.iterator(); v.hasNext(); ) {
//                            VisualClassPathItem valcpi = (VisualClassPathItem)v.next();
//                            String raw = valcpi.getRaw();
//                            if (raw != null && raw.indexOf(vcpi.getRaw()) > -1) {
//                                used = true;
//                                break;
//                            }
//                        }
//                     }
//                     if (!used) {
//                        refHelper.destroyForeignFileReference(vcpi.getRaw());
//                     }
//
//            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( PROJECT );
        boolean changed = false;
        
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = vcpi.getRaw();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        File projDir = FileUtil.toFile(updateHelper.getAntProjectHelper().getProjectDirectory());
        for( Iterator it = added.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                // add property to project.properties pointing to relativized 
                // library jar(s) if possible
                String prop = vcpi.getRaw();
                prop = prop.substring(2, prop.length()-1);
                String value = relativizeLibraryClasspath(prop, projDir);
                if (value != null) {
                    ep.setProperty(prop, value);
                    ep.setComment(prop, new String[]{
                        "# Property "+prop+" is set here just to make sharing of project simpler.", // NOI18N
                        "# The library definition has always preference over this property."}, false); // NOI18N
                    changed = true;
                }
            }
        }
        if (changed) {
            updateHelper.putProperties(PROJECT, ep);
        }
    }
        
      /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param property library property name ala "libs.someLib.classpath"
     * @param projectDir project dir for relativization
     * @return relativized library classpath or null if some jar is not collocated
     */
    private String relativizeLibraryClasspath(String property, File projectDir) {
        String value = PropertyUtils.getGlobalProperties().getProperty(property);
        // bugfix #42852, check if the classpath property is set, otherwise return null
        if (value == null) {
            return null;
        }
        String[] paths = PropertyUtils.tokenizePath(value);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<paths.length; i++) {
            File f = updateHelper.getAntProjectHelper().resolveFile(paths[i]);
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
        } else {
            return sb.toString();
        }
    }
    
    class PropertyInfo {        
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
            this.value = propertyDesciptor.parser.decode( rawValue, project, updateHelper.getAntProjectHelper(), evaluator, refHelper );
            this.newValue = null;
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {            
            if ( isModified() ) {
                newValueEncoded = propertyDesciptor.parser.encode( newValue, project, updateHelper.getAntProjectHelper(), refHelper);
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
    
    static class PropertyDescriptor {
        interface Saver {
            public void save(PropertyInfo propertyInfo);
        }

        final PropertyParser parser;
        final String name;
        final String dest;
        final Saver saver;


        PropertyDescriptor(String name, String dest, PropertyParser parser, Saver saver) {
            this.name = name;
            this.dest = dest;
            this.saver = saver;
            this.parser = parser;
        }

        PropertyDescriptor( String name, String dest, PropertyParser parser ) {
            this(name, dest, parser, null);
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
    
    private static class PathParser extends PropertyParser {
        private String webLibraryElementName;
        private static final String TAG_PATH_IN_WAR = "path-in-war"; //NOI18N
        private static final String TAG_FILE = "file"; //NOI18N
        private static final String TAG_LIBRARY = "library"; //NOI18N

        // XXX Define in the LibraryManager
        private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
        private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
    
        // Contains well known paths in the WebProject
        private static final String[][] WELL_KNOWN_PATHS = new String[][] {
            { JAVAC_CLASSPATH, NbBundle.getMessage( WebProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) }, //NOI18N
            { BUILD_CLASSES_DIR, NbBundle.getMessage( WebProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) }, //NOI18N
            { JAVAC_TEST_CLASSPATH, NbBundle.getMessage (WebProjectProperties.class,"LBL_JavacTestClasspath_DisplayName") }, //NOI18N
            { RUN_TEST_CLASSPATH, NbBundle.getMessage( WebProjectProperties.class, "LBL_RunTestClasspath_DisplayName" ) }, //NOI18N
            { BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (WebProjectProperties.class,"LBL_BuildTestClassesDir_DisplayName") } //NOI18N
        };
    
        public PathParser() {
            this(null);
        }

        public PathParser(String webLibraryElementName) {
            this.webLibraryElementName = webLibraryElementName;
        }

        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            Map warIncludesMap = createWarIncludesMap(antProjectHelper);
            if (raw != null) {
                String pe[] = PropertyUtils.tokenizePath( raw );
                for( int i = 0; i < pe.length; i++ ) {
                    final String pathItem = pe[i];
                    if (!warIncludesMap.containsKey(pathItem)) {
                        warIncludesMap.put(pathItem, VisualClassPathItem.PATH_IN_WAR_NONE);
                    }
                }
            }
            List cpItems = new ArrayList(warIncludesMap.size() );
            for (Iterator it = warIncludesMap.keySet().iterator(); it.hasNext();) {
                String pathItem = (String) it.next();
                String pathInWar = (String) warIncludesMap.get(pathItem);
                cpItems.add(createVisualClassPathItem(antProjectHelper, refHelper, pathItem, pathInWar));
            }
            return cpItems;
        }

        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            Element data = null;
            Element webModuleLibs = null;
            Document doc = null;
            if(webLibraryElementName != null) {
                data = antProjectHelper.getPrimaryConfigurationData(true);
                doc = data.getOwnerDocument();
                webModuleLibs = (Element) data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                    webLibraryElementName).item(0);
                //prevent NPE thrown from older projects
                if (webModuleLibs == null) {
                    webModuleLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, webLibraryElementName); //NOI18N
                    data.appendChild(webModuleLibs);
                }
                while (webModuleLibs.hasChildNodes()) {
                    webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
                }
            }
            StringBuffer sb = new StringBuffer();
            for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
                VisualClassPathItem visualClassPathItem = (VisualClassPathItem)it.next();
                String pathItem = getPathItem(visualClassPathItem, refHelper);
                if(webLibraryElementName != null) {
                    webModuleLibs.appendChild(createLibraryElement(doc, pathItem, visualClassPathItem));
                }
                //do not add applet libraries to classpath
                if (!VisualClassPathItem.PATH_IN_WAR_APPLET.equals (visualClassPathItem.getPathInWAR ())) {
                    sb.append(pathItem);
                    if ( it.hasNext() ) {
                        sb.append( File.pathSeparatorChar );
                    }
                }
            }
            if(webLibraryElementName != null) {
                antProjectHelper.putPrimaryConfigurationData(data, true);
            }
            return sb.toString();
        }

        private static Element createLibraryElement(Document doc, String pathItem,
                VisualClassPathItem visualClassPathItem) {
            Element libraryElement = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                    TAG_LIBRARY);
            Element webFile = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_FILE);
            libraryElement.appendChild(webFile);
            webFile.appendChild(doc.createTextNode(pathItem));
            if (visualClassPathItem.getPathInWAR() != VisualClassPathItem.PATH_IN_WAR_NONE) {
                Element pathInWar = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        TAG_PATH_IN_WAR);
                pathInWar.appendChild(doc.createTextNode(visualClassPathItem.getPathInWAR()));
                libraryElement.appendChild(pathInWar);
            }
            return libraryElement;
        }

        private Map createWarIncludesMap(AntProjectHelper antProjectHelper) {
            Map warIncludesMap = new LinkedHashMap();
            if (webLibraryElementName != null) {
                Element data = antProjectHelper.getPrimaryConfigurationData(true);
                final String ns = WebProjectType.PROJECT_CONFIGURATION_NAMESPACE;
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webLibraryElementName).item(0);
                NodeList ch = webModuleLibs.getChildNodes();
                for (int i = 0; i < ch.getLength(); i++) {
                    if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element library = (Element) ch.item(i);
                        Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                        NodeList pathInWarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_WAR); 
                        warIncludesMap.put(findText(webFile), pathInWarElements.getLength() > 0 ?
                                findText(pathInWarElements.item(0)) : VisualClassPathItem.PATH_IN_WAR_NONE);
                    }
                }
            }
            return warIncludesMap;
        }

        private static VisualClassPathItem createVisualClassPathItem(AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper, String pathItem, String pathInWar) {
            // First try to find out whether the item is well known classpath
            // in the J2SE project type
            for (int j = 0; j < WELL_KNOWN_PATHS.length; j++) {
                final String[] wellKnownPath = WELL_KNOWN_PATHS[j];
                if (wellKnownPath[0].equals(getAntPropertyName(pathItem))) {
                    return new VisualClassPathItem(pathItem, VisualClassPathItem.TYPE_CLASSPATH, pathItem,
                            wellKnownPath[1], pathInWar);
                }
            }
            if (pathItem.startsWith(LIBRARY_PREFIX)) {
                // Library from library manager
                // String eval = antProjectHelper.evaluate(getAntPropertyName(pathItem));
                String eval = pathItem.substring(LIBRARY_PREFIX.length(), pathItem.lastIndexOf('.')); //NOI18N
                Library lib = LibraryManager.getDefault().getLibrary(eval);
                if (lib != null) {
                    return new VisualClassPathItem(lib, VisualClassPathItem.TYPE_LIBRARY, pathItem, eval, pathInWar);
                } else {
                    return new VisualClassPathItem(null, VisualClassPathItem.TYPE_LIBRARY, pathItem, null,
                            pathInWar);
                }
            } else if (pathItem.startsWith(ANT_ARTIFACT_PREFIX)) {
                AntArtifact artifact = refHelper.getForeignFileReferenceAsArtifact(pathItem);
                if (artifact != null) {
                    // Sub project artifact
                    String eval = artifact.getArtifactLocation().toString();
                    return new VisualClassPathItem(artifact, VisualClassPathItem.TYPE_ARTIFACT, pathItem, eval,
                            pathInWar);
                } else {
                    return new VisualClassPathItem(null, VisualClassPathItem.TYPE_ARTIFACT, pathItem, null,
                            pathInWar);
                }
            } else {
                // Standalone jar or property
                String eval;
                if (isAntProperty(pathItem)) {
                    eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(getAntPropertyName(pathItem));
                } else {
                    eval = pathItem;
                }
                File f = (eval == null) ? null : antProjectHelper.resolveFile(eval);
                return new VisualClassPathItem(f, VisualClassPathItem.TYPE_JAR, pathItem, eval, pathInWar);
            }
        }

        private static String getPathItem(VisualClassPathItem vcpi, ReferenceHelper refHelper) {
            switch (vcpi.getType()) {
                case VisualClassPathItem.TYPE_JAR:
                    String pathItem = vcpi.getRaw();
                    if (pathItem == null) {
                        // New file
                        // pass null as expected artifact type to always get file reference
                        return refHelper.createForeignFileReference((File) vcpi.getObject(), null);
                    } else {
                        return pathItem;
                    }
                case VisualClassPathItem.TYPE_ARTIFACT:
                    if (vcpi.getObject() != null) {
                        return refHelper.createForeignFileReference((AntArtifact) vcpi.getObject());
                    } else {
                        return vcpi.getRaw();
                    }
                case VisualClassPathItem.TYPE_LIBRARY:
                case VisualClassPathItem.TYPE_CLASSPATH:
                    return vcpi.getRaw();
            }
            assert false: "unexpected type of classpath element";
            return null;
        }
    }
    
    /**
     * Extract nested text from a node.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent node
     * @return the nested text, or null if none was found
     */
    private static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    private static JavaPlatform findPlatform(String platformAntID) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();            
        for(int i = 0; i < platforms.length; i++) {
            String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name"); // NOI18N
            if (normalizedName != null && normalizedName.equals(platformAntID)) {
                return platforms[i];
            }
        }
        return null;
    }
    
    private static class PlatformParser extends PropertyParser {
        
        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            JavaPlatform platform = findPlatform(raw);
            if (platform != null) {
                return platform.getDisplayName();
            }
            // if platform does not exist then return raw reference.
            return raw;
        }
        
        public String encode(Object value, Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
                    new Specification ("j2se",null)); // NOI18N
            if (platforms.length == 0) {
                // platform for this project does not exist. broken reference? its displayname should 
                // correspond to platform ID. so just return it:
                return (String)value;
            } else {
                return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
            }
        }
    }
    
    private static class SourceRootsParser extends PropertyParser {

        public SourceRootsParser () {
        }

        public Object decode(String raw, Project project, AntProjectHelper antProjectHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
            SourceRoots roots = null;
            if (SOURCE_ROOTS.equals(raw)) {
                roots = ((WebProject) project).getSourceRoots();
            }
            else if (TEST_ROOTS.equals(raw)) {
                roots = ((WebProject) project).getTestSourceRoots();
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
