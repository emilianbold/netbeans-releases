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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.earproject.BrokenProjectSupport;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.UpdateHelper;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class. Defines constants for properties. Knows the proper
 * place where to store the properties.
 *
 * @author Petr Hrebejk
 */
public final class EarProjectProperties {
    
    public static final String J2EE_SPEC_14_LABEL =
            NbBundle.getMessage(EarProjectProperties.class, "J2EESpecLevel_14");
    public static final String JAVA_EE_SPEC_50_LABEL =
            NbBundle.getMessage(EarProjectProperties.class, "JavaEESpecLevel_50");
    
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
    public static final String DIST_JAR = "dist.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String JAR_NAME = "jar.name"; //NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String JAR_CONTENT_ADDITIONAL = "jar.content.additional"; //NOI18N
    
    public static final String APPLICATION_CLIENT = "app.client"; // NOI18N
    public static final String APPCLIENT_MAIN_CLASS = "main.class"; // NOI18N
    public static final String APPCLIENT_ARGS = "application.args"; // NOI18N
    public static final String APPCLIENT_JVM_OPTIONS = "j2ee.appclient.jvmoptions"; // NOI18N
    public static final String APPCLIENT_MAINCLASS_ARGS = "j2ee.appclient.mainclass.args"; // NOI18N
    
    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String META_INF = "meta.inf"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N
    
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
    
    public static final String CLIENT_NAME = "j2ee.clientName"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    
    public static final String APPCLIENT_TOOL_RUNTIME = "j2ee.appclient.tool.runtime"; // NOI18N
    public static final String APPCLIENT_TOOL_MAINCLASS = "j2ee.appclient.tool.mainclass"; // NOI18N
    public static final String APPCLIENT_TOOL_JVMOPTS = "j2ee.appclient.tool.jvmoptions";  // NOI18N
    public static final String APPCLIENT_TOOL_ARGS = "j2ee.appclient.tool.args"; // NOI18N
    
    /**
     * "API" contract between Application Client and Glassfish plugin's
     * J2eePlatformImpl implementation.
     */
    private static final String J2EE_PLATFORM_APPCLIENT_ARGS = "j2ee.appclient.args"; // NOI18N
    
    static final String APPCLIENT_WA_COPY_CLIENT_JAR_FROM = "wa.copy.client.jar.from"; // NOI18N
    
    // Shortcuts 
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    
//    public static final String TAG_WEB_MODULE_LIBRARIES = "j2ee-module-libraries"; // NOI18N
//    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "j2ee-module-additional-libraries"; //NOI18N
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; // NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N

    
    static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private final PropertyParser PATH_PARSER = new PathParser();
    private final PropertyParser JAVAC_CLASSPATH_PARSER = new PathParser(TAG_WEB_MODULE_LIBRARIES);
    private final PropertyParser JAR_CONTENT_ADDITIONAL_PARSER =
            new PathParser(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    
    // Info about the property destination
    private final PropertyDescriptor PROPERTY_DESCRIPTORS[] = {
        new PropertyDescriptor( WEB_PROJECT_NAME, null, STRING_PARSER ),
        new PropertyDescriptor( J2EE_PLATFORM, PROJECT, STRING_PARSER ),
                
        new PropertyDescriptor( SOURCE_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_FILE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( LIBRARIES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, JAVAC_CLASSPATH_PARSER ),
        new PropertyDescriptor( COMPILE_JSPS, PROJECT, BOOLEAN_PARSER ),
        //new PropertyDescriptor( JSP_COMPILER_CLASSPATH, PRIVATE, PATH_PARSER ),
        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( RUN_CLASSPATH, PROJECT, PATH_PARSER ),

        new PropertyDescriptor( JAR_NAME, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAR_COMPRESS, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAR_CONTENT_ADDITIONAL, PROJECT, JAR_CONTENT_ADDITIONAL_PARSER ),
        
        new PropertyDescriptor( APPLICATION_CLIENT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( APPCLIENT_MAIN_CLASS, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( APPCLIENT_JVM_OPTIONS, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( APPCLIENT_ARGS, PRIVATE, STRING_PARSER ),
        
        new PropertyDescriptor( LAUNCH_URL_RELATIVE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( CLIENT_MODULE_URI, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DISPLAY_BROWSER, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_TYPE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_SOURCE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER ),       
        new PropertyDescriptor( JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVAC_TARGET, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( RESOURCE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( WEB_DOCBASE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( NO_DEPENDENCIES, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
        new PropertyDescriptor( DEPLOY_ANT_PROPS_FILE, PRIVATE, STRING_PARSER ),
        
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
    };
    
    // Private fields ----------------------------------------------------------
    
    /** Maps ant property name to its info. */
    private final Map<String, PropertyInfo> properties;
    
    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper refHelper;
    private final AntBasedProjectType abpt;
    private final UpdateHelper updateHelper;
    private final EarProject earProject;
    private final GeneratedFilesHelper genFilesHelper;
    
    /** Utility field used by bound properties. */
    private final PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);

    public EarProjectProperties(final EarProject project,
            final ReferenceHelper refHelper, final AntBasedProjectType abpt) {
        this.earProject = project;
        this.properties = new HashMap<String, PropertyInfo>();
        this.updateHelper = project.getUpdateHelper();
        this.antProjectHelper = updateHelper.getAntProjectHelper();
        this.refHelper = refHelper;
        this.abpt = abpt;
        this.genFilesHelper = project.getGeneratedFilesHelper();
        read();
    }
    
    /** <strong>Package private for unit test only</strong>. */
    ReferenceHelper getReferenceHelper() {
        return refHelper;
    }
    
    /** <strong>Package private for unit test only</strong>. */
    void updateContentDependency(Set<VisualClassPathItem> oldContent, Set<VisualClassPathItem> newContent) {
        Application app = earProject.getAppModule().getApplication();
        
        Set<VisualClassPathItem> deleted = new HashSet<VisualClassPathItem>(oldContent);
        deleted.removeAll(newContent);
        Set<VisualClassPathItem> added = new HashSet<VisualClassPathItem>(newContent);
        added.removeAll(oldContent);
        
        //do not update the file if there is no change
        boolean same = true;
        if(deleted.size() == added.size()) {
            Iterator<VisualClassPathItem> deletedIterator = deleted.iterator();
            Iterator<VisualClassPathItem> addedIterator = added.iterator();
            while (deletedIterator.hasNext() && addedIterator.hasNext()) {
                VisualClassPathItem del = deletedIterator.next();
                VisualClassPathItem add = addedIterator.next();
                //I suppose the vcpi-s should not be null?!?
                if(del != null && add != null && !del.equals(add)) {
                    same = false;
                    break;
                }
            }
        } else {
            same = false;
        }
        
        boolean saveNeeded = false;
        if (!same) {
            // delete the old entries out of the application
            for (VisualClassPathItem vcpi : deleted) {
                removeItemFromAppDD(app,vcpi);
            }
            // add the new stuff "back"
            for (VisualClassPathItem vcpi : added) {
                addItemToAppDD(app,vcpi);
            }
            saveNeeded = true;
        }
        for (VisualClassPathItem vcpi : newContent) { // #76008
            if (vcpi.getPathInEAR() != null // #103898
                    && !vcpi.getPathInEAR().equals(vcpi.getOrigPathInEAR())) {
                removeItemFromAppDD(app, vcpi, vcpi.getCompletePathInArchive(true));
                addItemToAppDD(app, vcpi);
                saveNeeded = true;
            }
        }
        if (saveNeeded && EarProjectUtil.isDDWritable(earProject)) {
            try {
                app.write(earProject.getAppModule().getDeploymentDescriptor());
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            }
        }
    }
    
    private void removeItemFromAppDD(Application dd, VisualClassPathItem vcpi) {
        removeItemFromAppDD(dd, vcpi, vcpi.getCompletePathInArchive());
    }
    
    private void removeItemFromAppDD(final Application dd,
            final VisualClassPathItem vcpi, final String pathInEAR) {
        Module m = searchForModule(dd, pathInEAR);
        if (null != m) {
            dd.removeModule(m);
            setClientModuleUri("");
            Object obj = vcpi.getObject();
            if (obj instanceof AntArtifact) {
                AntArtifact aa = (AntArtifact) obj;
                Project p = aa.getProject();
                J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        earProject.getAppModule().removeModuleProvider(jmp, pathInEAR);
                    }
                }
            }
        }
    }
    
    private Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods) {
            len = mods.length;
        }
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getConnector();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getJava();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w) {
                val = w.getWebUri();
            }
            if (null != val && val.equals(path)) {
                return mods[i];
            }
        }
        return null;
    }
    
    public void addItemToAppDD(Application dd, VisualClassPathItem vcpi) {
        Object obj = vcpi.getObject();
        String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
        Module mod = null;
        if (obj instanceof AntArtifact) {
            mod = getModFromAntArtifact((AntArtifact) obj, dd, path);
        } else if (obj instanceof File) {
           mod = getModFromFile((File) obj, dd, path);
        }
        if (mod != null && mod.getWeb() != null) {
            replaceEmptyClientModuleUri(path);
        }
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod) {
            dd.addModule(mod);
        }
    }
    
    
    private Module getModFromAntArtifact(AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                String serverInstanceId = earProject.getServerInstanceID();
                if (serverInstanceId != null) {
                    jmp.setServerInstanceID(serverInstanceId);
                }
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    earProject.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean(Application.MODULE);
                if (jm.getModuleType() == J2eeModule.EJB) {
                    mod.setEjb(path); // NOI18N
                } else if (jm.getModuleType() == J2eeModule.WAR) {
                    Web w = mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    FileObject tmp = aa.getScriptFile();
                    if (null != tmp) {
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    }
                    WebModule wm = null;
                    if (null != tmp) {
                        wm = WebModule.getWebModule(tmp);
                    }
                    String contextPath = null;
                    if (null != wm) {
                        contextPath = wm.getContextPath();
                    } 
                    if (contextPath == null) {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        contextPath = path.substring(0,endex);
                    }
                    w.setContextRoot(contextPath);
                    mod.setWeb(w);
                } else if (jm.getModuleType() == J2eeModule.CONN) {
                    mod.setConnector(path);
                } else if (jm.getModuleType() == J2eeModule.CLIENT) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            Exceptions.printStackTrace(cnfe);
        }
        return mod;
    }
    
    private void setClientModuleUri(String newVal) {
        put(EarProjectProperties.CLIENT_MODULE_URI,newVal);
    }
    
    private void replaceEmptyClientModuleUri(String path) {
        // set the context path if it is not set...
        Object current = get(EarProjectProperties.CLIENT_MODULE_URI);
        if (null == current) {
            setClientModuleUri(path);
        }
        if (current instanceof String && ((String) current).length() < 1) {
            setClientModuleUri(path);
        }
    }
    
    private Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile(f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    Web w = mod.newWeb(); 
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
            } catch (ClassNotFoundException cnfe) {
                Logger.getLogger("global").log(Level.INFO, cnfe.getLocalizedMessage());
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            } finally {
                try {
                    if (null != jar) {
                        jar.close();
                    }
                } catch (IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }
    
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
    public void addPropertyChangeListener(PropertyChangeListener l) {

        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
    @SuppressWarnings("unchecked")
    public List<VisualClassPathItem> getJarContentAdditional() {
        List<VisualClassPathItem> vcpis = Collections.emptyList();
        Object o = properties.get(JAR_CONTENT_ADDITIONAL);
        if (o instanceof PropertyInfo) {
            PropertyInfo pi = (PropertyInfo) o;
            Object value = pi.getValue();
            assert value instanceof List : JAR_CONTENT_ADDITIONAL + " is not a List: " + value.getClass();
            vcpis = (List) value;
        }
        return vcpis;
    }
    
    /**
     * Acquires modules form the earproject's metadata (properties files).
     */
    public Map<String, J2eeModuleProvider> getModuleMap() {
        Map<String, J2eeModuleProvider> mods = new HashMap<String, J2eeModuleProvider>();
        for (VisualClassPathItem vcpi : getJarContentAdditional()) {
            Object obj = vcpi.getObject();
            Project p;
            if (obj instanceof AntArtifact) {
                AntArtifact aa = (AntArtifact) obj;
                p = aa.getProject();
            } else {
                continue;
            }
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
                    mods.put(path, jmp);
                }
            }
        }
        return mods; // earProject.getAppModule().setModules(mods);
    }


    public void addJ2eeSubprojects(Project[] moduleProjects) {
        List<AntArtifact> artifactList = new ArrayList<AntArtifact>();
        for (int i = 0; i < moduleProjects.length; i++) {
            AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                    moduleProjects[i],
                    EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE); //the artifact type is the some for both ejb and war projects
            if (null != artifacts) {
                artifactList.addAll(Arrays.asList(artifacts));
            }
        }
        // create the vcpis
        List<VisualClassPathItem> newVCPIs = new ArrayList<VisualClassPathItem>();
        BrokenProjectSupport bps = earProject.getLookup().lookup(BrokenProjectSupport.class);
        for (AntArtifact art : artifactList) {
            VisualClassPathItem vcpi = VisualClassPathItem.createArtifact(art);
            vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
            newVCPIs.add(vcpi);
            bps.watchAntArtifact(art);
        }
        List<VisualClassPathItem> vcpis = getJarContentAdditional();
        newVCPIs.addAll(vcpis);
        put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newVCPIs);
        store();
    }

    /**
     * @see #getApplicationSubprojects(Object)
     */
    List<Project> getApplicationSubprojects() {
        return getApplicationSubprojects(null);
    }

    /**
     * Acquires modules (in the form of projects) from "JAVA EE Modules" not from the deployment descriptor (application.xml).
     * <p>
     * The reason is that for JAVA EE 5 the deployment descriptor is not compulsory.
     * @param moduleType the type of module, see {@link J2eeModule J2eeModule constants}. 
     *                   If it is <code>null</code> then all modules are returned.
     * @return list of EAR project subprojects.
     */
    List<Project> getApplicationSubprojects(Object moduleType) {
        List<VisualClassPathItem> vcpis = earProject.getProjectProperties().getJarContentAdditional();
        List<Project> projects = new ArrayList<Project>(vcpis.size());
        for (VisualClassPathItem vcpi : vcpis) {
            Object obj = vcpi.getObject();
            if (!(obj instanceof AntArtifact)) {
                continue;
            }
            Project vcpiProject = ((AntArtifact) obj).getProject();
            J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (jmp == null) {
                continue;
            }
            if (moduleType == null) {
                projects.add(vcpiProject);
            } else if (moduleType.equals(jmp.getJ2eeModule().getModuleType())) {
                projects.add(vcpiProject);
            }
        }
        return projects;
    }
    
    public String[] getWebUris() {
        Set<String> result = new TreeSet<String>();
        for (Project p : getApplicationSubprojects(J2eeModule.WAR)) {
            result.add(ProjectUtils.getInformation(p).getDisplayName());
        }
        return result.toArray(new String[result.size()]);
    }
    
    public String[] getAppClientUris() {
        Set<String> result = new TreeSet<String>();
        for (Project p : getApplicationSubprojects(J2eeModule.CLIENT)) {
            result.add(ProjectUtils.getInformation(p).getDisplayName());
        }
        return result.toArray(new String[result.size()]);
    }
    
    boolean isWebUri(String uri) {
        return EarProjectUtil.hasLength(uri) && Arrays.binarySearch(getWebUris(), uri) >= 0;
    }
    
    boolean isAppClientUri(String uri) {
        return EarProjectUtil.hasLength(uri) && Arrays.binarySearch(getAppClientUris(), uri) >= 0;
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
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        PropertyInfo pi = properties.get( propertyName );
        pi.setValue( value );
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }
    
    public Object get(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        PropertyInfo pi = properties.get(propertyName);
        return pi == null ? null : pi.getValue();
    }
    
    public boolean isModified( String propertyName ) {
        PropertyInfo pi = properties.get( propertyName );
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        return pi.isModified();
    }
    
    public List getSortedSubprojectsList() {
        List<Project> subprojects = new ArrayList<Project>();
        addSubprojects( earProject, subprojects ); // Find the projects recursively
        String[] displayNames = new String[subprojects.size()];
         
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            displayNames[i] = ProjectUtils.getInformation(subprojects.get(i)).getDisplayName();
        }

        Arrays.sort(displayNames, Collator.getInstance());
        return Arrays.asList(displayNames);
    }
    
    public EarProject getProject() {
        return earProject;
    }
    
    /** Gets all subprojects recursively
     */
    private void addSubprojects( Project project, List<Project> result ) {
        SubprojectProvider spp = project.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator/*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project sp = (Project) it.next();
            if (ProjectUtils.hasSubprojectCycles(project, sp)) {
                Logger.getLogger("global").log(Level.WARNING, "There would be cyclic " + // NOI18N
                        "dependencies if the " + sp + " would be added. Skipping..."); // NOI18N
                continue;
            }
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
        Map<String, EditableProperties> eProps = new HashMap<String, EditableProperties>(2);
        eProps.put( PROJECT, updateHelper.getProperties( PROJECT ) ); 
        eProps.put( PRIVATE, updateHelper.getProperties( PRIVATE ) );
   
        // Initialize the property map with objects
        for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
            final String propertyName = pd.name;
            if ( pd.dest == null ) {
                // Specially handled properties
                if ( WEB_PROJECT_NAME.equals( propertyName ) ) {
                    String projectName = ProjectUtils.getInformation(earProject).getDisplayName();
                    PropertyInfo pi = properties.get(propertyName);
                    if (null == pi) {
                        properties.put(propertyName, new PropertyInfo(pd, projectName, projectName));
                    } else {
                        pi.update(pd, projectName, projectName);
                    }
                }
            } else {
                // Standard properties
                String raw = eProps.get( pd.dest ).getProperty( propertyName );
                String eval = antProjectHelper.getStandardPropertyEvaluator ().getProperty ( propertyName );
                PropertyInfo pi = properties.get(propertyName);
                if (null == pi) {
                    PropertyInfo propertyInfo = new PropertyInfo(pd, raw, eval);
                    properties.put(propertyName, propertyInfo);
                } else {
                    pi.update(pd, raw, eval);
                }
            }
        }
    }

    void initProperty(final String propertyName, final PropertyInfo propertyInfo) {
        properties.put(propertyName, propertyInfo);
    }
    
    /**
     * Transforms all the Objects from GUI controls into String Ant properties
     * and stores them in the project.
     */
    public void store() {
        try {
            // Store properties
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    URL buildImplXSL = EarProject.class.getResource("resources/build-impl.xsl");
                    int state = genFilesHelper.getBuildScriptState(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH, buildImplXSL);
                    if ((state & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) {
                        if (showModifiedMessage(NbBundle.getMessage(EarProjectProperties.class,"TXT_ModifiedTitle"))) {
                            //Delete user modified build-impl.xml
                            FileObject fo = updateHelper.getAntProjectHelper().getProjectDirectory().
                                    getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                            if (fo != null) {
                                fo.delete();
                                genFilesHelper.refreshBuildScript(
                                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                        buildImplXSL,
                                        false);
                            }
                        } else {
                            return false;
                        }
                    }
                    storeProperties();
                    return true;
                }
            });
            // and save the project
            if (result) {
                ProjectManager.getDefault().saveProject(earProject);
            }
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void storeProperties() {
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
            if (pd.dest == null && newValueEncoded != null && WEB_PROJECT_NAME.equals(pd.name)) {
                assert false : "No support yet for changing name of J2SEProject; cf. J2SEProject.setName";  //NOI18N
            }
            
            if (JAVA_PLATFORM.equals(pd.name) && newValueEncoded != null) {
                defaultPlatform =
                        Boolean.valueOf(pi.getNewValueEncoded().equals(JavaPlatformManager.getDefault()
                        .getDefaultPlatform()
                        .getProperties()
                        .get("platform.ant.name"))); // NOI18N
                setPlatform(defaultPlatform.booleanValue(), pi.getNewValueEncoded());
            }
        }
        
        // Reread the properties. It may have changed when
        // e.g. when setting references to another projects
        Map<String, EditableProperties> eProps = new HashMap<String, EditableProperties>(2);
        eProps.put(PROJECT, updateHelper.getProperties(PROJECT));
        eProps.put(PRIVATE, updateHelper.getProperties(PRIVATE));
        
        //generate library content references into private.properties
        for (Iterator it = properties.values().iterator(); it.hasNext();) {
            PropertyInfo pi = (PropertyInfo) it.next();
            PropertyDescriptor pd = pi.getPropertyDescriptor();
            if(JAR_CONTENT_ADDITIONAL.equals(pd.name)) {
                //FIX of #58079 - newValue is null when the store() is called from resolve references dialog
                Object piValue = (pi.newValue != null ? pi.newValue : pd.parser.decode(pi.evaluatedValue, antProjectHelper, refHelper ));
                if(piValue != null) {
                    //add an entry into private properties
                    @SuppressWarnings("unchecked")
                    Iterator<VisualClassPathItem> newItems  = ((List)piValue).iterator();
                    @SuppressWarnings("unchecked")
                    Iterator<VisualClassPathItem> oldItems = ((List)pi.value).iterator();
                    storeLibrariesLocations(newItems, oldItems, eProps.get(PRIVATE));
                    break;
                }
            }
        }
        
        // Set the changed properties
        for (Iterator it = properties.values().iterator(); it.hasNext();) {
            PropertyInfo pi = (PropertyInfo) it.next();
            PropertyDescriptor pd = pi.getPropertyDescriptor();
            String newValueEncoded = pi.getNewValueEncoded();

            if (CLIENT_MODULE_URI.equals(pd.name) && pd.dest != null) {
                // #109006 - special case when adding CAR, newValueEncoded is null
                updateClientModuleUri(eProps.get(pd.dest), newValueEncoded);
            } else if (newValueEncoded != null && pd.dest != null) {
                // Standard properties
                EditableProperties ep = eProps.get(pd.dest);
                //                  if (PATH_PARSER.equals(pd.parser)) {
                if (pd.parser instanceof PathParser) {
                    // XXX: perhaps PATH_PARSER could return List of paths so that
                    // tokenizing could be omitted here:
                    String[] items = PropertyUtils.tokenizePath(newValueEncoded);

                    for (int i = 0; i < items.length - 1; i++) {
                        items[i] += File.pathSeparatorChar;
                    }
                    ep.setProperty(pd.name, items);
                } else if (NO_DEPENDENCIES.equals(pd.name) && newValueEncoded.equals("false")) { // NOI18N
                    ep.remove(pd.name);
                } else {
                    if (JAVA_PLATFORM.equals(pd.name)) { // update javac.source and javac.target
                        assert defaultPlatform != null;
                        updateSourceLevel(defaultPlatform.booleanValue(), newValueEncoded, ep);
                    } else if (JAVAC_CLASSPATH.equals(pd.name)) {
                        writeWebLibraries(refHelper, (List) pi.getValue(),
                                          TAG_WEB_MODULE_LIBRARIES);
                    } else if (JAR_CONTENT_ADDITIONAL.equals(pd.name)) {
                        writeWebLibraries(refHelper, (List) pi.getValue(),
                                          TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                    } else if (J2EE_SERVER_INSTANCE.equals(pd.name)) {
                        String serverInstanceID = (String) pi.getValue();
                        
                        // ant deployment support
                        File projectFolder = FileUtil.toFile(earProject.getProjectDirectory());
                        try {
                            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ANT_DEPLOY_BUILD_SCRIPT), J2eeModule.EAR, serverInstanceID);
                        } catch (IOException ioe) {
                            Logger.getLogger("global").log(Level.INFO, null, ioe);
                        }
                        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
                        if (deployAntPropsFile == null) {
                            ep.remove(DEPLOY_ANT_PROPS_FILE);
                        } else {
                            ep.setProperty(DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
                        }
                        
                        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
                        EarProjectProperties.setACProperties(j2eePlatform, eProps.get(PROJECT));
                        EarProjectProperties.setACPrivateProperties(j2eePlatform, serverInstanceID, eProps.get(PRIVATE));
                    }
                    ep.setProperty(pd.name, newValueEncoded);
                }
            }
        }
        
        // Store the property changes into the project
        updateHelper.putProperties(PROJECT, eProps.get(PROJECT));
        updateHelper.putProperties(PRIVATE, eProps.get(PRIVATE));
    }
    
    private void updateClientModuleUri(EditableProperties ep, String newValue) {
        
        if (isWebUri(newValue)) {
            ep.put(CLIENT_MODULE_URI, newValue);
            ep.remove(APPLICATION_CLIENT);
            return;
        }
        if (isAppClientUri(newValue)) {
            ep.put(APPLICATION_CLIENT, newValue);
            ep.put(CLIENT_MODULE_URI, getClientModuleUriForAppClient());
            return;
        }
        
        // 2 possibilities here:
        //  a) newValue is empty (removing module via context menu)
        //  b) newValue is not in correct form (adding module via context menu)
        
        // check current module uri
        String clientModuleUri = ep.getProperty(CLIENT_MODULE_URI);
        if (EarProjectUtil.hasLength(clientModuleUri)) {
            // uri exists -> is still valid?
            if (isWebUri(clientModuleUri)) {
                // web module is valid => keep it
                return;
            } else if (getClientModuleUriForAppClient().equals(clientModuleUri)) {
                // could be app client
                String appClient = ep.get(APPLICATION_CLIENT);
                if (isAppClientUri(appClient)) {
                    // app client module is valid => keep it
                    return;
                }
            }
        }
        
        // uri doesn't exist or is not valid
        //  => so remove it and try to set any valid module (because there's no '<none>' option for client module in customizer)
        ep.remove(APPLICATION_CLIENT);
        ep.remove(CLIENT_MODULE_URI);
        
        String[] webUris = getWebUris();
        for (String webUri : webUris) {
            ep.put(CLIENT_MODULE_URI, webUri);
            ep.remove(APPLICATION_CLIENT);
            return;
        }
        String[] appClientUris = getAppClientUris();
        for (String appClientUri : appClientUris) {
            ep.put(APPLICATION_CLIENT, appClientUri);
            ep.put(CLIENT_MODULE_URI, getClientModuleUriForAppClient());
            return;
        }
    }
    
    String getClientModuleUriForAppClient() {
        PropertyInfo earNamePI = properties.get(JAR_NAME);
        assert earNamePI != null;
        String earName = (String) earNamePI.getValue();
        assert earName != null;
        if (earName.endsWith(".ear")) { // NOI18N
            earName = earName.substring(0, earName.length() - 4);
        }
        return earName + "/${" + APPLICATION_CLIENT + '}'; // NOI18N
    }

    public static void setACProperties(final J2eePlatform j2eePlatform, final EditableProperties ep) {
        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            ep.put(APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
            ep.remove(CLIENT_NAME);
        } else if ((mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, CLIENT_NAME)) != null) {
            ep.put(CLIENT_NAME, mainClassArgs);
            ep.remove(APPCLIENT_MAINCLASS_ARGS);
        } else {
            ep.remove(APPCLIENT_MAINCLASS_ARGS);
            ep.remove(CLIENT_NAME);
        }
    }
    
    private void setAndSaveACPrivateProperties(final String servInstIDs, final J2eePlatform platform) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EditableProperties priv = updateHelper.getProperties(PRIVATE);
                    setACPrivateProperties(platform, servInstIDs, priv);
                    updateHelper.putProperties(PRIVATE, priv);
                    ProjectManager.getDefault().saveProject(earProject);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    public static void setACPrivateProperties(final J2eePlatform j2eePlatform,
            final String serverInstanceID, final EditableProperties ep) {
        // XXX rather hotfix for #75518. Get rid of it with fixing or #75574
        if (!j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.CLIENT)) {
            return;
        }
        File[] accrt = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
        ep.setProperty(APPCLIENT_TOOL_RUNTIME, EarProjectGenerator.toClasspathString(accrt));
        
        String mainClass = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (mainClass != null) {
            ep.setProperty(APPCLIENT_TOOL_MAINCLASS, mainClass);
        }
        
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            ep.setProperty(APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            ep.setProperty(APPCLIENT_TOOL_ARGS, args);
        }    
        
        // set j2ee.platform.classpath
        String classpath = EarProjectGenerator.toClasspathString(j2eePlatform.getClasspathEntries());
        ep.setProperty("j2ee.platform.classpath", classpath); // NOI18N
        
        //WORKAROUND for --retrieve option in asadmin deploy command
        //works only for local domains
        //see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        File asRoot = j2eePlatform.getPlatformRoots()[0];
        InstanceProperties ip = InstanceProperties.getInstanceProperties(serverInstanceID);
        //check if we have AS
        if (ip != null && new File(asRoot, "lib/admin-cli.jar").exists()) { // NOI18N
            File exFile = new File(asRoot, "lib/javaee.jar"); // NOI18N
            if (exFile.exists()) {
                ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/generated/xml/j2ee-apps").getAbsolutePath()); // NOI18N
            } else {
                ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/applications/j2ee-apps").getAbsolutePath()); // NOI18N
            }
        } else {
            ep.remove(APPCLIENT_WA_COPY_CLIENT_JAR_FROM);
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
            // The platform does not exist. Perhaps this is project with broken references?
            // Do not update target and source because nothing is known about the platform.
        }
    }
    
    private final SpecificationVersion JDKSpec13 = new SpecificationVersion("1.3"); // NOI18N
    
    private void setPlatform(boolean isDefault, String platformAntID) {
        Element pcd = updateHelper.getPrimaryConfigurationData( true );
        NodeList sps = pcd.getElementsByTagName( "explicit-platform" ); // NOI18N
        if (isDefault && sps.getLength() > 0) {
            pcd.removeChild(sps.item(0));
        } else if (!isDefault) {
            Element el;
            if (sps.getLength() == 0) {
                el = pcd.getOwnerDocument().createElement("explicit-platform"); // NOI18N
                pcd.appendChild(el);
            } else {
                el = (Element)sps.item(0);
            }
            boolean explicitSource = true;
            JavaPlatform platform = findPlatform(platformAntID);
            if ((platform != null && platform.getSpecification().getVersion().compareTo(JDKSpec13) <= 0) || platform == null) {
                explicitSource = false;
            }
            el.setAttribute("explicit-source-supported", explicitSource ? "true" : "false"); // NOI18N
        }
        updateHelper.putPrimaryConfigurationData(pcd, true);
    }
    
    public void ensurePlatformIsSet(final boolean showAlert) throws IOException {
        final String servInstID = (String) get(EarProjectProperties.J2EE_SERVER_INSTANCE);
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
        if (platform == null) {
            // if there is some server instance of the type which was used
            // previously do not ask and use it
            String serverType = (String) get(EarProjectProperties.J2EE_SERVER_TYPE);
            if (serverType != null) {
                String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                if (servInstIDs.length > 0) {
                    EarProjectProperties.setServerInstance(earProject, earProject.getUpdateHelper(), servInstIDs[0]);
                    platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                    if (platform != null) {
                        setAndSaveACPrivateProperties(servInstIDs[0], platform);
                    }
                }
            }
            if (showAlert && platform == null) {
                BrokenServerSupport.showAlert();
            }
        } else {    
            // if the project server instance exists, make sure that the Ant deployment 
            // support is present, #85749
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    EditableProperties privateProps = updateHelper.getProperties(PRIVATE);
                    boolean changed = generateAntDeploymentSupport(privateProps, earProject.getProjectDirectory(), servInstID, false);
                    if (changed) {
                        updateHelper.putProperties(PRIVATE, privateProps);
                        try {
                            ProjectManager.getDefault().saveProject(earProject);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Generates the Ant deployment script and sets the respective properties
     *
     * @param privateProps private properties
     * @param projectDirectory the project directory
     * @param serverInstanceID the server instance ID
     * @param force if true the Ant deployment script is always (re)generated,
     *        otherwise only if it does not exist
     *
     * @return true if the private properties changed and should be saved, false 
     *         otherwise.
     */
    private static boolean generateAntDeploymentSupport(EditableProperties privateProps, 
            FileObject projectDirectory, String serverInstanceID, boolean force) {
        
        // check the Ant deployment script
        try {
            File projectDir = FileUtil.toFile(projectDirectory);
            File antDeploymentScript = new File(projectDir, ANT_DEPLOY_BUILD_SCRIPT);
            if (force || !antDeploymentScript.exists()) {
                AntDeploymentHelper.writeDeploymentScript(antDeploymentScript, J2eeModule.EAR, serverInstanceID);
            }
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        
        // check the Ant deployment properties
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        
        String newDeployAntPropsFilePath = (deployAntPropsFile == null) ? null : deployAntPropsFile.getAbsolutePath();
        String oldDeployAntPropsFilePath = privateProps.getProperty(DEPLOY_ANT_PROPS_FILE);
        
        if (oldDeployAntPropsFilePath != null && newDeployAntPropsFilePath == null) {
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
        } else if (newDeployAntPropsFilePath != null && !newDeployAntPropsFilePath.equals(oldDeployAntPropsFilePath)) {
            privateProps.setProperty(DEPLOY_ANT_PROPS_FILE, newDeployAntPropsFilePath);
        } else {
            // private properties did not change
            return false;
        }
        // private properties changed
        return true;
    }
    
    public static void setServerInstance(final Project project, final UpdateHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(PROJECT);
                    EditableProperties privateProps = helper.getProperties(PRIVATE);
                    
                    // update j2ee.server.type & j2ee.server.instance
                    projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(serverInstanceID));
                    privateProps.setProperty(J2EE_SERVER_INSTANCE, serverInstanceID);

                    // ant deployment support
                    generateAntDeploymentSupport(privateProps, project.getProjectDirectory(), serverInstanceID, true);

                    helper.putProperties(PROJECT, projectProps);
                    helper.putProperties(PRIVATE, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    /** Finds out what are new and removed project dependencies and
     * applies the info to the project
     */
    private void resolveProjectDependencies() {
    
        String allPaths[] = { JAVAC_CLASSPATH, JAR_CONTENT_ADDITIONAL, RUN_CLASSPATH };
        
        // Create a set of old and new artifacts.
        Set<VisualClassPathItem> oldArtifacts = new HashSet<VisualClassPathItem>();
        Set<VisualClassPathItem> newArtifacts = new HashSet<VisualClassPathItem>();
        for ( int i = 0; i < allPaths.length; i++ ) {            
            PropertyInfo pi = properties.get( allPaths[i] );

            // Get original artifacts
            @SuppressWarnings("unchecked")
            List<VisualClassPathItem> oldList = (List)pi.getOldValue();
            if ( oldList != null ) {
                oldArtifacts.addAll(oldList);
            }
            
            // Get artifacts after the edit
            @SuppressWarnings("unchecked")
            List<VisualClassPathItem> newList = (List) pi.getValue();
            if ( newList != null ) {
                newArtifacts.addAll(newList);
            }
                        
        }

        // Create set of removed artifacts and remove them
        Set<VisualClassPathItem> removed = new HashSet<VisualClassPathItem>(oldArtifacts);
        removed.removeAll( newArtifacts );
        Set<VisualClassPathItem> added = new HashSet<VisualClassPathItem>(newArtifacts);
        added.removeAll(oldArtifacts);
        
        @SuppressWarnings("unchecked")
        Set<VisualClassPathItem> oldContent = new HashSet<VisualClassPathItem>(
                (List) properties.get(JAR_CONTENT_ADDITIONAL).getOldValue());
        @SuppressWarnings("unchecked")
        Set<VisualClassPathItem> newContent = new HashSet<VisualClassPathItem>(
                (List) properties.get(JAR_CONTENT_ADDITIONAL).getValue());
        
        updateContentDependency(oldContent, newContent);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for (VisualClassPathItem vcpi : removed) {
            if ( vcpi.getType() == VisualClassPathItem.Type.ARTIFACT ||
                    vcpi.getType() == VisualClassPathItem.Type.JAR ) {
                boolean used = false; // now check if the file reference isn't used anymore
                for (int i=0; i < allPaths.length; i++) {
                    PropertyInfo pi = properties.get( allPaths[i] );
                    @SuppressWarnings("unchecked")
                    List<VisualClassPathItem> values = (List) pi.getValue();
                    if (values == null) {
                        break;
                    }
                    for (VisualClassPathItem valcpi : values) {
                        if (valcpi.getRaw() != null
                                && valcpi.getRaw().indexOf(vcpi.getRaw()) > -1) {
                            used = true;
                            break;
                        }
                    }
                }
                if (!used) {
                    refHelper.destroyReference(vcpi.getRaw());
                }
                
            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( PROJECT );
        boolean changed = false;

        for (VisualClassPathItem vcpi : removed) {
            if (vcpi.getType() == VisualClassPathItem.Type.LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = vcpi.getRaw();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        File projDir = FileUtil.toFile(antProjectHelper.getProjectDirectory());
        for (VisualClassPathItem vcpi : added) {
            if (vcpi.getType() == VisualClassPathItem.Type.LIBRARY) {
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
            File f = antProjectHelper.resolveFile(paths[i]);
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
    
    private class PropertyInfo {
        
        private PropertyDescriptor propertyDesciptor;
        private Object value;
        private String evaluatedValue;
        private Object newValue;
        private String newValueEncoded;
        
        public PropertyInfo( PropertyDescriptor propertyDescriptor, String rawValue, String evaluatedValue) {
            update(propertyDescriptor, rawValue, evaluatedValue);
        }
        
        final void update(PropertyDescriptor propertyDescriptor, String rawValue, String evaluatedValue) {
            this.propertyDesciptor = propertyDescriptor;
            this.evaluatedValue = evaluatedValue;
            this.value = propertyDesciptor.parser.decode( rawValue, antProjectHelper, refHelper );
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {            
            if ( isModified() ) {
                newValueEncoded = propertyDesciptor.parser.encode( newValue, antProjectHelper, refHelper);                
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
            void save(PropertyInfo propertyInfo);
        }
        
        final PropertyParser parser;
        final String name;
        final String dest;
        final Saver saver;

        /**
         * @param name name of the property
         * @param dest either {@link AntProjectHelper#PROJECT_PROPERTIES_PATH}
         *             or {@link AntProjectHelper#PRIVATE_PROPERTIES_PATH}.
         */
        PropertyDescriptor(String name, String dest, PropertyParser parser, Saver saver) {
            this.name = name;
            this.dest = dest;
            this.saver = saver;
            this.parser = parser;
        }
        
        /** 
         * Delegates to {@link PropertyDescriptor(String, String, PropertyParser, Saver)}
         * with <code>null</code> for <code>saver parameter</code>.
         */
        PropertyDescriptor( String name, String dest, PropertyParser parser ) {
            this(name, dest, parser, null);
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
                
                if ( lowecaseRaw.equals( "true") || // NOI18N
                        lowecaseRaw.equals( "yes") || // NOI18N
                        lowecaseRaw.equals( "enabled") ) { // NOI18N
                    return Boolean.TRUE;
                }
            }
            
            return Boolean.FALSE;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return ((Boolean)value).booleanValue() ? "true" : "false"; // NOI18N
        }
        
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        
        @Override
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {                    
            return ((Boolean)super.decode( raw, antProjectHelper, refHelper )).booleanValue() ? Boolean.FALSE : Boolean.TRUE;           
        }
        
        @Override
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper, refHelper );
        }
        
    }
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
        
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        { JAVAC_CLASSPATH, NbBundle.getMessage( EarProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) } //NOI18N
    };
    
    private class PathParser extends PropertyParser {
        private final String webLibraryElementName;
        private static final String TAG_PATH_IN_EAR = "path-in-war"; //NOI18N
        private static final String TAG_FILE = "file"; //NOI18N
        private static final String TAG_LIBRARY = "library"; //NOI18N

        public PathParser() {
            this(null);
        }

        public PathParser(String webLibraryElementName) {
            this.webLibraryElementName = webLibraryElementName;
        }

        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            Map<String, String> earIncludesMap = createEarIncludesMap();
            if (raw != null) {
                String pe[] = PropertyUtils.tokenizePath( raw );
                for( int i = 0; i < pe.length; i++ ) {
                    final String pathItem = pe[i];
                    if (!earIncludesMap.containsKey(pathItem)) {
                        earIncludesMap.put(pathItem, VisualClassPathItem.PATH_IN_EAR); // NONE);
                    }
                }
            }
            List<VisualClassPathItem> cpItems = new ArrayList<VisualClassPathItem>(earIncludesMap.size() );
            for (Map.Entry<String, String> entry : earIncludesMap.entrySet()) {
                cpItems.add(createVisualClassPathItem(antProjectHelper, refHelper,
                        entry.getKey(), entry.getValue()));
            }
            return cpItems;
        }

        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            Element data = null;
            Element webModuleLibs = null;
            Document doc = null;
            if(webLibraryElementName != null) {
                final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
                data = updateHelper.getPrimaryConfigurationData(true);
                doc = data.getOwnerDocument();
                webModuleLibs = (Element) data.getElementsByTagNameNS(ns,
                                    webLibraryElementName).item(0);
                //prevent NPE thrown from older projects
                if (webModuleLibs == null) {
                    webModuleLibs = doc.createElementNS(ns, webLibraryElementName); //NOI18N
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
                sb.append(pathItem);
                if ( it.hasNext() ) {
                    sb.append( File.pathSeparatorChar );
                }
            }
            if(webLibraryElementName != null) {
                updateHelper.putPrimaryConfigurationData(data, true);
            }
            return sb.toString();
        }

        private Element createLibraryElement(Document doc, String pathItem,
                VisualClassPathItem visualClassPathItem) {
            final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
            Element libraryElement = doc.createElementNS(ns,
                    TAG_LIBRARY);
            
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            getFilesForItem(visualClassPathItem, files, dirs);
            if (files.size() > 0) {
                libraryElement.setAttribute(ATTR_FILES, "" + files.size());
            }
            if (dirs.size() > 0) {
                libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size());
            }           
            
            Element webFile = doc.createElementNS(ns, TAG_FILE);
            libraryElement.appendChild(webFile);
            webFile.appendChild(doc.createTextNode(pathItem));
            if (visualClassPathItem.getPathInEAR() != VisualClassPathItem.PATH_IN_EAR_NONE) {
                Element pathInEar = doc.createElementNS(ns,
                        TAG_PATH_IN_EAR);
                pathInEar.appendChild(doc.createTextNode(visualClassPathItem.getPathInEAR()));
                libraryElement.appendChild(pathInEar);
            }
            return libraryElement;
        }

        private Map<String, String> createEarIncludesMap() {
            Map<String, String> earIncludesMap = new LinkedHashMap<String, String>();
            if (webLibraryElementName != null) {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
//                final String ns = WebProjectType.PROJECT_CONFIGURATION_NAMESPACE;
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webLibraryElementName).item(0);
                if(webModuleLibs != null) {
                    NodeList ch = webModuleLibs.getChildNodes();
                    for (int i = 0; i < ch.getLength(); i++) {
                        if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) ch.item(i);
                            Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                            NodeList pathInEarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_EAR);
                            earIncludesMap.put(findText(webFile), pathInEarElements.getLength() > 0 ?
                                findText(pathInEarElements.item(0)) : VisualClassPathItem.PATH_IN_EAR_NONE);
                        }
                    }
                }
            }
            return earIncludesMap;
        }

        private VisualClassPathItem createVisualClassPathItem(AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper, String raw, String pathInEAR) {
            // First try to find out whether the item is well known classpath
            // in the J2SE project type
            for (int j = 0; j < WELL_KNOWN_PATHS.length; j++) {
                final String[] wellKnownPath = WELL_KNOWN_PATHS[j];
                if (wellKnownPath[0].equals(getAntPropertyName(raw))) {
                    return new VisualClassPathItem(raw, VisualClassPathItem.Type.CLASSPATH, raw,
                            wellKnownPath[1], pathInEAR);
                }
            }
            if (raw.startsWith(LIBRARY_PREFIX)) {
                // Library from library manager
                // String eval = antProjectHelper.evaluate(getAntPropertyName(pathItem));
                String eval = raw.substring(LIBRARY_PREFIX.length(), raw.lastIndexOf('.')); //NOI18N
                Library lib = LibraryManager.getDefault().getLibrary(eval);
                if (lib != null) {
                    return new VisualClassPathItem(lib, VisualClassPathItem.Type.LIBRARY, raw, eval, pathInEAR);
                } else {
                    return new VisualClassPathItem(null, VisualClassPathItem.Type.LIBRARY, raw, null, pathInEAR);
                }
            } else if (raw.startsWith(ANT_ARTIFACT_PREFIX)) {
                AntArtifact artifact = (AntArtifact) refHelper.findArtifactAndLocation(raw)[0];
                if (artifact != null) {
                    // Sub project artifact
                    return VisualClassPathItem.createArtifact(artifact, raw, pathInEAR);
                } else {
                    return VisualClassPathItem.createArtifact(null, raw, null, pathInEAR);
                }
            } else {
                // Standalone jar or property
                String eval;
                if (isAntProperty(raw)) {
                    eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(getAntPropertyName(raw));
                } else {
                    eval = raw;
                }
                File f = (eval == null) ? null : antProjectHelper.resolveFile(eval);
                return VisualClassPathItem.createJAR(f, raw, pathInEAR, eval);
            }
        }

        private String getPathItem(VisualClassPathItem vcpi, ReferenceHelper refHelper) {
            switch (vcpi.getType()) {
                case JAR:
                    String pathItem = vcpi.getRaw();
                    if (pathItem == null) {
                        // New file
                        return refHelper.createForeignFileReference((File) vcpi.getObject(),
                                JavaProjectConstants.ARTIFACT_TYPE_JAR);
                    } else {
                        return pathItem;
                    }
                case ARTIFACT:
                    if (vcpi.getObject() != null) {
                        AntArtifact aa = (AntArtifact) vcpi.getObject();
                        return refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                    } else {
                        return vcpi.getRaw();
                    }
                case LIBRARY:
                case CLASSPATH:
                    return vcpi.getRaw();
                default:
                    assert false : "Unknown VisualClassPathItem type: " + vcpi.getType();
                    return null;
            }
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
            String normalizedName = platforms[i].getProperties().get("platform.ant.name"); // NOI18N
            if (normalizedName != null && normalizedName.equals(platformAntID)) {
                return platforms[i];
            }
        }
        return null;
    }
    
    private static class PlatformParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform platform = findPlatform(raw);
            if (platform != null) {
                return platform.getDisplayName();
            }
            // if platform does not exist then return raw reference.
            return raw;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
                    new Specification ("j2se",null)); // NOI18N
            if (platforms.length == 0) {
                // platform for this project does not exist. broken reference? its displayname should 
                // correspond to platform ID. so just return it:
                return (String)value;
            } else {
                return platforms[0].getProperties().get("platform.ant.name");  //NOI18N
            }
        }
    }

    private void writeWebLibraries(ReferenceHelper refHelper, List value,
            final String elementName) {
        Element data = updateHelper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(abpt.getPrimaryConfigurationDataElementNamespace(true),
                elementName).item(0); //NOI18N

        //prevent NPE thrown from older projects
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), elementName); //NOI18N
            data.appendChild(webModuleLibs);
        }

        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }

        for (Iterator it = value.iterator(); it.hasNext();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) it.next();
            String library_tag_value = "";
            
            //TODO: prevent NPE from CustomizerCompile - need to investigate
            if (vcpi == null) {
                return;
            }

            switch (vcpi.getType()) {
                case JAR:
                    String raw = vcpi.getRaw();

                    if (raw == null) {
                        // New file
                        File file = (File) vcpi.getObject();
                        String reference = refHelper.createForeignFileReference(file,
                                JavaProjectConstants.ARTIFACT_TYPE_JAR);
                        library_tag_value = reference;
                    } else {
                        // Existing property
                        library_tag_value = raw;
                    }

                    break;
                case LIBRARY:
                    library_tag_value = vcpi.getRaw();
                    break;
                case ARTIFACT:
                    AntArtifact aa = (AntArtifact) vcpi.getObject();
                    String reference = refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                    library_tag_value = reference;
                    break;
                case CLASSPATH:
                    library_tag_value = vcpi.getRaw();
                    break;
                default:
                    assert false : "Unknown VisualClassPathItem type: " + vcpi.getType();
            }

            Element library = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "library"); //NOI18N
            webModuleLibs.appendChild(library);
            Element webFile = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "file"); //NOI18N
            library.appendChild(webFile);
            webFile.appendChild(doc.createTextNode(library_tag_value));
            String piw = vcpi.getPathInEAR();
            if (piw != VisualClassPathItem.PATH_IN_EAR_NONE) {
                Element pathInEar = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "path-in-war"); //NOI18N
                pathInEar.appendChild(doc.createTextNode(vcpi.getPathInEAR()));
                library.appendChild(pathInEar);
            }
        }
        updateHelper.putPrimaryConfigurationData(data, true);
    }
    
    /** Store locations of libraries in the classpath param that have more the one
     * file into the properties in the following format:
     * 
     * <ul>
     * <li>libs.foo.classpath.libdir.1=C:/foo
     * <li>libs.foo.classpath.libdirs=1
     * <li>libs.foo.classpath.libfile.1=C:/bar/a.jar
     * <li>libs.foo.classpath.libfile.2=C:/bar/b.jar
     * <li>libs.foo.classpath.libfiles=2
     * </ul>
     * This is needed for the Ant copy task as it cannot copy more the one file
     * and it needs different handling for files and directories.
     * <br>
     * It removes all properties that match this format that were in the {@link #properties}
     * but are not in the {@link #classpath}.
     */
    public static void storeLibrariesLocations (Iterator<VisualClassPathItem> classpath, 
            Iterator<VisualClassPathItem> oldClasspath, EditableProperties privateProps) {
        List<String> exLibs = new ArrayList<String>();
        Iterator propKeys = privateProps.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = (String) propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            VisualClassPathItem item = classpath.next();
            //do not update anything if the classpath element is null
            //this may happen when the library is broken or removed
            if(item.getObject() == null) {
                continue;
            } 
            
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            getFilesForItem (item, files, dirs);
            String key;
            String ref = item.getRaw();
            if (files.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    key = getAntPropertyName(ref)+".libfile." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
            if (dirs.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                for (int i = 0; i < dirs.size(); i++) {
                    File f = dirs.get(i);
                    key = getAntPropertyName(ref)+".libdir." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
        }
        Iterator unused = exLibs.iterator();
        while (unused.hasNext()) {
            privateProps.remove(unused.next());
        }
    }
    
    public static final void getFilesForItem (VisualClassPathItem item, List<File> files, List<File> dirs) {
        if (item.getType() == VisualClassPathItem.Type.LIBRARY) {
            @SuppressWarnings("unchecked")
            List<URL> roots = ((Library)item.getObject()).getContent("classpath");  //NOI18N
            for (URL rootUrl : roots) {
                FileObject root = URLMapper.findFileObject (rootUrl);
                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                    root = FileUtil.getArchiveFile (root);
                }
                File f = FileUtil.toFile(root);
                if (f != null) {
                    if (f.isFile()) {
                        files.add(f); 
                    } else {
                        dirs.add(f);
                    }
                }
            }
        }
        if (item.getType() == VisualClassPathItem.Type.JAR) {
            File root = (File)item.getObject();
            if (root != null) {
                if (root.isFile()) {
                    files.add(root); 
                } else {
                    dirs.add(root);
                }
            }
        }
        if (item.getType() == VisualClassPathItem.Type.ARTIFACT) {
            AntArtifact artifact = (AntArtifact)item.getObject();
            if (artifact != null) {
                String artifactFolder = artifact.getScriptLocation().getParent();
                URI roots[] = artifact.getArtifactLocations();
                for (int i = 0; i < roots.length; i++) {
                    String root = artifactFolder + File.separator + roots [i];
                    if (root.endsWith(File.separator)) {
                        dirs.add(new File(root));
                    } else {
                        files.add(new File(root));
                    }
                }
            }
        }
    }
    
    private static boolean showModifiedMessage(final String title) {
        String message = NbBundle.getMessage(EarProjectProperties.class,"TXT_Regenerate");
        JButton regenerateButton = new JButton(NbBundle.getMessage(EarProjectProperties.class,"CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EarProjectProperties.class,"AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }
    
}
