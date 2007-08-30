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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.JbiProjectType;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import java.text.Collator;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.projects.jbi.ComponentHelper;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectHelper;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;


/**
 * Helper class. Defines constants for properties. Knows the proper place where to store the
 * properties.
 *
 * @author Petr Hrebejk, Chris Webster
 */
public class JbiProjectProperties {
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_1_3 = "1.3"; // NOI18N
    
    // Special properties of the project
    
    /**
     * DOCUMENT ME!
     */
    public static final String EJB_PROJECT_NAME = "j2ee.jbi.name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES
    
    /**
     * root of external web module sources (full path), ".." if the sources are within project
     * folder
     */
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    //public static final String JAR_NAME = "jar.name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_ARGS = "javac.compilerargs"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SRC_DIR = "src.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String META_INF = "meta.inf"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_PRIVATE = "javadoc.private"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_TREE = "javadoc.notree"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_USE = "javadoc.use"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_NAVBAR = "javadoc.nonavbar"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_INDEX = "javadoc.noindex"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_SPLIT_INDEX = "javadoc.splitindex"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_AUTHOR = "javadoc.author"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_VERSION = "javadoc.version"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_WINDOW_TITLE = "javadoc.windowtitle"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_ENCODING = "javadoc.encoding"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_PREVIEW = "javadoc.preview"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_CONTENT_ADDITIONAL = "jbi.content.additional"; //NOI18N
    
    /**
     * Stores Java EE jars only
     */
    public static final String JBI_JAVAEE_JARS = "jbi.content.javaee.jars"; //NOI18N
    
    /**
     *
     */
    public static final String JBI_JAVAEE_RESOURCE_DIRS = "jbi.javaee.res.dirs"; //NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_CONTENT_COMPONENT = "jbi.content.component"; //NOI18N
    
    // Start Test Framework
    /**
     * DOCUMENT ME!
     */
    public static final String TEST_DIR = "test.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String TEST_RESULTS_DIR = "test.results.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SRC_BUILD_DIR = "src.build.dir"; // NOI18N
    
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    
    //================== Start of JBI  =====================================//
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String ASSEMBLY_UNIT_ALIAS = "org.netbeans.modules.compapp.jbiserver.alias.assembly-unit"; // NOI18N
    
    /**
     * @deprecated Use SERVICE_ASSEMBLY_ID instead.
     */
    public static final String ASSEMBLY_UNIT_UUID = "org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit"; // NOI18N
    public static final String SERVICE_ASSEMBLY_ID = "jbi.service-assembly.id"; // NOI18N
    
    /**
     * @deprecated Use SERVICE_ASSEMBLY_DESCRIPTION instead.
     */
    public static final String ASSEMBLY_UNIT_DESCRIPTION = "org.netbeans.modules.compapp.jbiserver.description.assembly-unit"; // NOI18N
    public static final String SERVICE_ASSEMBLY_DESCRIPTION = "jbi.service-assembly.description"; // NOI18N
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String APPLICATION_SUB_ASSEMBLY_ALIAS = "org.netbeans.modules.compapp.jbiserver.alias.application-sub-assembly"; // NOI18N
    
    /**
     * @deprecated use SERVICE_ASSEMBLY_DESCRIPTION instead.
     */
    public static final String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "org.netbeans.modules.compapp.jbiserver.description.application-sub-assembly"; // NOI18N
    public static final String SERVICE_UNIT_DESCRIPTION = "jbi.service-unit.description"; // NOI18N
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String JBI_COMPONENT_CONF_FILE = "org.netbeans.modules.compapp.jbiserver.component.conf.file"; // NOI18N
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String JBI_COMPONENT_CONF_ROOT = "org.netbeans.modules.compapp.jbiserver.component.conf.root"; // NOI18N
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String JBI_DEPLOYMENT_CONF_FILE = "org.netbeans.modules.compapp.jbiserver.deployment.conf.file"; // NOI18N
    
    /**
     * @deprecated Should no longer be used.
     */
    public static final String JBI_DEPLOYMENT_CONF_ROOT = "org.netbeans.modules.compapp.jbiserver.deployment.conf.root"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DISPLAY_NAME_PROPERTY_KEY = "com.sun.appserver.instance.displayName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HOST_NAME_PROPERTY_KEY = "com.sun.appserver.instance.hostName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.appserver.instance.administrationPort"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DOMAIN_PROPERTY_KEY = "com.sun.appserver.instance.domain"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.appserver.instance.httpMonitorOn"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.appserver.instance.httpPortNumber"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String LOCATION_PROPERTY_KEY = "com.sun.appserver.instance.location"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String PASSWORD_PROPERTY_KEY = "com.sun.appserver.instance.password"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String URL_PROPERTY_KEY = "com.sun.appserver.instance.url"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String USER_NAME_PROPERTY_KEY = "com.sun.appserver.instance.userName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_ROUTING = "com.sun.jbi.routing"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_SA_INTERNAL_ROUTING = "com.sun.jbi.sa.internal.routing"; // NOI18N
    
//    public static final String JBI_TARGET_COMPONENT_LIST_KEY ="com.sun.jbi.target.component.list"; // NOI18N
    
    //================== End of JBI  =======================================//
    // Shortcuts
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    private static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();    
    private static final CharsetParser CHARSET_PARSER = new CharsetParser();
    private static final PathParser PATH_PARSER = new PathParser();
    private static final PathParser SEMICOLON_PATH_PARSER = new SemiColonPathParser();
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    private static final StringListParser STRING_LIST_PARSER = new StringListParser();
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        {
            JAVAC_CLASSPATH,
                    NbBundle.getMessage(JbiProjectProperties.class, "LBL_JavacClasspath_DisplayName") // NOI18N
        },
        {
            BUILD_CLASSES_DIR,
                    NbBundle.getMessage(JbiProjectProperties.class, "LBL_BuildClassesDir_DisplayName") // NOI18N
        }
    };
    
    /*
       private static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
       private final PropertyParser WAR_CONTENT_ADDITIONAL_PARSER =
               new JbiPathParser(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
     */
    
    // Info about the property destination
    // XXX only properties which are visually set should be described here
    // XXX refactor this list
    private PropertyDescriptor[] PROPERTY_DESCRIPTORS = {
        new PropertyDescriptor(EJB_PROJECT_NAME, null, STRING_PARSER),
        new PropertyDescriptor(J2EE_PLATFORM, PROJECT, STRING_PARSER),
        
        new PropertyDescriptor(SOURCE_ROOT, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_FILE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_JAR, PROJECT, PATH_PARSER),
        new PropertyDescriptor(JAVAC_CLASSPATH, PROJECT, PATH_PARSER),
        new PropertyDescriptor(DEBUG_CLASSPATH, PROJECT, PATH_PARSER),
        
        //new PropertyDescriptor(JAR_NAME, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAR_COMPRESS, PROJECT, BOOLEAN_PARSER),
        
        new PropertyDescriptor(J2EE_SERVER_TYPE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JAVAC_SOURCE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVAC_TARGET, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVAC_ARGS, PROJECT, STRING_PARSER),
        new PropertyDescriptor(SRC_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(META_INF, PROJECT, PATH_PARSER),
        new PropertyDescriptor(BUILD_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_CLASSES_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_JAVADOC_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVA_PLATFORM, PROJECT, PLATFORM_PARSER),
        
        new PropertyDescriptor(JAVADOC_PRIVATE, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_TREE, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_USE, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_NAVBAR, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_INDEX, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_SPLIT_INDEX, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_AUTHOR, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_VERSION, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_WINDOW_TITLE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVADOC_ENCODING, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVADOC_PREVIEW, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(SOURCE_ENCODING, PROJECT, CHARSET_PARSER),
        
        // This should be OS-agnostic
        new PropertyDescriptor(JBI_CONTENT_ADDITIONAL, PROJECT, SEMICOLON_PATH_PARSER),
        new PropertyDescriptor(JBI_JAVAEE_JARS, PROJECT, SEMICOLON_PATH_PARSER),
        new PropertyDescriptor(JBI_JAVAEE_RESOURCE_DIRS, PROJECT, STRING_LIST_PARSER),
        new PropertyDescriptor(JBI_CONTENT_COMPONENT, PROJECT, STRING_LIST_PARSER),
        
        // Start Test Framework
        new PropertyDescriptor(TEST_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(TEST_RESULTS_DIR, PROJECT, STRING_PARSER),
        // End Test Framework
        
        //================== Start of JBI  =====================================//
        new PropertyDescriptor(JBI_ROUTING, PROJECT, STRING_PARSER),
        new PropertyDescriptor(SERVICE_ASSEMBLY_ID, PROJECT, STRING_PARSER),
        new PropertyDescriptor(SERVICE_ASSEMBLY_DESCRIPTION, PROJECT, STRING_PARSER),
        new PropertyDescriptor(SERVICE_UNIT_DESCRIPTION, PROJECT, STRING_PARSER),
//        new PropertyDescriptor(JBI_COMPONENT_CONF_ROOT, PROJECT, STRING_PARSER),
//        new PropertyDescriptor(JBI_DEPLOYMENT_CONF_ROOT, PROJECT, STRING_PARSER),
//        new PropertyDescriptor(JBI_COMPONENT_CONF_FILE, PROJECT, STRING_PARSER),
//            new PropertyDescriptor(JBI_TARGET_COMPONENT_LIST_KEY, PROJECT, STRING_PARSER),
        
//        new PropertyDescriptor(JBI_DEPLOYMENT_CONF_FILE, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(DISPLAY_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HOST_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(ADMINISTRATION_PORT_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(DOMAIN_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HTTP_MONITOR_ON_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HTTP_PORT_NUMBER_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(LOCATION_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(PASSWORD_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(URL_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(USER_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        //new PropertyDescriptor(ASSEMBLY_UNIT_GUID_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JBI_REGISTRY_COMPONENT_FILE_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JBI_REGISTRY_BROKER_HOST_KEY, PRIVATE, STRING_PARSER),
        //================== End of JBI  =======================================//
    };
    
    // Private fields ----------------------------------------------------------
    private JbiProject project;
    private HashMap<String, PropertyInfo> properties;
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    private AntBasedProjectType abpt;
    private List<VisualClassPathItem> bindingList = new Vector();
    private List<AntArtifact> sunresourceProjs;
    javax.swing.text.Document DIST_JAR_MODEL;
    
    /**
     * Creates a new JbiProjectProperties object.
     *
     * @param project DOCUMENT ME!
     * @param antProjectHelper DOCUMENT ME!
     * @param refHelper DOCUMENT ME!
     */
    public JbiProjectProperties(
            JbiProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
            ) {
        this.project = project;
        this.properties = new HashMap<String, PropertyInfo>();
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        this.abpt = project.getAntBasedProjectType();
        read();
        
        PropertyEvaluator evaluator = antProjectHelper.getStandardPropertyEvaluator();
        StoreGroup projectGroup = new StoreGroup();
        DIST_JAR_MODEL = projectGroup.createStringDocument(evaluator, DIST_JAR);
    }
    
    /**
     * XXX to be deleted when introduced in AntPropertyHeleper API
     *
     * @param property DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    static String getAntPropertyName(String property) {
        if ((property != null) && property.startsWith("${") && // NOI18N
                property.endsWith("}")) { // NOI18N
            
            return property.substring(2, property.length() - 1);
        } else {
            return property;
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<VisualClassPathItem> getBindingList() {
        return bindingList;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void put(String propertyName, Object value) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        //        if (JAVAC_CLASSPATH.equals (propertyName)) {
        //            assert value instanceof List : "Wrong format of property " + propertyName; //NOI18N
        //            writeJavacClasspath ((List) value, antProjectHelper, refHelper);
        //        }
        PropertyInfo pi = properties.get(propertyName);
        
        if (pi == null) {
            PropertyDescriptor pd = null;
            
            for (int i = 0; i < PROPERTY_DESCRIPTORS.length; i++) {
                pd = PROPERTY_DESCRIPTORS[i];
                
                if (pd.name.compareTo(propertyName) == 0) {
                    break;
                }
                
                pd = null;
            }
            
            if (pd == null) {
                return;
            }
            
            // todo: assuming the new prop value is string...
            pi = new PropertyInfo(pd, (String) value, (String) value);
            properties.put(pd.name, pi);
        }
        
        pi.setValue(value);
        
        if (J2EE_SERVER_INSTANCE.equals(propertyName)) {
            put(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID((String) value));
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object get(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        //        if (JAVAC_CLASSPATH.equals (propertyName)) {
        //            return readJavacClasspath (antProjectHelper, refHelper);
        //        }
        PropertyInfo pi = properties.get(propertyName);
        
        if (pi == null) {
            return null;
        }
        
        return pi.getValue();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isModified(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        PropertyInfo pi = properties.get(propertyName);
        
        if (pi == null) {
            return false;
        }
        
        return pi.isModified();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getSortedSubprojectsList() {
        List subprojects = new ArrayList(5);
        addSubprojects(project, subprojects); // Find the projects recursively
        
        // Replace projects in the list with formated names
        for (int i = 0; i < subprojects.size(); i++) {
            Project p = (Project) subprojects.get(i);
            subprojects.set(i, ProjectUtils.getInformation(p).getDisplayName());
        }
        
        // Sort the list
        Collections.sort(subprojects, Collator.getInstance());
        
        return subprojects;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    JbiProject getProject() {
        return project;
    }
    
    /**
     * Gets all subprojects recursively
     *
     * @param project DOCUMENT ME!
     * @param result DOCUMENT ME!
     */
    private void addSubprojects(Project project, List<Project> result) {
        SubprojectProvider spp = 
                project.getLookup().lookup(SubprojectProvider.class);
        
        if (spp == null) {
            return;
        }
        
        for (Project sp : spp.getSubprojects()) {
            if (!result.contains(sp)) {
                result.add(sp);
                addSubprojects(sp, result);
            }
        }
    }
    
    /**
     * Reads all the properties of the project and converts them to objects suitable for usage in
     * the GUI controls.
     */
    private void read() {
        // Read the properties from the project
        Map<String, EditableProperties> eProps = new HashMap<String, EditableProperties>(2);
        eProps.put(PROJECT, antProjectHelper.getProperties(PROJECT));
        eProps.put(PRIVATE, antProjectHelper.getProperties(PRIVATE));
        
        // Initialize the property map with objects
        for (PropertyDescriptor pd : PROPERTY_DESCRIPTORS) {            
            if (pd.dest == null) {
                // Specialy handled properties
                if (EJB_PROJECT_NAME.equals(pd.name)) {
                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
                    properties.put(pd.name, new PropertyInfo(pd, projectName, projectName));
                }
            } else {
                // Standard properties
                String raw = ((EditableProperties) eProps.get(pd.dest)).getProperty(pd.name);
                String eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(pd.name);
                properties.put(pd.name, new PropertyInfo(pd, raw, eval));
            }
        }
    }
    
    public void addSunResourceProject(AntArtifact aa){
        if (this.sunresourceProjs == null){
            this.sunresourceProjs = new ArrayList<AntArtifact>();
        }
        this.sunresourceProjs.add(aa);
    }
    
    public void removeSunResourceProject(AntArtifact aa){
        if (this.sunresourceProjs != null){
            this.sunresourceProjs.remove(aa);
        }
    }
    
    /**
     * Transforms all the Objects from GUI controls into String Ant  properties and stores them in
     * the project
     */
    public void store() {
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    resolveProjectDependencies();
                    
                    // Some properties need special handling e.g. if the
                    // property changes the project.xml files
                    for (PropertyInfo pi : properties.values()) {
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        pi.encode();
                        
                        String newValueEncoded = pi.getNewValueEncoded();
                        
                        if ((pd.dest == null) && (newValueEncoded != null)) {
                            // Specialy handled properties
                            if (EJB_PROJECT_NAME.equals(pd.name)) {
                                assert false : "No support yet for changing name of EJBProject; cf. EJBProject.setName"; // NOI18N
                            }
                        }
                        
                        if (JAVA_PLATFORM.equals(pd.name) && (newValueEncoded != null)) {
                            setPlatform(
                                    pi.getNewValueEncoded().equals(
                                    JavaPlatformManager.getDefault().getDefaultPlatform()
                                    .getProperties().get(
                                    "platform.ant.name" // NOI18N
                                    )
                                    )
                                    );
                        }
                    }
                    
                    try {
                        updateAssemblyInfoAndCasa();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    Map<String, EditableProperties> eProps = 
                            new HashMap<String, EditableProperties>(2);
                    eProps.put(PROJECT, antProjectHelper.getProperties(PROJECT));
                    eProps.put(PRIVATE, antProjectHelper.getProperties(PRIVATE));
                    
                    // Set the changed properties
                    for (PropertyInfo pi : properties.values()) {
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        String newValueEncoded = pi.getNewValueEncoded();
                        
                        if (newValueEncoded != null) {
                            if (pd.dest != null) {
                                // Standard properties
                                eProps.get(pd.dest).setProperty(pd.name, newValueEncoded);
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    antProjectHelper.putProperties(PROJECT, eProps.get(PROJECT));
                    antProjectHelper.putProperties(PRIVATE, eProps.get(PRIVATE));
                    ProjectManager.getDefault().saveProject(project);
                    
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
            }
            );
            
            if (this.sunresourceProjs != null){
                Iterator<AntArtifact> itr = this.sunresourceProjs.iterator();
                AntArtifact aa = null;
                while (itr.hasNext()){
                    aa = itr.next();
                    SunResourcesUtil.addJavaEEResourceMetaData(this.getProject(), aa);
                }
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    private void updateAssemblyInfoAndCasa() throws Exception {        
        saveAssemblyInfo();
        CasaHelper.updateCasaWithJBIModules(project, this);
    }
    
    
    private void setPlatform(boolean isDefault) {
        Element pcd = antProjectHelper.getPrimaryConfigurationData(true);
        
        NodeList sps = pcd.getElementsByTagName("explicit-platform"); // NOI18N
        
        if (isDefault && (sps.getLength() > 0)) {
            pcd.removeChild(sps.item(0));
        } else if (!isDefault && (sps.getLength() == 0)) {
            pcd.appendChild(pcd.getOwnerDocument().createElement("explicit-platform")); // NOI18N
        }
        
        antProjectHelper.putPrimaryConfigurationData(pcd, true);
    }
    
    /**
     * Finds out what are new and removed project dependencies and  applyes the info to the project
     */
    private void resolveProjectDependencies() {
        String[] allPaths = {JBI_CONTENT_ADDITIONAL}; // JAVAC_CLASSPATH,  DEBUG_CLASSPATH };
        
        // Create a set of old and new artifacts.
        Set<VisualClassPathItem> oldArtifacts = new HashSet<VisualClassPathItem>();
        Set<VisualClassPathItem> newArtifacts = new HashSet<VisualClassPathItem>();
        
        for (int i = 0; i < allPaths.length; i++) {
            PropertyInfo pi = properties.get(allPaths[i]);
            
            // Get original artifacts
            List<VisualClassPathItem> oldList = (List) pi.getOldValue();
            if (oldList != null) {
                for (VisualClassPathItem vcpi : oldList) {
                    if (vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT) {
                        oldArtifacts.add(vcpi);
                    }
                }
            }
            
            // Get artifacts after the edit
            List<VisualClassPathItem> newList = (List) pi.getValue();
            if (newList != null) {
                for (VisualClassPathItem vcpi : newList) {
                    if (vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT) {
                        newArtifacts.add(vcpi);
                    }
                }
            }
        }
        
        // Create set of removed artifacts and remove them
        Set<VisualClassPathItem> removed =
                new HashSet<VisualClassPathItem>(oldArtifacts);
        removed.removeAll(newArtifacts);
        
        for (VisualClassPathItem vcpi : removed) {
            refHelper.destroyReference(vcpi.getRaw());
        }
    }
    
    // AssemblyInfo methods ------------------------------------
    private static Element generateIdentificationElement(Document document, 
            String name, String description) {
        Element idElement = document.createElement("identification"); // NOI18N
        
        // Name
        Element nameElement = document.createElement("name"); // NOI18N
        nameElement.appendChild(document.createTextNode(name));
        idElement.appendChild(nameElement);
        
        // Description
        Element descElement = document.createElement("description"); // NOI18N
        descElement.appendChild(document.createTextNode(description));
        idElement.appendChild(descElement);
        
        return idElement;
    }
    
    private static Element generateTargetElement(Document document, 
            String artifactsZip, String componentName) {
        Element targetElement = document.createElement("target"); // NOI18N
        
        // artifacts-zip
        Element artifactElement = document.createElement("artifacts-zip"); // NOI18N
        artifactElement.appendChild(document.createTextNode(artifactsZip));
        targetElement.appendChild(artifactElement);
        
        // component-name
        Element compNameElement = document.createElement("component-name"); // NOI18N
        compNameElement.appendChild(document.createTextNode(componentName));
        targetElement.appendChild(compNameElement);
        
        return targetElement;
    }
        
    private static Element generateServiceUnitElement(JbiProject jbiProject,
            Document document, VisualClassPathItem vi, String target, 
            boolean isEngine) {
        Element suElement = document.createElement("service-unit"); // NOI18N
        
        String desc = vi.getAsaDescription();
        String shortName = vi.getShortName();
        AntArtifact aa = vi.getAntArtifact();
        
        if (desc == null) { // if needed, use default one...
            desc = JbiProjectHelper.getServiceUnitDescription(jbiProject);
            vi.setAsaDescription(desc);
        }
                
        vi.setAsaTarget(target);
        
        String jbiProjName = jbiProject.getName();
        String suName;
        String suJarName;
        
        if (isEngine) {
            String suProjName = vi.getProjectName();
            suName = jbiProjName + "-" + suProjName; // NOI18N
            suJarName = suProjName + ".jar"; // e.x., SynchronousSample.jar // NOI18N
        } else {
            suName = jbiProjName + "-" + target; // NOI18N
            suJarName = target + ".jar"; // e.x., sun-http-binding.jar // NOI18N
        }
        
        Element identificationElement = 
                generateIdentificationElement(document, suName, desc);
        suElement.appendChild(identificationElement);
        
        Element targetElement = 
                generateTargetElement(document, suJarName, target);
        suElement.appendChild(targetElement);
        
        return suElement;
    }
    
    private List<VisualClassPathItem> loadBindingComponentInfo(String compFileDst) {
        List<VisualClassPathItem> bindingList = new ArrayList<VisualClassPathItem>();
        AntArtifact bcjar = antProjectHelper.createSimpleAntArtifact(
                "CAPS.jbi:bpelse", "build/BCDeployment.jar", // NOI18N
                antProjectHelper.getStandardPropertyEvaluator(), "dist_bc", "clean" // NOI18N
                );
        
        try {
            File dst = new File(compFileDst);
            
            if (dst.exists()) {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
                List compList = compDoc.getJbiComponentList();
                Iterator iterator = compList.iterator();
                JBIComponentStatus component = null;
                
                // Added compNames Set to avoid duplicate entries in ASI.xml
                // caused by problems due to "incorrect" order of NB 5.5 to 6.0
                // upgrade and component name changes.
                Set<String> compNames = new HashSet<String>();
                
                while ((iterator != null) && (iterator.hasNext() == true)) {
                    component = (JBIComponentStatus) iterator.next();
                    
                    String compName = component.getName();
                    
                    if (!compNames.contains(compName)) {
                        compNames.add(compName);
                        
                        // update the target combo model..
                        if (component.getType().compareToIgnoreCase("Binding") == 0) { // NOI18N
                            VisualClassPathItem vi = new VisualClassPathItem(
                                    bcjar, VisualClassPathItem.TYPE_ARTIFACT, "BCDeployment.jar", null, // NOI18N
                                    true
                                    );
                            vi.setAsaTarget(component.getName());
                            bindingList.add(vi);
                            
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        
        return bindingList;
    }
    
    /**
     * DOCUMENT ME!
     */
    public void saveAssemblyInfo() {
        List os = (List) this.get(JbiProjectProperties.META_INF);
        String compFileDst = null;
        String jbiFileLoc = null;
        
        if ((os != null) && (os.size() > 0)) {
            String path = FileUtil.toFile(project.getProjectDirectory()).getPath() + "/" + os.get(0).toString(); // NOI18N
            /*
            if ((path.indexOf(':') < 0) && (!path.startsWith("/"))) {
                path = "/" + path; // In unix, it returns an incorrect path..
            }
             */
            compFileDst = path + "/" + "ComponentInformation.xml"; // NOI18N
            jbiFileLoc = path + "/" + "AssemblyInformation.xml"; // NOI18N
        }
        
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("jbi"); // NOI18N
            root.setAttribute("version", "1.0"); // NOI18N
            root.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi"); // NOI18N
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
            root.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/jbi ./jbi.xsd"); // NOI18N
            document.appendChild(root);
            
            // Service Assembly ...
            Element saElement = document.createElement("service-assembly"); // NOI18N
            
            Element identificationElement = generateIdentificationElement(
                    document, 
                    JbiProjectHelper.getJbiProjectName(project),
                    JbiProjectHelper.getServiceAssemblyDescription(project));
            saElement.appendChild(identificationElement);
            
            // for each SE jar..
            List<VisualClassPathItem> items =
                    (List) this.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            List<String> targetIDs =
                    (List) this.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);
            
            assert items.size() == targetIDs.size() : 
                "Corrupted project.properties file: mismatching service unit artifacts and target components."; // NOI18N
                      
            for (int i = 0, size = items.size(); i < size; i++) {
                VisualClassPathItem vi = items.get(i);
                String targetID = targetIDs.get(i);
                assert (vi != null) && (targetID != null);                
                Element sesuElement = generateServiceUnitElement(
                        project, document, vi, targetID, true);
                saElement.appendChild(sesuElement);
            }
            
            // for each BC jar...
            bindingList = loadBindingComponentInfo(compFileDst);            
            for (VisualClassPathItem vi : bindingList) {
                String targetID = vi.getAsaTarget();                
                if (vi != null && targetID != null && vi.isInDeployment()) {
                    Element bcsuElement = generateServiceUnitElement(
                            project, document, vi, targetID, false);
                    saElement.appendChild(bcsuElement);
                }
            }
            
            root.appendChild(saElement);
            document.getDocumentElement().normalize();
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(jbiFileLoc));
            
            //tFactory.setAttribute("indent-number", new Integer(4));
            // indent the output to make it more legible...
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); // NOI18N
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            
            transformer.transform(source, result);
            
        } catch (Exception e) {
            //ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            e.printStackTrace();
        }
    }
    
    public void fixComponentTargetList() {
        
        List<VisualClassPathItem> items =
                (List) this.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        List<String> targetIDs =
                (List) this.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);
        
        boolean fixNeeded = false;
        if (items.size() != targetIDs.size()) {
            fixNeeded = true;
        } else {
            for (String targetID : targetIDs) {
                if (targetID.startsWith("com.sun.") || targetID.equals("JavaEEServiceEngine")) { // NOI18N
                    fixNeeded = true;
                    break;
                }
            }
        }
        
        if (fixNeeded) {
            List<String> newTargetIDs = new ArrayList<String>();
            
            ComponentHelper componentHelper = new ComponentHelper(project);
            
            for (VisualClassPathItem item : items) {
                String asaType = item.getAsaType(); // sun-bpel-engine, or old com.sun.bpelse
                String target = componentHelper.getDefaultTarget(asaType);
                if (target == null) {
                    throw new RuntimeException("Unknown component target name for asaType of \"" + asaType + "\".");
                }
                newTargetIDs.add(target);
            }
            
            put(JBI_CONTENT_COMPONENT, newTargetIDs);
            store();
        }
    }
    
    /**
     * Extract nested text from an element. Currently does not handle coalescing text nodes, CDATA
     * sections, etc.
     *
     * @param parent a parent element
     *
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) l.item(i);
                
                return text.getNodeValue();
            }
        }
        
        return null;
    }
    
    private static List librariesInDeployment(AntProjectHelper helper) {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList libs = data.getElementsByTagNameNS(
                JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                );
        List cpItems = new ArrayList(libs.getLength());
        
        for (int i = 0; i < libs.getLength(); i++) {
            Element library = (Element) libs.item(i);
            cpItems.add(findText(library));
        }
        
        return cpItems;
    }
    
    private class PropertyInfo {
        private PropertyDescriptor propertyDesciptor;
        private String rawValue;
        private String evaluatedValue;
        private Object value;
        private Object newValue;
        private String newValueEncoded;
        
        /**
         * Creates a new PropertyInfo object.
         *
         * @param propertyDesciptor DOCUMENT ME!
         * @param rawValue DOCUMENT ME!
         * @param evaluatedValue DOCUMENT ME!
         */
        public PropertyInfo(
                PropertyDescriptor propertyDesciptor, String rawValue, String evaluatedValue
                ) {
            this.propertyDesciptor = propertyDesciptor;
            this.rawValue = rawValue;
            this.evaluatedValue = evaluatedValue;
            this.value = propertyDesciptor.parser.decode(rawValue, antProjectHelper, refHelper);
            this.newValue = null;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        /**
         * DOCUMENT ME!
         */
        public void encode() {
            if (isModified()) {
                newValueEncoded = propertyDesciptor.parser.encode(
                        newValue, antProjectHelper, refHelper, getOldValue()
                        );
            } else {
                newValueEncoded = null;
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getValue() {
            return isModified() ? newValue : value;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         */
        public void setValue(Object value) {
            newValue = value;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getNewValueEncoded() {
            return newValueEncoded;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean isModified() {
            return newValue != null;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getOldValue() {
            return value;
        }
    }
    
    private static class PropertyDescriptor {
        /**
         * DOCUMENT ME!
         */
        final PropertyParser parser;
        
        /**
         * DOCUMENT ME!
         */
        final String name;
        
        /**
         * DOCUMENT ME!
         */
        final String dest;
        
        /**
         * Creates a new PropertyDescriptor object.
         *
         * @param name DOCUMENT ME!
         * @param dest DOCUMENT ME!
         * @param parser DOCUMENT ME!
         */
        PropertyDescriptor(String name, String dest, PropertyParser parser) {
            this.name = name;
            this.dest = dest;
            this.parser = parser;
        }
    }
    
    private static abstract class PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public abstract Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                );
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public abstract String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                );
        
        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, Object oldValue) {
            return encode(value, antProjectHelper, refHelper);
        };
        
    }
    
    private static class StringParser extends PropertyParser {
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
            return raw;
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
            return (String) value;
        }
    }
    
    private static class BooleanParser extends PropertyParser {
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
            if (raw != null) {
                String lowecaseRaw = raw.toLowerCase();
                
                if (
                        lowecaseRaw.equals("true") || lowecaseRaw.equals("yes") || // NOI18N
                        lowecaseRaw.equals("enabled") // NOI18N
                        ) {
                    return Boolean.TRUE;
                }
            }
            
            return Boolean.FALSE;
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
            return ((Boolean) value).booleanValue() ? "true" : "false"; // NOI18N
        }
    }
    
    private static class InverseBooleanParser extends BooleanParser {
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
            return ((Boolean) super.decode(raw, antProjectHelper, refHelper)).booleanValue()
            ? Boolean.FALSE : Boolean.TRUE;
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
            return super.encode(
                    ((Boolean) value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper,
                    refHelper
                    );
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
    
    private static class PlatformParser extends PropertyParser {
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
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            
            for (int i = 0; i < platforms.length; i++) {
                String normalizedName = platforms[i].getProperties().get(
                        "platform.ant.name" // NOI18N
                        );
                
                if ((normalizedName != null) && normalizedName.equals(raw)) {
                    return platforms[i].getDisplayName();
                }
            }
            
            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
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
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                    (String) value, new Specification("j2se", null) // NOI18N
                    );
            
            if (platforms.length == 0) {
                return null;
            } else {
                return platforms[0].getProperties().get("platform.ant.name"); //NOI18N
            }
        }
    }
    
    private static class PathParser extends PropertyParser {
        
        protected String getPathSeparator() {
            return File.pathSeparator;
        }
        
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
            if ((raw == null) || (raw.trim().length() < 1)) {
                return new ArrayList();
            }
            
            EditableProperties ep = antProjectHelper.getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH
                    );
            String classpath = raw; // ep.getProperty(JbiProjectProperties.JAVAC_CLASSPATH);
            
            if (classpath == null) {
                return new ArrayList();
            }
            
            String[] classPathElement = classpath.split(getPathSeparator());
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
                
                for (int j = 0; j < WELL_KNOWN_PATHS.length; j++) {
                    if (WELL_KNOWN_PATHS[j][0].equals(propertyName)) {
                        wellKnownPathIndex = j;
                        
                        break;
                    }
                }
                
                if (wellKnownPathIndex != -1) {
                    cpItem = new VisualClassPathItem(
                            file, VisualClassPathItem.TYPE_CLASSPATH, file,
                            WELL_KNOWN_PATHS[wellKnownPathIndex][1], inDeployment
                            );
                } else if (file.startsWith(LIBRARY_PREFIX)) {
                    // Library from library manager
                    String eval = file.substring(LIBRARY_PREFIX.length(), file.lastIndexOf('.')); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary(eval);
                    
                    if (lib != null) {
                        cpItem = new VisualClassPathItem(
                                lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, inDeployment
                                );
                    } else {
                        //Invalid library. The lbirary was probably removed from system.
                        cpItem = null;
                    }
                } else {
                    Object os[] = refHelper.findArtifactAndLocation( file );
                    if ((os != null) && (os.length > 0) ) {
                        AntArtifact artifact = (AntArtifact) os[0];
                        // Sub project artifact
                        String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(
                                file
                                );
                        cpItem = new VisualClassPathItem(
                                artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval,
                                inDeployment
                                );
                    } else {
                        // Standalone jar or property
                        String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(
                                file
                                );
                        cpItem = new VisualClassPathItem(
                                file, VisualClassPathItem.TYPE_JAR, file, eval, inDeployment
                                );
                    }
                }
                
                if (cpItem != null) {
                    cpItems.add(cpItem);
                }
            }
            
            return cpItems;
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
            return encode(value, antProjectHelper, refHelper, value);
        }
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, Object oldValue
                ) {
            StringBuffer sb = new StringBuffer();
            Element data = antProjectHelper.getPrimaryConfigurationData(true);
            org.w3c.dom.Document doc = data.getOwnerDocument();
            NodeList libs = data.getElementsByTagNameNS(
                    JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                    ); //NOI18N
            
            // 03/24/05, fixed a bug in removing libray entries
            int ns = libs.getLength();
            
            for (int i = ns; i > 0; i--) {
                Node n = libs.item(i - 1);
                n.getParentNode().removeChild(n);
            }
            
            if (value != null) {
                List<VisualClassPathItem> removedItemsList = new ArrayList<VisualClassPathItem>();
                for (VisualClassPathItem vcpi : (List<VisualClassPathItem>) oldValue) {
                    if(((List) value).indexOf(vcpi) == -1) {  // If the newValue doesn't contain any oldValue element, then
                        removedItemsList.add(vcpi);           // that element got removed
                    }
                }
                
                for (VisualClassPathItem vcpi : removedItemsList) {   //Remove the references
                    switch (vcpi.getType()) {
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        refHelper.destroyReference(vcpi.getRaw());
                        break;
                    }
                }
            }
            
            String pathSeparator = getPathSeparator();
            
            for (VisualClassPathItem vcpi : (List<VisualClassPathItem>) value) {
                
                String library_tag_value = ""; // NOI18N
                
                switch (vcpi.getType()) {
                                        case VisualClassPathItem.TYPE_JAR:
                        
                        String raw = vcpi.getRaw();
                        
                        if (raw == null) {
                            // New file
                            File file = (File) vcpi.getObject();
                            String reference = refHelper.createForeignFileReference(
                                    file, JavaProjectConstants.ARTIFACT_TYPE_JAR
                                    );
                            library_tag_value = reference;
                        } else {
                            // Existing property
                            library_tag_value = raw;
                        }
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_LIBRARY:
                        library_tag_value = vcpi.getRaw();
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        
                        AntArtifact aa = (AntArtifact) vcpi.getObject();
                        // String reference = refHelper.addReference( aa, null );
                        String reference = aa == null ? vcpi.getRaw() : // prevent NPE thrown from older projects
                            refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                        library_tag_value = reference;
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_CLASSPATH:
                        library_tag_value = vcpi.getRaw();
                        
                        break;
                }
                
                sb.append(library_tag_value);
                sb.append(pathSeparator);
                
                if (vcpi.isInDeployment()) {
                    Element library = doc.createElementNS(
                            JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                            );
                    library.appendChild(doc.createTextNode(getAntPropertyName(library_tag_value)));
                    data.appendChild(library);
                }
            }
            
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            
            antProjectHelper.putPrimaryConfigurationData(data, true);
            
            return sb.toString();
        }
    }
    
    private static class SemiColonPathParser extends PathParser {
        protected String getPathSeparator() {
            return ";";
        }
    }
    
    private static class StringListParser extends PropertyParser {
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            if ((raw == null) || (raw.trim().length() < 1)) {
                return new ArrayList();
            }
            String[] result = raw.split(";");
            return Arrays.asList(result);
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            List<String> list = (List<String>) value;
            String result = "";
            for (Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
                String str = iter.next();
                if (iter.hasNext()) {
                    result = result + str + ";";
                } else {
                    result = result + str;
                }
            }
            return result;
        }
        
    }
}
