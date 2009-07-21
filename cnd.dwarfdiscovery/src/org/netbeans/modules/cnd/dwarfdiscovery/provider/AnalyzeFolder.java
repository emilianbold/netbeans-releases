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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.discovery.api.DiscoveryProvider.class)
public class AnalyzeFolder extends BaseDwarfProvider {
    private Map<String,ProviderProperty> myProperties = new HashMap<String,ProviderProperty>();
    public static final String FOLDER_KEY = "folder"; // NOI18N
    
    public AnalyzeFolder() {
        clean();
    }
    
    public void clean() {
        myProperties.clear();
        myProperties.put(FOLDER_KEY, new ProviderProperty(){
            private String myPath;
            public String getName() {
                return i18n("Folder_Files_Name"); // NOI18N
            }
            public String getDescription() {
                return i18n("Folder_Files_Description"); // NOI18N
            }
            public Object getValue() {
                return myPath;
            }
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Folder;
            }
        });
        myProperties.put(RESTRICT_SOURCE_ROOT, new ProviderProperty(){
            private String myPath="";
            public String getName() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }
            public String getDescription() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }
            public Object getValue() {
                return myPath;
            }
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
        myProperties.put(RESTRICT_COMPILE_ROOT, new ProviderProperty(){
            private String myPath="";
            public String getName() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }
            public String getDescription() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }
            public Object getValue() {
                return myPath;
            }
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
    }
    
    public String getID() {
        return "dwarf-folder"; // NOI18N
    }
    
    public String getName() {
        return i18n("Folder_Provider_Name"); // NOI18N
    }
    
    public String getDescription() {
        return i18n("Folder_Provider_Description"); // NOI18N
    }
    
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    public int canAnalyze(ProjectProxy project) {
        String root = (String)getProperty(FOLDER_KEY).getValue();
        if (root == null || root.length() == 0) {
            return 0;
        }
        Set<String> set = getObjectFiles(root);
        if (set.size() == 0) {
            return 0;
        }
        int i = 0;
        for(String obj : set){
            i++;
            if (sizeComilationUnit(obj) > 0) {
                return 50;
            }
            if (i > 25) {
                return 0;
            }
        }
        return 0;
    }
    
    public List<Configuration> analyze(ProjectProxy project, final Progress progress) {
        isStoped.set(false);
        List<Configuration> confs = new ArrayList<Configuration>();
        setCommpilerSettings(project);
        if (!isStoped.get()){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration());
                }
                
                public List<Configuration> getDependencies() {
                    return null;
                }
                
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        Set<String> set = getObjectFiles((String)getProperty(FOLDER_KEY).getValue());
                        if (progress != null) {
                            progress.start(set.size());
                        }
                        if (set.size() > 0) {
                            myFileProperties = getSourceFileProperties(set.toArray(new String[set.size()]), progress);
                        } else {
                            myFileProperties = new ArrayList<SourceFileProperties>();
                        }
                        if (progress != null) {
                            progress.done();
                        }
                    }
                    return myFileProperties;
                }
                
                public List<String> getIncludedFiles(){
                    if (myIncludedFiles == null) {
                        HashSet<String> set = new HashSet<String>();
                        for(SourceFileProperties source : getSourcesConfiguration()){
                            if (isStoped.get()) {
                                break;
                            }
                            set.addAll( ((DwarfSource)source).getIncludedFiles() );
                            set.add(source.getItemPath());
                        }
                        HashSet<String> unique = new HashSet<String>();
                        for(String path : set){
                            if (isStoped.get()) {
                                break;
                            }
                            File file = new File(path);
                            if (CndFileUtils.exists(file)) {
                                unique.add(CndFileUtils.normalizeFile(file).getAbsolutePath());
                            }
                        }
                        myIncludedFiles = new ArrayList<String>(unique);
                    }
                    return myIncludedFiles;
                }
            };
            confs.add(conf);
        }
        return confs;
    }
    
    private Set<String> getObjectFiles(String root){
        HashSet<String> set = new HashSet<String>();
        gatherSubFolders(new File(root), set);
        HashSet<String> map = new HashSet<String>();
        for (Iterator it = set.iterator(); it.hasNext();){
            if (isStoped.get()) {
                break;
            }
            File d = new File((String)it.next());
            if (d.exists() && d.isDirectory() && d.canRead()){
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    if (ff[i].isFile()) {
                        String name = ff[i].getName();
                        if (name.endsWith(".o") ||  // NOI18N
                            name.endsWith(".so") ||  // NOI18N
                            name.endsWith(".dylib") ||  // NOI18N
                            name.endsWith(".a") ||  // NOI18N
                            isExecutable(ff[i])){
                            String path = ff[i].getAbsolutePath();
                            if (Utilities.isWindows()) {
                                path = path.replace('\\', '/');
                            }
                            map.add(path);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private boolean isExecutable(File file){
        String name = file.getName();
        if (Utilities.isWindows()) {
            return name.endsWith(".exe") || name.endsWith(".dll");  // NOI18N
        } else if (Utilities.isUnix()){
            // FIXUP: There are no way to detect "executable".
            return name.indexOf('.') < 0;
            //try{
            //    //Since 1.6
            //    return file.canExecute();
            //} catch (SecurityException ex) {
            //}
        }
        return false;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set){
        if (isStoped.get()) {
            return;
        }
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (DiscoveryUtils.ignoreFolder(d)){
                return;
            }
            String path = d.getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    gatherSubFolders(ff[i], set);
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeFolder.class,id);
    }
}
