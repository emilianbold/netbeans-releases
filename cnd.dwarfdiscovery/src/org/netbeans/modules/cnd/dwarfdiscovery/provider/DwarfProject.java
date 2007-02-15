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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DwarfProject implements ProjectProperties {
    private static boolean gatherFolders = true;
    private static boolean isLogicalStructure = false;
    
    private ItemProperties.LanguageKind language;
    private Set<String> userIncludes = new LinkedHashSet<String>();
    private Set<String> systemIncludes = new LinkedHashSet<String>();
    private Map<String,String> userMacros = new HashMap<String,String>();
    private Map<String,FolderProperties> folders = new HashMap<String,FolderProperties>();
    
    /** Creates a new instance of DwarfProject */
    public DwarfProject(ItemProperties.LanguageKind language) {
        this.language = language;
    }
    
    void update(SourceFileProperties source){
        userIncludes.addAll(source.getUserInludePaths());
        for (String path : source.getUserInludePaths()) {
            if (!path.startsWith(File.separator)){
                if (path.equals(".")) { // NOI18N
                    userIncludes.add(source.getCompilePath());
                } else {
                    try {
                        path = (new File(source.getCompilePath()+File.separator+path)).getCanonicalPath();
                        if (Utilities.isWindows()) {
                            path = path.replace('\\', '/');
                        }
                        userIncludes.add(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        userMacros.putAll(source.getUserMacros());
        if (gatherFolders) {
            updateFolder(source);
        }
    }
    
    private void updateFolder(SourceFileProperties source){
        //String path = source.getItemPath();
        //if (path.lastIndexOf('/')>0){
        //    path = path.substring(0,path.lastIndexOf('/'));
        //}
        String path = null;
        if (isLogicalStructure) {
            path = source.getCompilePath();
        } else {
            File file = new File(source.getItemPath());
            if (file.exists()) {
                try {
                    path = file.getParentFile().getCanonicalPath();
                    if (Utilities.isWindows()) {
                        path = path.replace('\\', '/');
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (path == null) {
                path = source.getItemPath();
                if (path.lastIndexOf('/')>0){
                    path = path.substring(0,path.lastIndexOf('/'));
                }
            }
        }
        
        FolderProperties folder = folders.get(path);
        if (folder == null) {
            folders.put(path,new DwarfFolder(path,source));
        } else {
            ((DwarfFolder)folder).update(source);
        }
    }
    
    public List<FolderProperties> getConfiguredFolders(){
        return new ArrayList<FolderProperties>(folders.values());
    }
    
    public String getMakePath() {
        return null;
    }
    
    public String getBinaryPath() {
        return null;
    }
    
    public ProjectProperties.BinaryKind getBinaryKind() {
        return null;
    }
    
    public List<String> getUserInludePaths() {
        return new ArrayList<String>(userIncludes);
    }
    
    public List<String> getSystemInludePaths() {
        return new ArrayList<String>(systemIncludes);
    }
    
    public Map<String, String> getUserMacros() {
        return userMacros;
    }
    
    public Map<String, String> getSystemMacros() {
        return null;
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }
}
