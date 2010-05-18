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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
            FolderConfigurationImpl fo = addChild(folder.getItemPath(), root);
            for (SourceFileProperties file : folder.getFiles()) {
                FileConfigurationImpl fi = new FileConfigurationImpl(file);
                fo.addFile(fi);
            }
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
        project.setUserInludePaths(root.getUserInludePaths(false));
        project.setUserMacros(root.getUserMacros(false));
    }
    
    private static void consolidateFile(FolderConfigurationImpl folder){
        Set<String> commonFoldersIncludes = new HashSet<String>();
        Map<String,String> commonFoldersMacros = new HashMap<String,String>();
        boolean haveSubFolders = false;
        for(FolderConfiguration f : folder.getFolders()){
            FolderConfigurationImpl subFolder = (FolderConfigurationImpl) f;
            consolidateFile(subFolder);
            if (!haveSubFolders) {
                commonFoldersIncludes.addAll(subFolder.getUserInludePaths(false));
                commonFoldersMacros.putAll(subFolder.getUserMacros(false));
                haveSubFolders = true;
            } else {
                if (commonFoldersIncludes.size() > 0) {
                    commonFoldersIncludes.retainAll(subFolder.getUserInludePaths(false));
                }
                if (commonFoldersMacros.size() > 0) {
                    Set<String> intersection = commonFoldersMacros.keySet();
                    intersection.retainAll(subFolder.getUserMacros(false).keySet());
                    Map<String,String> newcommonFoldersMacros = new HashMap<String,String>();
                    for(String key : intersection){
                        String value1 = commonFoldersMacros.get(key);
                        String value2 = subFolder.getUserMacros(false).get(key);
                        if (value1 == null && value2 == null || value1 != null && value1.equals(value2)){
                            newcommonFoldersMacros.put(key, value1);
                        }
                    }
                    commonFoldersMacros = newcommonFoldersMacros;
                }
            }
        }
        Set<String> commonFilesIncludes = new HashSet<String>();
        Map<String,String> commonFilesMacros = new HashMap<String,String>();
        boolean first = true;
        if (haveSubFolders) {
            commonFilesIncludes = new HashSet<String>(commonFoldersIncludes);
            commonFilesMacros = new HashMap<String,String>(commonFoldersMacros);
            first = false;
        }
        for(FileConfiguration f : folder.getFiles()){
            FileConfigurationImpl file =((FileConfigurationImpl)f);
            file.setOverrideIncludes(false);
            file.setOverrideMacros(false);
            if (first) {
                commonFilesIncludes.addAll(file.getUserInludePaths());
                commonFilesMacros.putAll(file.getUserMacros());
                first = false;
            } else {
                if (commonFilesIncludes.size() > 0) {
                    commonFilesIncludes.retainAll(file.getUserInludePaths());
                }
                if (commonFilesMacros.size() > 0) {
                    Set<String> intersection = commonFilesMacros.keySet();
                    intersection.retainAll(file.getUserMacros().keySet());
                    Map<String,String> newCommonMacros = new HashMap<String,String>();
                    for(String key : intersection){
                        String value1 = commonFilesMacros.get(key);
                        String value2 = file.getUserMacros().get(key);
                        if (value1 == null && value2 == null || value1 != null && value1.equals(value2)){
                            newCommonMacros.put(key, value1);
                        }
                    }
                    commonFilesMacros = newCommonMacros;
                }
            }
            file.setUserInludePaths(file.getUserInludePaths());
            file.setUserMacros(file.getUserMacros());
        }
        folder.setOverrideIncludes(false);
        folder.setOverrideMacros(false);
        if (commonFilesIncludes.size() > 0) {
            folder.setUserInludePaths(commonFilesIncludes);
        } else {
            folder.setUserInludePaths(null);
        }
        if (commonFilesMacros.size() > 0) {
            folder.setUserMacros(commonFilesMacros);
        } else {
            folder.setUserMacros(null);
        }
    }
}
