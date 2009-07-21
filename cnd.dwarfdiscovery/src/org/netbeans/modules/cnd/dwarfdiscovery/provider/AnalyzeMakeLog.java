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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.discovery.api.DiscoveryProvider.class)
public class AnalyzeMakeLog extends BaseDwarfProvider {
    private Map<String,ProviderProperty> myProperties = new LinkedHashMap<String,ProviderProperty>();
    public static final String MAKE_LOG_KEY = "make-log-file"; // NOI18N
    
    public AnalyzeMakeLog() {
        clean();
    }
    
    public void clean() {
        myProperties.clear();
        myProperties.put(MAKE_LOG_KEY, new ProviderProperty(){
            private String myPath;
            public String getName() {
                return i18n("Make_Log_File_Name"); // NOI18N
            }
            public String getDescription() {
                return i18n("Make_Log_File_Description"); // NOI18N
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
                return ProviderProperty.PropertyKind.MakeLogFile;
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
        return "make-log"; // NOI18N
    }
    
    public String getName() {
        return i18n("Make_Log_Provider_Name"); // NOI18N
    }
    
    public String getDescription() {
        return i18n("Make_Log_Provider_Description"); // NOI18N
    }
    
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }

    @Override
    public boolean isApplicable(ProjectProxy project) {
//        if (detectMakeLog(project) != null){
        Object o = getProperty(RESTRICT_COMPILE_ROOT).getValue();
        if (o == null || "".equals(o.toString())){ // NOI18N
            getProperty(RESTRICT_COMPILE_ROOT).setValue(project.getSourceRoot());
            return true;
        }
        return false;
    }
    
    private String detectMakeLog(ProjectProxy project){
        String root = project.getSourceRoot();
        int i = root.indexOf("/usr/src/"); // NOI18N
        if (i < 0 && root.endsWith("/usr/src")){ // NOI18N
            i = root.indexOf("/usr/src"); // NOI18N
        }
        if (i > 0) {
            String latest = null;
            String logfolder = root.substring(0, i) + "/log"; // NOI18N
            File log = new File(logfolder);
            if (log.exists() && log.isDirectory() && log.canRead()) {
                for (File when : log.listFiles()) {
                    if (when.exists() && when.isDirectory() && when.canRead()) {
                        for (File l : when.listFiles()) {
                            String current = l.getAbsolutePath();
                            if (current.endsWith("/nightly.log")) { // NOI18N
                                if (latest == null) {
                                    latest = current;
                                } else {
                                    String folder1 = latest.substring(0, latest.lastIndexOf("/nightly.log")); // NOI18N
                                    String folder2 = current.substring(0, current.lastIndexOf("/nightly.log")); // NOI18N
                                    if (folder1.compareTo(folder2) < 0) {
                                        latest = current;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            return latest;
        }
        return null;
    }
    
    public int canAnalyze(ProjectProxy project) {
        String set = (String)getProperty(MAKE_LOG_KEY).getValue();
        if (set == null || set.length() == 0) {
            set = detectMakeLog(project);
            if (set != null && set.length() > 0){
                getProperty(MAKE_LOG_KEY).setValue(set);
            }
        }
        if (set == null || set.length() == 0) {
            return 0;
        }
        return 80;
    }

    @Override
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String,SourceFileProperties> map){
        ProviderProperty p = getProperty(RESTRICT_COMPILE_ROOT);
        String root = "";
        if (p != null) {
            root = (String)p.getValue();
        }
        List<SourceFileProperties> res = runLogReader(objFileName, root, progress);
        progress = null;
        return res;

    }
    
    /* package-local */ List<SourceFileProperties> runLogReader(String objFileName, String root, Progress progress){
        LogReader clrf = new LogReader(objFileName, root);
        List<SourceFileProperties> list = clrf.getResults(progress, isStoped);
        return list;
    }
    
    private Progress progress;
    public List<Configuration> analyze(final ProjectProxy project, Progress progress) {
        isStoped.set(false);
        List<Configuration> confs = new ArrayList<Configuration>();
        setCommpilerSettings(project);
        this.progress = progress;
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
                        String set = (String)getProperty(MAKE_LOG_KEY).getValue();
                        if (set == null || set.length() == 0) {
                            set = detectMakeLog(project);
                        }
                        if (set != null && set.length() > 0) {
                            myFileProperties = getSourceFileProperties(new String[]{set},null);
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
                            if (source instanceof DwarfSource) {
                                set.addAll( ((DwarfSource)source).getIncludedFiles() );
                                set.add(source.getItemPath());
                            }
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
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeMakeLog.class,id);
    }
}
