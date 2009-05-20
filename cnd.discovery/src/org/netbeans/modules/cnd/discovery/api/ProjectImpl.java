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

package org.netbeans.modules.cnd.discovery.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Utilities;

/**
 * Default implementation of ProjectProperties.
 * Enough in most cases.
 * 
 * @author Alexander Simon
 */
public final class ProjectImpl implements ProjectProperties {
    private static boolean gatherFolders = true;
    
    private ItemProperties.LanguageKind language;
    private Set<String> userIncludes = new LinkedHashSet<String>();
    private Set<String> systemIncludes = new LinkedHashSet<String>();
    private Map<String,String> userMacros = new HashMap<String,String>();
    private Map<String,FolderProperties> folders = new HashMap<String,FolderProperties>();
    
    /** Creates a new instance of DwarfProject */
    public ProjectImpl(ItemProperties.LanguageKind language) {
        this.language = language;
    }
    
    /**
     * Adds source file in the project
     * @param sources source file
     */
    public void update(SourceFileProperties source){
        userIncludes.addAll(source.getUserInludePaths());
        for (String path : source.getUserInludePaths()) {
            userIncludes.add(DiscoveryUtils.convertRelativePathToAbsolute(source,path));
        }
        userMacros.putAll(source.getUserMacros());
        if (gatherFolders) {
            updateFolder(source);
        }
    }

    /**
     * Helper method that divides list of source properties by language.
     * @param sources list of source files
     * @return list of language projects
     */
    public static List<ProjectProperties> divideByLanguage(List<SourceFileProperties> sources) {
        ProjectImpl cProp = null;
        ProjectImpl cppProp = null;
        for (SourceFileProperties source : sources) {
            ItemProperties.LanguageKind lang = source.getLanguageKind();
            ProjectImpl current = null;
            if (lang == ItemProperties.LanguageKind.C) {
                if (cProp == null) {
                    cProp = new ProjectImpl(lang);
                }
                current = cProp;
            } else {
                if (cppProp == null) {
                    cppProp = new ProjectImpl(lang);
                }
                current = cppProp;
            }
            current.update(source);
        }
        List<ProjectProperties> languages = new ArrayList<ProjectProperties>();
        if (cProp != null) {
            languages.add(cProp);
        }
        if (cppProp != null) {
            languages.add(cppProp);
        }
        return languages;
    }
   
    private void updateFolder(SourceFileProperties source){
        File file = new File(source.getItemPath());
        String path = CndFileUtils.normalizeFile(file.getParentFile()).getAbsolutePath();
        // folders should use unix style
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        FolderProperties folder = folders.get(path);
        if (folder == null) {
            folders.put(path,new FolderImpl(path,source));
        } else {
            ((FolderImpl)folder).update(source);
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
