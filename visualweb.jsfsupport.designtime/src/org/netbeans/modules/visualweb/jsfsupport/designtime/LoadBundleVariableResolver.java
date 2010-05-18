/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.jsfsupport.designtime;

import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.xhtml.F_LoadBundle;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.event.DesignProjectAdapter;
import com.sun.rave.designtime.event.DesignProjectListener;
import com.sun.rave.designtime.faces.FacesDesignProject;
import org.netbeans.modules.visualweb.jsfsupport.container.RaveFacesContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;


/**
 * A design-time variable resolver that mimics the JSF loadBundle tag. The resolver
 * attempts to determine if the requested variable corresponds to a property bundle
 * file in the project source directory. If a property bundle file is found, the
 * resolver creates a map of all key-value pairs found in the file, and returns it
 * as the resolution of the variable. The map is cached, to expedite future requests
 * for the same variable.
 *
 * <p>Each bundle map listens for changes in its underlying file. If the file is
 * modified, the map keys are refreshed. Note that this only happens when the user
 * explicitly saves the bundle file in the IDE.
 *
 * <p>If the underyling file is renamed or deleted, the map is destroyed.
 *
 * @author gjmurphy
 */
public class LoadBundleVariableResolver extends VariableResolver {

    private static final String VAR_PROPERTY = "var"; //NOI18N
    private static final String BASENAME_PROPERTY = "basename"; //NOI18N

    // Listener for context close event on the design project
    private DesignProjectListener projectListener;
    // Parent of this variable resolver, to which requests are delegated
    private VariableResolver parentVariableResolver;
    // A map of resource bundle base names to maps containing the key/value
    // pairs defined by the corresponding property resource bundle file
    private Map<String,Map> propertyResourceMapMap = new HashMap<String,Map>();

    public LoadBundleVariableResolver(VariableResolver parentVariableResolver){
        this.parentVariableResolver =  parentVariableResolver;
    }

    // @Override
    public Object resolveVariable(FacesContext context, String name) {
        DesignContext designContext = ((RaveFacesContext) context).getDesignContext();
        DesignBean loadBundleDesignBean = null;
        if (name != null && designContext != null) {
            // Search for a LoadBundle design bean that corresponds to the variable name
            DesignProject designProject = designContext.getProject();
            if (designProject instanceof FacesDesignProject) {
                DesignContext candidateDesignContext =
                        ((FacesDesignProject) designProject).findDesignContext(name);
                if (candidateDesignContext == null) {
                    DesignBean[] loadBundleBeans = designContext.getBeansOfType(F_LoadBundle.class);
                    for (DesignBean designBean : loadBundleBeans) {
                        if (name.equals(designBean.getProperty(VAR_PROPERTY).getValue())) {
                            loadBundleDesignBean = designBean;
                            break;
                        }
                    }
                }
            }
        }
        if (loadBundleDesignBean != null) {
            // If a LoadBundle design bean was found, fetch the corresponding properties map
            // from cache (or create a new map if not found in the cache)
            String bundleBaseName = (String) loadBundleDesignBean.getProperty(BASENAME_PROPERTY).getValue();
            if (bundleBaseName != null && bundleBaseName.length() > 0) {
                // Lazy initialization of design context listener
                if (this.projectListener == null) {
                    this.projectListener = new LoadBundleProjectListener(designContext);
                    designContext.getProject().addDesignProjectListener(this.projectListener);
                }
                Map propertyResourceMap = this.propertyResourceMapMap.get(bundleBaseName);
                if (propertyResourceMap == null) {
                    List<String> candidateBundleNames =
                            generateCandidateBundleNames(bundleBaseName, Locale.getDefault());
                    File sourceDir = this.getSourceDir(loadBundleDesignBean);
                    for (String candidateBundleName : candidateBundleNames) {
                        try {
                            File resourceBundleFile = new File(sourceDir, candidateBundleName);
                            if (resourceBundleFile.exists()) {
                                propertyResourceMap = new PropertyResourceMap(bundleBaseName, resourceBundleFile);
                                this.propertyResourceMapMap.put(bundleBaseName, propertyResourceMap);
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (propertyResourceMap != null)
                    return propertyResourceMap;
            }
        }
        return parentVariableResolver.resolveVariable(context, name);
    }
    
    
    private static final String SOURCE_DIR = "src/";
    private static final URI SOURCE_URI = URI.create(SOURCE_DIR);
    
    private File sourceDir;
    
    /**
     * A utility method that returns a file object which represents the root
     * source directory of the current project.
     */
    private File getSourceDir(DesignBean designBean) {
        if (sourceDir == null)
            sourceDir = designBean.getDesignContext().getProject().getResourceFile(SOURCE_URI);
        // Depending on the project type, the source directory will be either ./src or
        // ./src/java
        File javaSourceDir = new File(sourceDir, "java");
        if (javaSourceDir.exists())
            sourceDir = javaSourceDir;
        return sourceDir;
    }
    
    private static final String BUNDLE_SUFFIX = ".properties";
    
    /**
     * A utility method that generates a list of all candidate bundle file names
     * for the base name and locale specified.
     */
    private static List<String> generateCandidateBundleNames(String baseName, Locale locale) {
        List<String> candidateBundleNames = new ArrayList<String>(8);
        String language = locale.getLanguage();
        int languageLength = language.length();
        String country = locale.getCountry();
        int countryLength = country.length();
        String variant = locale.getVariant();
        int variantLength = variant.length();
        
        if (baseName == null)
            return candidateBundleNames;
        StringBuffer buffer = new StringBuffer(baseName.replace('.', '/'));
        int bufferLength = buffer.length();
        buffer.append(BUNDLE_SUFFIX);
        candidateBundleNames.add(buffer.toString());
        buffer.setLength(bufferLength);
        
        if (languageLength + countryLength + variantLength == 0)
            return candidateBundleNames;
        
        buffer.append('_');
        buffer.append(language);
        if (languageLength > 0) {
            bufferLength = buffer.length();
            buffer.append(BUNDLE_SUFFIX);
            candidateBundleNames.add(0, buffer.toString());
            buffer.setLength(bufferLength);
        }
        
        if (countryLength + variantLength == 0)
            return candidateBundleNames;
        buffer.append('_');
        buffer.append(country);
        if (countryLength > 0) {
            bufferLength = buffer.length();
            buffer.append(BUNDLE_SUFFIX);
            candidateBundleNames.add(0, buffer.toString());
            buffer.setLength(bufferLength);
        }
        
        if (variantLength == 0)
            return candidateBundleNames;
        buffer.append('_');
        buffer.append(variant);
        buffer.append(BUNDLE_SUFFIX);
        candidateBundleNames.add(0, buffer.toString());
        
        return candidateBundleNames;
    }
    
    void clearPropertyResourceMap(String bundleBaseName) {
        Map propertyResourceMap = LoadBundleVariableResolver.this.propertyResourceMapMap.get(bundleBaseName);
        propertyResourceMap.clear();
        LoadBundleVariableResolver.this.propertyResourceMapMap.remove(bundleBaseName);
    }
    
    
    /**
     * A design context listener that removes all file object listeners when this
     * property resolver's design context is deactivated.
     */
    class LoadBundleProjectListener extends DesignProjectAdapter {
        
        DesignContext context;
        
        LoadBundleProjectListener(DesignContext context) {
            this.context = context;
        }
        
        public void contextClosed(DesignContext context) {
            if (this.context == context) {
                // System.err.println("CONTEXT DEACTIVATED");
                context.getProject().removeDesignProjectListener(this);
                LoadBundleVariableResolver.this.projectListener = null;
                for (String bundleBaseName : LoadBundleVariableResolver.this.propertyResourceMapMap.keySet()) {
                    clearPropertyResourceMap(bundleBaseName);
                }
            }
        }
        
    }
    
    
    /**
     * A property resource bundle that listens for changes to its underlying
     * property file, and refreshes all of its keys in response to changes in the
     * file. The bundle is implemented as a {@link java.util.Map} so that it can
     * be returned directly in response to requests to resolve variables that
     * reference load bundle components.
     */
    class PropertyResourceMap extends HashMap {
        
        FileChangeListener fileChangeListener;
        
        PropertyResourceMap(String bundleBaseName, File propertyResourceFile) throws IOException {
            // System.err.println("FILE CREATED");
            this.setFileObject(FileUtil.toFileObject(propertyResourceFile));
            this.setBundleBaseName(bundleBaseName);
            this.fileChangeListener = new FileChangeAdapter() {
                public void fileRenamed(FileRenameEvent fileRenameEvent) {
                    // System.err.println("FILE RENAMED");
                    setFileObject(fileRenameEvent.getFile());
                    LoadBundleVariableResolver.this.clearPropertyResourceMap(getBundleBaseName());
                }
                public void fileDeleted(FileEvent fileEvent) {
                    // System.err.println("FILE DELETED");
                    setFileObject(fileEvent.getFile());
                    LoadBundleVariableResolver.this.clearPropertyResourceMap(getBundleBaseName());
                }
                public void fileChanged(FileEvent fileEvent) {
                    // System.err.println("FILE CHANGED");
                    setFileObject(fileEvent.getFile());
                    try {
                        update();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            FileObject fileObject = this.getFileObject();
            fileObject.addFileChangeListener(this.fileChangeListener);
            update();
        }
        
        public void clear() {
            // System.err.println("REMOVING LISTENER FOR " + this.getBundleBaseName());
            FileObject fileObject = this.getFileObject();
            fileObject.removeFileChangeListener(this.fileChangeListener);
            super.clear();
        }
        
        private void update() throws IOException {
            File file = FileUtil.toFile(this.getFileObject());
            ResourceBundle resourceBundle = new PropertyResourceBundle(new FileInputStream(file));
            Enumeration<String> keysEnumeration = resourceBundle.getKeys();
            super.clear();
            while (keysEnumeration.hasMoreElements()) {
                String key = keysEnumeration.nextElement();
                this.put(key, resourceBundle.getString(key));
            }
            resourceBundle = null;
        }
        
        private String bundleBaseName;
        
        String getBundleBaseName() {
            return this.bundleBaseName;
        }
        
        void setBundleBaseName(String bundleBaseName) {
            this.bundleBaseName = bundleBaseName;
        }

        private FileObject fileObject;

        public FileObject getFileObject() {
            return this.fileObject;
        }

        public void setFileObject(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Object get(Object key) {
            Object value = super.get(key);
            if (value == null && key instanceof String)
                value = "??? " + (String) key + " ???";
            return value;
        }
        
    }
    
}
