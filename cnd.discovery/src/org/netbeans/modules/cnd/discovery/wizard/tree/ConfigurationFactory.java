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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;

/**
 *
 * @author Alexander Simon
 */
public class ConfigurationFactory {
    
    private ConfigurationFactory() {
    }
    
    public static ProjectConfigurationImpl makeRoot(ProjectProperties project, String rootFolder){
        Collection<FolderProperties> folders = project.getConfiguredFolders();
        FolderConfigurationImpl root = new FolderConfigurationImpl("");
        for(FolderProperties folder : folders){
            //if (folder.getItemPath().startsWith(rootFolder)) {
                FolderConfigurationImpl fo = addChild(folder.getItemPath(), root);
                for(SourceFileProperties file : folder.getFiles()){
                    FileConfigurationImpl fi = new FileConfigurationImpl(file);
                    fo.addFile(fi);
                }
            //} else {
                //System.out.println("Out of scope "+folder);
            //}
        }
        // remove empty root
        StringTokenizer st = new StringTokenizer(rootFolder,"/\\"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (st.hasMoreTokens()){
            list.add(st.nextToken());
        }
        while (true){
            FolderConfigurationImpl r = root.cut();
            if (r == null) {
                break;
            }
            root = r;
            String name = r.getFolderName();
            if (list.size()>0 && list.get(list.size()-1).equals(name)){
                break;
            }
        }
        return new ProjectConfigurationImpl(project, root);
    }
    
    private static FolderConfigurationImpl addChild(String child, FolderConfigurationImpl folder){
        FolderConfigurationImpl current = folder;
        StringTokenizer st = new StringTokenizer(child,"/\\"); // NOI18N
        StringBuilder currentName = new StringBuilder();
        boolean first = true;
        while(st.hasMoreTokens()){
            String segment = st.nextToken();
            if (!first || child.startsWith("/")) { // NOI18N
                currentName.append("/"); // NOI18N
            }
            first = false;
            currentName.append(segment);
            FolderConfigurationImpl found = current.getChild(segment);
            if (found == null) {
                found = new FolderConfigurationImpl(currentName.toString());
                current.addChild(found);
            }
            current = found;
        }
        return current;
    }
    
    public static void consolidateProject(ProjectConfigurationImpl project){
        FolderConfigurationImpl root = (FolderConfigurationImpl)project.getRoot();
        Set<String> userIncludes = new LinkedHashSet<String>();
        Map<String,String> userMacros = new HashMap<String,String>();
        consolidateProject(root, userIncludes, userMacros);
        root.setOverrideIncludes(false);
        root.setOverrideMacros(false);
        root.setUserInludePaths(null);
        root.setUserMacros(null);
        project.setUserInludePaths(userIncludes);
        project.setUserMacros(userMacros);
    }
    
    private static void consolidateProject(FolderConfigurationImpl folder, Set<String> userIncludes, Map<String,String> userMacros){
        for(FolderConfiguration f : folder.getFolders()){
            FolderConfigurationImpl sub = (FolderConfigurationImpl)f;
            consolidateProject(sub, userIncludes, userMacros);
            sub.setOverrideIncludes(false);
            sub.setOverrideMacros(false);
            sub.setUserInludePaths(null);
            sub.setUserMacros(null);
        }
        for(FileConfiguration f : folder.getFiles()){
            FileConfigurationImpl file =((FileConfigurationImpl)f);
            userIncludes.addAll(file.getUserInludePaths());
            userMacros.putAll(file.getUserMacros());
            file.setOverrideIncludes(false);
            file.setOverrideMacros(false);
            file.setUserInludePaths(null);
            file.setUserMacros(null);
        }
    }
    
    public static void consolidateFolder(ProjectConfigurationImpl project){
        FolderConfigurationImpl root = (FolderConfigurationImpl)project.getRoot();
        consolidateFolder(root);
        project.setUserInludePaths(null);
        project.setUserMacros(null);
    }
    
    private static void consolidateFolder(FolderConfigurationImpl folder){
        for(FolderConfiguration f : folder.getFolders()){
            FolderConfigurationImpl sub = (FolderConfigurationImpl)f;
            consolidateFolder(sub);
        }
        Set<String> userIncludes = new LinkedHashSet<String>();
        Map<String,String> userMacros = new HashMap<String,String>();
        boolean hasFiles = false;
        for(FileConfiguration f : folder.getFiles()){
            hasFiles = true;
            FileConfigurationImpl file =((FileConfigurationImpl)f);
            userIncludes.addAll(file.getUserInludePaths());
            userMacros.putAll(file.getUserMacros());
            file.setOverrideIncludes(false);
            file.setOverrideMacros(false);
            file.setUserInludePaths(null);
            file.setUserMacros(null);
        }
        if (hasFiles){
            folder.setOverrideIncludes(true);
            folder.setOverrideMacros(true);
            folder.setUserInludePaths(userIncludes);
            folder.setUserMacros(userMacros);
        } else {
            folder.setOverrideIncludes(false);
            folder.setOverrideMacros(false);
            folder.setUserInludePaths(null);
            folder.setUserMacros(null);
        }
    }
    
    public static void consolidateFile(ProjectConfigurationImpl project){
        FolderConfigurationImpl root = (FolderConfigurationImpl)project.getRoot();
        consolidateFile(root);
        project.setUserInludePaths(null);
        project.setUserMacros(null);
    }
    
    private static void consolidateFile(FolderConfigurationImpl folder){
        for(FolderConfiguration f : folder.getFolders()){
            FolderConfigurationImpl sub = (FolderConfigurationImpl)f;
            consolidateFile(sub);
        }
        for(FileConfiguration f : folder.getFiles()){
            FileConfigurationImpl file =((FileConfigurationImpl)f);
            file.setOverrideIncludes(true);
            file.setOverrideMacros(true);
            file.setUserInludePaths(file.getUserInludePaths());
            file.setUserMacros(file.getUserMacros());
        }
        folder.setOverrideIncludes(false);
        folder.setOverrideMacros(false);
        folder.setUserInludePaths(null);
        folder.setUserMacros(null);
    }
}
