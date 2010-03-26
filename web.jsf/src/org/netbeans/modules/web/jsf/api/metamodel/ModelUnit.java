/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.jsf.api.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;


/**
 * For given classpath the list of configuration files is maintained and
 * event is fired when list of configuration files or individual
 * file has changed.
 */
public class ModelUnit {
    
    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;
    private final WebModule module;
    
    private final PropertyChangeSupport changeSupport;

    private List<FileObject> configFiles;

    private static final String META_INF = "META-INF";      // NOI18N
    private static final String FACES_CONFIG = "faces-config.xml";// NOI18N
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml"; // NOI18N
    private static final String DEFAULT_FACES_CONFIG_PATH = "WEB-INF/faces-config.xml"; //NOI18N

    /**
     * Property name which is fired when there was
     * a configuration files relevant change.
     */
    public final String PROP_CONFIG_FILES = "configFiles";

    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, WebModule webModule )
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, 
                webModule);
    }
    
    private ModelUnit(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath,  WebModule webModule ) {
        Parameters.notNull("sourcePath", sourcePath);
        this.bootPath= bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.module = webModule;
        changeSupport = new PropertyChangeSupport(this);
        initListeners();
    }
    
    
    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }

    /**
     * Returns main faces-config.xml file if presented or null.
     */
    public FileObject getApplicationFacesConfig() {
        List<FileObject> l = getConfigFilesImpl();
        if (l.size() == 0) {
            return null;
        }
        FileObject first = l.iterator().next();

        FileObject documentBase = module.getDocumentBase();
        if (documentBase == null) {
            return null;
        }
        FileObject mainConfigFile = documentBase.getFileObject(DEFAULT_FACES_CONFIG_PATH);
        if (mainConfigFile != null && mainConfigFile.equals(first)) {
            return first;
        }
        return null;
    }

    /**
     * Returns list of other configuration files excluding
     * the main faces-config.xml file. Returns always non-null potentially
     * empty list.
     */
    public List<FileObject> getApplicationConfigurationResources() {
        List<FileObject> l = getConfigFilesImpl();
        FileObject applicationFacesConfig = getApplicationFacesConfig();
        if (applicationFacesConfig != null) {
            return l.subList(1, l.size());
        } else {
            return l;
        }
    }

    private void collectConfigurationFilesFromClassPath(ClassPath cp, List<FileObject> configs) {
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject roots[];
            if (entry.isValid()) {
                roots = new FileObject[]{entry.getRoot()};
            } else {
                // if classpath root does not exist then perhaps it is
                // a project which has not been built - use SourceForBinaryQuery
                // to use project sources instead:
                SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                roots = res.getRoots();
            }
            for (FileObject root : roots) {
                FileObject metaInf = root.getFileObject(META_INF);
                if (metaInf != null) {
                    FileObject[] children = metaInf.getChildren();
                    for (FileObject fileObject : children) {
                        String name = fileObject.getNameExt();
                        if (name.equals(FACES_CONFIG) || name.endsWith(FACES_CONFIG_SUFFIX)) {
                             if(!configs.contains(fileObject)) {
                                //do not duplicate
                                configs.add( fileObject );
                             }
                        }
                    }
                }
            }
        }
    }

    private synchronized List<FileObject> getConfigFiles() {
        return configFiles;
    }

    private synchronized void setConfigFiles(List<FileObject> configFiles) {
        this.configFiles = configFiles;
    }

    private List<FileObject> getConfigFilesImpl() {
        List<FileObject> configs = getConfigFiles();
        if (configs != null) {
            return configs;
        }
        FileObject[] objects = ConfigurationUtils.getFacesConfigFiles( module );
        //add all the configs from WEB-INF/faces-config.xml and all configs declared in faces config DD entry
        //we need to ensure the original ordering
        configs = new LinkedList<FileObject>(Arrays.asList(objects));
        //find for configs in meta-inf
        collectConfigurationFilesFromClassPath(sourcePath, configs);
        collectConfigurationFilesFromClassPath(compilePath, configs);
        setConfigFiles(configs);
        return configs;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private void fireChange() {
        // reset list of config files to be re-read:
        setConfigFiles(null);
        
        changeSupport.firePropertyChange(PROP_CONFIG_FILES, null, null);
    }

    private void initListeners() {
        Listener l = new Listener();
        // listen to any change on source path classpath:
        sourcePath.addPropertyChangeListener(l);
        // listen to any change on compilation classpath:
        compilePath.addPropertyChangeListener(l);
        // listen to any relevant configuration file change:
        FileUtil.addFileChangeListener(l);
    }

    private class Listener implements FileChangeListener, PropertyChangeListener {

        public void propertyChange( PropertyChangeEvent event ) {
            if (event.getPropertyName().equals(ClassPath.PROP_ENTRIES)) {
                fireChange();
            }
        }
        
        private boolean isRelevantFileEvent(FileEvent fe) {
            // relevant files changes are (JSF spec "11.4.2 Application Startup Behavior"):
            //   - resources that match either "META-INF/faces-config.xml" or
            //     end with ".facesconfig.xml" directly in the "META-INF" directory
            //   - existence of a context initialization parameter named javax.faces.CONFIG_FILES iun web.xml
            //   - /WEB-INF/faces-config.xml
            String path = fe.getFile().getPath();
            boolean res = path.endsWith("/web.xml") ||
                   path.endsWith("/WEB-INF/faces-config.xml") ||
                   path.endsWith("/META-INF/faces-config.xml") ||
                  (path.endsWith(FACES_CONFIG_SUFFIX) && fe.getFile().getParent() != null && fe.getFile().getParent().getNameExt().equals("META-INF"));
            if (!res && fe instanceof FileRenameEvent) {
                FileRenameEvent fre = (FileRenameEvent)fe;
                res = (fre.getName().equals("faces-config") || fre.getName().endsWith(".faces-config") || fre.getName().endsWith("web.xml")) &&
                        fre.getExt().equals("xml");
            }
            return res;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (isRelevantFileEvent(fe)) {
                fireChange();
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (isRelevantFileEvent(fe)) {
                fireChange();
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (isRelevantFileEvent(fe)) {
                fireChange();
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (isRelevantFileEvent(fe)) {
                fireChange();
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
    }
}
