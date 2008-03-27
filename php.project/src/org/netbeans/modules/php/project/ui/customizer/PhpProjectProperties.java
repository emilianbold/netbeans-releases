/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ResourceMarker;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.classpath.ClassPathSupport;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;


/**
 * @author ads
 *
 */
public class PhpProjectProperties {

    public static final String STATUS_USE_NO_HOST = "use_no_host";
    public static final String STATUS_ABSENT_HOST = "absent_host";

    public static final String SRC_              = "src.";               // NOI18N

    public static final String _DIR              = "dir";                // NOI18N

    public static final String SRC                = SRC_ + _DIR;

    public static final String SRC_DIR            = "${" + SRC + "}";     // NOI18N

    public static final String TMP_FILE_POSTFIX   = "~";     // NOI18N

    public static final String PROVIDER_ID  = "provider.id";        // NOI18N

    public static final String VERSION      = "version";            // NOI18N

    public static final String COMMAND_PATH = "command.path";       // NOI18N

    public static final String NAME        
            = PhpProjectSharedConstants.PHP_PROJECT_NAME; // NOI18N

    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N

    public static final String SOURCE_LBL  = "LBL_Node_Sources";   // NOI18N

    public static final String SOURCES_TYPE_PHP 
            = PhpProjectSharedConstants.SOURCES_TYPE_PHP;

    public static final String COPY_SRC_FILES = "copy.src.files"; // NOI18N
    public static final String COPY_SRC_TARGET = "copy.src.target"; // NOI18N
    public static final String URL = "url"; // NOI18N
    public static final String INCLUDE_PATH = "include.path"; // NOI18N
    // XXX will be replaced with global ide include path
    public static final String GLOBAL_INCLUDE_PATH = "global.include.path"; // NOI18N

    public static final Icon PROJECT_ICON = 
        new ImageIcon(Utilities.loadImage( 
                ResourceMarker.getLocation()+ResourceMarker.PROJECT_ICON ));

    private final ClassPathSupport classPathSupport;
    
    public final DefaultListModel INCLUDE_PATH_MODEL;
    public final ListCellRenderer INCLUDE_PATH_LIST_RENDERER;

    private static final Logger LOGGER = Logger.getLogger(PhpProjectProperties.class.getName());
    
    public PhpProjectProperties(PhpProject project, ClassPathSupport classPathSupport) {
        assert project != null;
        assert classPathSupport != null;

        myProject = project;
        this.classPathSupport = classPathSupport;
        // XXX
        load();

        INCLUDE_PATH_MODEL = ClassPathUiSupport.createListModel(classPathSupport.itemsIterator(myProperties.getProperty(INCLUDE_PATH)));
        INCLUDE_PATH_LIST_RENDERER = new ClassPathUiSupport.ClassPathListCellRenderer(project.getEvaluator(), project.getProjectDirectory());
    }

    public void save() {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {

            public Host run() {
                AntProjectHelper helper = getAntProjectHelper(getProject());
                
                // encode include path
                String[] includePath = classPathSupport.encodeToStrings(
                        ClassPathUiSupport.getIterator(INCLUDE_PATH_MODEL));
                
                EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

                /*
                 * Set source roots.
                 */
                configureSources(properties);
                /*
                 * Set Encoding
                 */
                configureEncoding(properties);
                /*
                 * Save provider and host properties
                 */
                configureProvider(properties);
                /*
                 * Set Commang line
                 */
                configureCommandLine(properties);
                
                /*
                 * Set version.
                 */
                //properties.setProperty( PhpProject.VERSION,  myProperties.getProperty( PhpProject.VERSION) );
                
                properties.setProperty(INCLUDE_PATH, includePath);
                
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);

                properties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, properties);

                /* delegate server specific configuration to provider */
                configureHost(helper);

                try {
                    ProjectManager.getDefault().saveProject(getProject());
                    // check whether src directory exists - if not, create it (can happen using customizer)
                    String src = getProject().getEvaluator().getProperty(SRC);
                    File srcDir = PropertyUtils.resolveFile(FileUtil.toFile(getProject().getProjectDirectory()), src);
                    if (!srcDir.exists()) {
                        FileUtil.createFolder(srcDir);
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }

                return null;
            }

            private void configureCommandLine(EditableProperties properties) {
                if (myProperties.containsKey(COMMAND_PATH)) {
                    String cmd = myProperties.getProperty(COMMAND_PATH);
                    properties.setProperty(COMMAND_PATH, cmd);
                } else {
                    properties.remove(COMMAND_PATH);
                }
            }
            
            private void configureProvider(EditableProperties properties) {
                if (myProperties.containsKey(STATUS_USE_NO_HOST)) {
                    properties.remove(PROVIDER_ID);
                    properties.remove(WebServerProvider.HOST_ID);
                    return;
                }


                String hostId = myProperties.getProperty(WebServerProvider.HOST_ID);
                if (hostId != null) {
                    Host host = Utils.findHostById(hostId);
                    // project's host is not removed
                    if (host != null) {
                        WebServerProvider provider = host.getProvider();
                        String provider_id = provider.getClass().getCanonicalName();
                        properties.setProperty(PROVIDER_ID, provider_id);
                        properties.setProperty(WebServerProvider.HOST_ID, host.getId());
                    }
                } else {
                    /* else is command line or there are no configured hosts
                     * TODO separate this.
                     * Now (hostId == null) means that we shouldn't overwrite values stored in properties  
                     */
                }
            }

            private void configureHost(AntProjectHelper helper) {
                if (myProperties.containsKey(STATUS_USE_NO_HOST)) {
                    return;
                }

                String hostId = myProperties.getProperty(WebServerProvider.HOST_ID);
                WebServerProvider provider = Utils.getProvider(getProject());

                if (hostId != null && provider != null) {
                    //Host host = provider.findHost(hostId);
                    ProjectConfigProvider confProvider = provider.getProjectConfigProvider();
                    confProvider.customizeProject(helper, myProperties);
                }
            }

            // TODO now it processes only one source root dir
            private void configureSources(EditableProperties properties) {

                properties.setProperty(SRC, myProperties.getProperty(SRC));

            }

            private void configureEncoding(EditableProperties properties) {
                
                properties.setProperty(SOURCE_ENCODING, 
                        myProperties.getProperty(SOURCE_ENCODING));

                // Ugh - this looks like global clobbering!
                String value = properties.get(SOURCE_ENCODING);
                if (value != null) {
                    try {
                        FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
                    } catch (UnsupportedCharsetException e) {
                        //When the encoding is not supported by JVM do not set it as default
                    }
                }
            }
        });
                AntProjectHelper helper = getAntProjectHelper(getProject());

        helper. getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
    }

    public PhpProject getProject() {
        return myProject;
    }

    // XXX remove this method
    public EditableProperties load() {
        ProjectManager.mutex().readAccess(new Mutex.Action<Object>() {

            public Host run() {
                myProperties = getAntProjectHelper(getProject()).getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

                return null;
            }
        });
        return myProperties;
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public void setProperty(String key, String value) {
        getProperties().setProperty(key, value);
    }

    public String remove(String key) {
        return getProperties().remove(key);
    }

    public boolean containsKey(String key) {
        return getProperties().containsKey(key);
    }


    /**
     * use load() before to load properties
     * @return EditableProperties object. null if properties were not loaded using load()
     */
    public EditableProperties getProperties() {
        return myProperties;
    }

    private AntProjectHelper getAntProjectHelper(Project project) {
        return project.getLookup().lookup(AntProjectHelper.class);
    }

    EditableProperties myProperties;

    private PhpProject myProject;

    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
}
