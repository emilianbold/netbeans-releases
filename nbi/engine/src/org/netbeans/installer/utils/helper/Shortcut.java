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
package org.netbeans.installer.utils.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Shortcut {
    private Map<Locale, String> names = new HashMap<Locale, String>();
    private Map<Locale, String> descriptions = new HashMap<Locale, String>();
    
    private String relativePath;
    private String fileName;
    
    private File executable;
    private File workingDirectory;
    private List<String> arguments = new ArrayList<String>();
    
    private File icon;
    
    private String[] categories;
    
    private String path;
    
    public Shortcut(final String name, final File executable) {
        this.names.put(Locale.getDefault(), name);
        
        this.executable = executable;
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
    
    public File getExecutable() {
        return executable;
    }
    
    public String getExecutablePath() {
        return executable.getAbsolutePath();
    }
    
    public void setExecutable(final File executable) {
        this.executable = executable;
    }
    
    public List<String> getArguments() {
        return arguments;
    }
    
    public String getArgumentsString() {
        if (arguments.size() == 0) {
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < arguments.size(); i++) {
                builder.append(arguments.get(i));
                
                if (i != arguments.size() - 1) {
                    builder.append(" ");
                }
            }
            
            return builder.toString();
        }  else {
            return null;
        }
    }
    
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }
    
    public void addArgument(final String argument) {
        arguments.add(argument);
    }
    
    public void removeArgument(final String argument) {
        arguments.remove(argument);
    }
    
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
}