/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.text.Document;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.rubyproject.JavaClassPathUi;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender.Item;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ui.StoreGroup;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Petr Hrebejk
 */
public class RailsProjectProperties extends SharedRubyProjectProperties {
    public static final String RAILS_PORT = "rails.port"; // NOI18N
    public static final String RAILS_SERVERTYPE = "rails.servertype"; // NOI18N
    public static final String RAILS_ENV = "rails.env"; // NOI18N
    
    // Special properties of the project
    //public static final String Ruby_PROJECT_NAME = "rails.project.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N

    // Properties stored in the PROJECT.PROPERTIES    
    // TODO - nuke me!
    public static final String MAIN_CLASS = "main.file"; // NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    //public static final String RUN_WORK_DIR = "work.dir"; // NOI18N
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
                    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    
//    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    
    // CustomizerRun
    Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> RUN_CONFIGS;
    String activeConfig;
    private Map<String,String> additionalProperties;

    // CustomizerRunTest

    // Private fields ----------------------------------------------------------------------    
    private RailsProject project;
    private HashMap properties;    
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private GeneratedFilesHelper genFileHelper;
    private ProjectPropertyExtender cs;
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    private RubyPlatform platform;
    private RubyInstance server;
    private String railsEnvironment;
    
    RailsProject getProject() {
        return project;
    }
    
   // Well known paths
    public static final String[] WELL_KNOWN_PATHS = new String[] {
            "${" + JAVAC_CLASSPATH + "}", // NOI18N
    };
    public static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    public static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N
    // XXX looks like there is some kind of API missing in ReferenceHelper?
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    /** Creates a new instance of RubyUIProperties and initializes them */
    public RailsProjectProperties( RailsProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper ) {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
        //this.cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getRakeProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX );
        this.cs = new ProjectPropertyExtender( evaluator, refHelper, updateHelper.getRakeProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX );
                
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new HashMap<String,String>();

        init(); // Load known properties        
    }

    /** Initializes the visual models 
     */
    private void init() {
        CLASS_PATH_LIST_RENDERER = new JavaClassPathUi.ClassPathListCellRenderer( evaluator );

        EditableProperties projectProperties = updateHelper.getProperties( RakeProjectHelper.PROJECT_PROPERTIES_PATH );         
        String cp = projectProperties.get( JAVAC_CLASSPATH )  ;
        JAVAC_CLASSPATH_MODEL = /*ClassPathUiSupport.*/createListModel(cs.itemsIterator(cp) );
        INCLUDE_JAVA_MODEL = projectGroup.createToggleButtonModel( evaluator, INCLUDE_JAVA );
        
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );
        
        // CustomizerRun
        RUN_CONFIGS = readRunConfigs();
        activeConfig = evaluator.getProperty("config");
                
    }
    
    // From ClassPathUiSupport:
    public static DefaultListModel createListModel( Iterator it ) {
        
        DefaultListModel model = new DefaultListModel();
        
        while( it.hasNext() ) {
            model.addElement( it.next() );
        }
        
        return model;
    }
    // From ClassPathUiSupport:
    public static Iterator<Item> getIterator( DefaultListModel model ) {        
        // XXX Better performing impl. would be nice
        return getList( model ).iterator();        
    }
    
    // From ClassPathUiSupport:
    @SuppressWarnings("unchecked")
    public static List<Item> getList( DefaultListModel model ) {
        return (List<Item>) Collections.list( model.elements() );
    }
    
    public void save() {
        try {                        
            // Store properties 
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                final FileObject projectDir = updateHelper.getRakeProjectHelper().getProjectDirectory();
                public Boolean run() throws IOException {
                    if ((genFileHelper.getBuildScriptState(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            RailsProject.class.getResource("resources/build-impl.xsl")) //NOI18N
                            & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) {  //NOI18N
                        if (showModifiedMessage(NbBundle.getMessage(RailsProjectProperties.class, "TXT_ModifiedTitle"))) {
                            //Delete user modified build-impl.xml
                            FileObject fo = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                            if (fo != null) {
                                fo.delete();
                            }
                        }
                        else {
                            return false;
                        }
                    }
                    storeProperties();
                    return true;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
        } 
        catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        catch ( IOException ex ) {
            ErrorManager.getDefault().notify( ex );
        }
    }
    
    
        
    private void storeProperties() throws IOException {
        // Encode all paths (this may change the project properties)
        //this.cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getAntProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX );
        String[] javac_cp = cs.encodeToStrings(/*ClassPathUiSupport.*/getIterator( JAVAC_CLASSPATH_MODEL ) );
        
        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( RakeProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);

        RailsProjectProperties.storePlatform(privateProperties, getPlatform());
        RailsProjectProperties.storeServer(privateProperties, getServer());
        if (getRailsEnvironment() != null) {
            privateProperties.setProperty(RAILS_ENV, getRailsEnvironment()); // NOI18N
        }

        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );
        
        storeRunConfigs(RUN_CONFIGS, projectProperties, privateProperties);
        EditableProperties ep = updateHelper.getProperties("nbproject/private/config.properties"); // NOI18N
        if (activeConfig == null) {
            ep.remove("config"); // NOI18N
        } else {
            ep.setProperty("config", activeConfig); // NOI18N
        }
        updateHelper.putProperties("nbproject/private/config.properties", ep); // NOI18N

        
        // Save all paths
        projectProperties.setProperty( JAVAC_CLASSPATH, javac_cp );
        
        projectProperties.putAll(additionalProperties);
        
        // Store the property changes into the project
        updateHelper.putProperties( RakeProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( RakeProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );        

        // Ugh - this looks like global clobbering!
        String value = additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }
    
    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }
    
    private static boolean showModifiedMessage (String title) {
        String message = NbBundle.getMessage(RailsProjectProperties.class,"TXT_Regenerate");
        JButton regenerateButton = new JButton (NbBundle.getMessage(RailsProjectProperties.class,"CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(RailsProjectProperties.class,"AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});        
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }

    RubyPlatform getPlatform() {
        if (platform == null) {
            platform = RubyPlatform.platformFor(project);
        }
        return platform;
    }

    void setPlatform(final RubyPlatform platform) {
        this.platform = platform;
    }

    public static void storePlatform(final EditableProperties ep, final RubyPlatform platform) {
        ep.setProperty("platform.active", platform.getID()); // NOI18N
    }

    RubyInstance getServer() {
        return this.server;
    }
    
    void setServer(RubyInstance server) {
        this.server = server;
    }
    
    public static void storeServer(final EditableProperties ep, final RubyInstance server) {
        ep.setProperty(RAILS_SERVERTYPE, server.getServerUri()); // NOI18N
    }

    String getRailsEnvironment() {
        return this.railsEnvironment;
    }

    void setRailsEnvironment(String railsEnvironment) {
        this.railsEnvironment = railsEnvironment;
    }
    
    
    /**
     * A mess.
     */
    Map<String/*|null*/,Map<String,String>> readRunConfigs() {
        Map<String,Map<String,String>> m = new TreeMap<String,Map<String,String>>(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        Map<String,String> def = new TreeMap<String,String>();
        for (String prop : new String[] { RAILS_PORT, RAILS_SERVERTYPE, RAKE_ARGS, RAILS_ENV,
                MAIN_CLASS, APPLICATION_ARGS, RUN_JVM_ARGS/*, RUN_WORK_DIR*/}) {
            String v = updateHelper.getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(prop);
            if (v == null) {
                v = updateHelper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                m.put(kid.getName(), new TreeMap<String,String>(updateHelper.getProperties(FileUtil.getRelativePath(project.getProjectDirectory(), kid))));
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                Map<String,String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                c.putAll(new HashMap<String,String>(updateHelper.getProperties(FileUtil.getRelativePath(project.getProjectDirectory(), kid))));
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * A royal mess.
     */
    void storeRunConfigs(Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        //System.err.println("storeRunConfigs: " + configs);
        Map<String,String> def = configs.get(null);
        for (String prop : new String[] {RAILS_PORT, RAILS_SERVERTYPE, RAKE_ARGS, RAILS_ENV,
                MAIN_CLASS, APPLICATION_ARGS, RUN_JVM_ARGS/*, RUN_WORK_DIR*/}) {
            String v = def.get(prop);
            EditableProperties ep = (prop.equals(RAILS_PORT) ||
                    prop.equals(RAKE_ARGS) ||
                    prop.equals(APPLICATION_ARGS)/* || prop.equals(RUN_WORK_DIR)*/) ?
                privateProperties : projectProperties;
            if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                if (v != null && v.length() > 0) {
                    ep.setProperty(prop, v);
                } else {
                    ep.remove(prop);
                }
            }
        }
        for (Map.Entry<String,Map<String,String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }
            String sharedPath = "nbproject/configs/" + config + ".properties"; // NOI18N
            String privatePath = "nbproject/private/configs/" + config + ".properties"; // NOI18N
            Map<String,String> c = entry.getValue();
            if (c == null) {
                updateHelper.putProperties(sharedPath, null);
                updateHelper.putProperties(privatePath, null);
                continue;
            }
            for (Map.Entry<String,String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                String path = (prop.equals(RAILS_PORT) || 
                    prop.equals(RAKE_ARGS) || prop.equals(RAILS_ENV) ||
                    prop.equals(RAILS_SERVERTYPE) || prop.equals(APPLICATION_ARGS) /* || prop.equals(RUN_WORK_DIR)*/) ?
                    privatePath : sharedPath;
                EditableProperties ep = updateHelper.getProperties(path);
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (def.get(prop) != null && def.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    updateHelper.putProperties(path, ep);
                }
            }
            // Make sure the definition file is always created, even if it is empty.
            updateHelper.putProperties(sharedPath, updateHelper.getProperties(sharedPath));
        }
    }
}
