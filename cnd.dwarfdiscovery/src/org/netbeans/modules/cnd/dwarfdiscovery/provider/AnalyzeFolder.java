/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
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
    
    public boolean canAnalyze(ProjectProxy project) {
        String root = (String)getProperty(FOLDER_KEY).getValue();
        if (root == null || root.length() == 0) {
            return false;
        }
        Set<String> set = getObjectFiles(root);
        if (set.size() == 0) {
            return false;
        }
        int i = 0;
        for(String obj : set){
            i++;
            if (sizeComilationUnit(obj) > 0) {
                return true;
            }
            if (i > 25) {
                return false;
            }
        }
        return false;
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
                        Set<String> set = getObjectFiles((String)getProperty(FOLDER_KEY).getValue());
                        if (set.size() > 0) {
                            myFileProperties = getSourceFileProperties(set.toArray(new String[set.size()]));
                        } else {
                            myFileProperties = new ArrayList<SourceFileProperties>();
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
    
    private Set<String> getObjectFiles(String root){
        HashSet<String> set = new HashSet<String>();
        gatherSubFolders(new File(root), set);
        HashSet<String> map = new HashSet<String>();
        for (Iterator it = set.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File d = new File((String)it.next());
            if (d.isDirectory()){
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
        if (isStoped) {
            return;
        }
        if (d.isDirectory()){
            String path = d.getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.endsWith("/SCCS") || path.endsWith("/CVS") || path.endsWith("/SunWS_cache")) { // NOI18N
                return;
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
