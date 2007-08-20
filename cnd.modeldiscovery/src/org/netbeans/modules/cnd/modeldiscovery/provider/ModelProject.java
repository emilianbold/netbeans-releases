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

package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.File;
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
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ModelProject implements ProjectProperties {
    private static boolean gatherFolders = true;
    
    private ItemProperties.LanguageKind language;
    private Set<String> userIncludes = new LinkedHashSet<String>();
    private Set<String> systemIncludes = new LinkedHashSet<String>();
    private Map<String,String> userMacros = new HashMap<String,String>();
    private Map<String,FolderProperties> folders = new HashMap<String,FolderProperties>();
    
    public ModelProject(ItemProperties.LanguageKind language) {
        this.language = language;
    }
    
    void update(SourceFileProperties source){
        userIncludes.addAll(source.getUserInludePaths());
        for (String path : source.getUserInludePaths()) {
            userIncludes.add(ModelSource.convertRelativePathToAbsolute(source,path));
        }
        userMacros.putAll(source.getUserMacros());
        if (gatherFolders) {
            updateFolder(source);
        }
    }
    
    private void updateFolder(SourceFileProperties source){
        File file = new File(source.getItemPath());
        String path = FileUtil.normalizeFile(file.getParentFile()).getAbsolutePath();
        // folders should use unix style
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        FolderProperties folder = folders.get(path);
        if (folder == null) {
            folders.put(path,new ModelFolder(path,source));
        } else {
            ((ModelFolder)folder).update(source);
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

