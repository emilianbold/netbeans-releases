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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.FolderProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;

/**
 *
 * @author Alexander Simon
 */
public class DwarfFolder implements FolderProperties {
    private String path;
    private ItemProperties.LanguageKind language;
    private Set<String> userIncludes = new LinkedHashSet<String>();
    private Set<String> systemIncludes = new LinkedHashSet<String>();
    private Map<String, String> userMacros = new HashMap<String,String>();
    private List<SourceFileProperties> files = new ArrayList<SourceFileProperties>();
    
    public DwarfFolder(String path, SourceFileProperties source) {
        this.path = path;
        this.language = source.getLanguageKind();
        update(source);
    }

    void update(SourceFileProperties source){
        files.add(source);
        userIncludes.addAll(source.getUserInludePaths());
        for (String currentPath : source.getUserInludePaths()) {
            userIncludes.add(DwarfSource.convertRelativePathToAbsolute(source,currentPath));
        }
        systemIncludes.addAll(source.getSystemInludePaths());
        userMacros.putAll(source.getUserMacros());
    }
    
    public String getItemPath() {
        return path;
    }
    
    public List<SourceFileProperties> getFiles() {
        return files;
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
