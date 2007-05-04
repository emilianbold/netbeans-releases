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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils.system.shortcut;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class Shortcut {
    private Map<Locale, String> names;
    private Map<Locale, String> descriptions;
    
    private String relativePath;
    private String fileName;
    
    
    private File workingDirectory;
    private File icon;
    private int iconIndex;
    
    private String[] categories;
    
    private String path;
    
   
    protected Shortcut(final String name) {
        setNames(new HashMap<Locale, String>());
        setDescriptions(new HashMap<Locale, String>());
        setName(name);
        setCategories(new String [] {});        
    }
    public Map<Locale, String> getNames() {
        return names;
    }
    
    public void setNames(final Map<Locale, String> names) {
        this.names = names;
    }
    
    public String getName() {
        return getName(Locale.getDefault());
    }
    
    public void setName(final String name) {
        setName(name, Locale.getDefault());
    }
    
    public String getName(final Locale locale) {
        return names.get(locale);
    }
    
    public void setName(final String name, final Locale locale) {
        if (name != null) {
            names.put(locale, name);
        } else {
            names.remove(locale);
        }
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public void setDescriptions(final Map<Locale, String> comments) {
        this.descriptions = comments;
    }
    
    public String getDescription() {
        return descriptions.get(Locale.getDefault());
    }
    
    public void setDescription(final String description) {
        setDescription(description, Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return descriptions.get(locale);
    }
    
    public void setDescription(final String description, final Locale locale) {
        descriptions.put(locale, description);
    }
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public void setRelativePath(final String relativePath) {
        this.relativePath = relativePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }    
    public abstract String getTargetPath();
    
    public File getWorkingDirectory() {
        return workingDirectory;
    }
    
    public String getWorkingDirectoryPath() {
        if (workingDirectory != null) {
            return workingDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    public File getIcon() {
        return icon;
    }
    
    public String getIconPath() {
        if (icon != null) {
            return icon.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    public void setIcon(final File icon) {
        this.icon = icon;
    }
    
    public void setIcon(final File icon, int index) {
        setIcon(icon);
        setIconIndex(index);
    }
    
    
    public String[] getCategories() {
        return categories;
    }
    
    public void setCategories(final String[] categories) {
        this.categories = categories;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(final String path) {
        this.path = path;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
}