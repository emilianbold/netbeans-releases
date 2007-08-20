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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class AnalyzeExecutable extends BaseDwarfProvider {
    private Map<String,ProviderProperty> myProperties = new LinkedHashMap<String,ProviderProperty>();
    public static final String EXECUTABLE_KEY = "executable"; // NOI18N
    public static final String LIBRARIES_KEY = "libraries"; // NOI18N
    
    public AnalyzeExecutable() {
        clean();
    }
    
    public void clean() {
        myProperties.clear();
        myProperties.put(EXECUTABLE_KEY, new ProviderProperty(){
            private String myPath;
            public String getName() {
                return i18n("Executable_Files_Name"); // NOI18N
            }
            public String getDescription() {
                return i18n("Executable_Files_Description"); // NOI18N
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
                return ProviderProperty.PropertyKind.BinaryFile;
            }
        });
        myProperties.put(LIBRARIES_KEY, new ProviderProperty(){
            private String myPath[];
            public String getName() {
                return i18n("Libraries_Files_Name"); // NOI18N
            }
            public String getDescription() {
                return i18n("Libraries_Files_Description"); // NOI18N
            }
            public Object getValue() {
                return myPath;
            }
            public void setValue(Object value) {
                if (value instanceof String[]){
                    myPath = (String[])value;
                }
            }
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.BinaryFiles;
            }
        });
    }
    
    public String getID() {
        return "dwarf-executable"; // NOI18N
    }
    
    public String getName() {
        return i18n("Executable_Provider_Name"); // NOI18N
    }
    
    public String getDescription() {
        return i18n("Executable_Provider_Description"); // NOI18N
    }
    
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    @Override
    public boolean canAnalyze(ProjectProxy project) {
        String set = (String)getProperty(EXECUTABLE_KEY).getValue();
        if (set == null || set.length() == 0) {
            return false;
        }
        return sizeComilationUnit(set) > 0;
    }
    
    public List<Configuration> analyze(ProjectProxy project) {
        isStoped = false;
        List<Configuration> confs = new ArrayList<Configuration>();
        setCommpilerSettings(project);
        if (!isStoped){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;
                public List<ProjectProperties> getProjectConfiguration() {
                    return divideByLanguage(getSourcesConfiguration());
                }
                
                public List<Configuration> getDependencies() {
                    return null;
                }
                
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        String set = (String)getProperty(EXECUTABLE_KEY).getValue();
                        if (set != null && set.length() > 0) {
                            String[] add = (String[])getProperty(LIBRARIES_KEY).getValue();
                            if (add == null || add.length==0) {
                                myFileProperties = getSourceFileProperties(new String[]{set});
                            } else {
                                String[] all = new String[add.length+1];
                                all[0] = set;
                                for(int i = 0; i < add.length; i++){
                                    all[i+1]=add[i];
                                }
                                myFileProperties = getSourceFileProperties(all);
                            }
                        }
                    }
                    return myFileProperties;
                }
                
                public List<String> getIncludedFiles(){
                    if (myIncludedFiles == null) {
                        HashSet<String> set = new HashSet<String>();
                        for(SourceFileProperties source : getSourcesConfiguration()){
                            if (isStoped) {
                                break;
                            }
                            set.addAll( ((DwarfSource)source).getIncludedFiles() );
                            set.add(source.getItemPath());
                        }
                        HashSet<String> unique = new HashSet<String>();
                        for(String path : set){
                            if (isStoped) {
                                break;
                            }
                            File file = new File(path);
                            if (file.exists()) {
                                unique.add(FileUtil.normalizeFile(file).getAbsolutePath());
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
