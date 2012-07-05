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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class AnalyzeExecutable extends BaseDwarfProvider {
    private Map<String,ProviderProperty> myProperties = new LinkedHashMap<String,ProviderProperty>();
    public static final String EXECUTABLE_KEY = "executable"; // NOI18N
    public static final String LIBRARIES_KEY = "libraries"; // NOI18N
    public static final String FILE_SYSTEM = "filesystem"; // NOI18N
    public static final String FIND_MAIN_KEY = "find_main"; // NOI18N
    public static final String EXECUTABLE_PROVIDER_ID = "dwarf-executable"; // NOI18N

    public AnalyzeExecutable() {
        clean();
    }
    
    @Override
    public final void clean() {
        myProperties.clear();
        myProperties.put(EXECUTABLE_KEY, new ProviderProperty(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Executable_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Executable_Files_Description"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myPath;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.BinaryFile;
            }
        });
        myProperties.put(FILE_SYSTEM, new ProviderProperty(){
            private FileSystem fs;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public Object getValue() {
                return fs;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof FileSystem){
                    fs = (FileSystem)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Object;
            }
        });
        myProperties.put(LIBRARIES_KEY, new ProviderProperty(){
            private String myPath[];
            @Override
            public String getName() {
                return i18n("Libraries_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Libraries_Files_Description"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myPath;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof String[]){
                    myPath = (String[])value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.BinaryFiles;
            }
        });
        myProperties.put(FIND_MAIN_KEY, new ProviderProperty(){
            private Boolean findMain = Boolean.TRUE;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public Object getValue() {
                return findMain;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof Boolean){
                    findMain = (Boolean)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Boolean;
            }
        });
        myProperties.put(RESTRICT_SOURCE_ROOT, new ProviderProperty(){
            private String myPath="";
            @Override
            public String getName() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myPath;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
        myProperties.put(RESTRICT_COMPILE_ROOT, new ProviderProperty(){
            private String myPath="";
            @Override
            public String getName() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }
            @Override
            public Object getValue() {
                return myPath;
            }
            @Override
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
    }
    
    @Override
    public String getID() {
        return EXECUTABLE_PROVIDER_ID; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Executable_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Executable_Provider_Description"); // NOI18N
    }
    
    @Override
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    @Override
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project) {
        String set = (String)getProperty(EXECUTABLE_KEY).getValue();
        if (set == null || set.length() == 0) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecutable.class, "NoExecutable")));
        }
        boolean findMain = (Boolean)getProperty(FIND_MAIN_KEY).getValue();
        Set<String> dlls = new HashSet<String>();
        FileSystem fs = (FileSystem) getProperty(FILE_SYSTEM).getValue();
        if (fs == null) {
            ApplicableImpl applicable = sizeComilationUnit(set, dlls, findMain);
            if (applicable.isApplicable()) {
                return new ApplicableImpl(true, applicable.getErrors(), applicable.getCompilerName(), 70, applicable.isSunStudio(),
                        applicable.getDependencies(), applicable.getSearchPaths(), applicable.getSourceRoot(), applicable.getMainFunction());
            }
            if (applicable.getErrors().size() > 0) {
                return ApplicableImpl.getNotApplicable(applicable.getErrors());
            }
        } else {
            RemoteJavaExecution processor = new RemoteJavaExecution(fs);
            ElfReader.SharedLibraries libs = processor.getDlls(set);
            if (libs != null) {
                return new ApplicableImpl(true, null, null, 0, false, libs.getDlls(), libs.getPaths(), processor.getSourceRoot(processor.getCompileLines(set)), null);
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecutable.class, "CannotAnalyzeExecutable",set)));
    }
    
    @Override
    public List<Configuration> analyze(final ProjectProxy project, Progress progress) {
        isStoped.set(false);
        List<Configuration> confs = new ArrayList<Configuration>();
        setCommpilerSettings(project);
        if (!isStoped.get()){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;
                private Set<String> myDependencies;
                @Override
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration(), project);
                }
                
                @Override
                public List<String> getDependencies() {
                    if (myDependencies == null) {
                        getSourcesConfiguration();
                    }
                    return new ArrayList<String>(myDependencies);
                }
                
                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        myDependencies = new HashSet<String>();
                        String set = (String)getProperty(EXECUTABLE_KEY).getValue();
                        if (set != null && set.length() > 0) {
                            String[] add = (String[])getProperty(LIBRARIES_KEY).getValue();
                            if (add == null || add.length==0) {
                                myFileProperties = getSourceFileProperties(new String[]{set},null, project, myDependencies, new CompileLineStorage());
                            } else {
                                String[] all = new String[add.length+1];
                                all[0] = set;
                                System.arraycopy(add, 0, all, 1, add.length);
                                myFileProperties = getSourceFileProperties(all,null, project, myDependencies, new CompileLineStorage());
                            }
                        }
                    }
                    return myFileProperties;
                }
                
                @Override
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
        return NbBundle.getMessage(AnalyzeFolder.class,id);
    }
}
