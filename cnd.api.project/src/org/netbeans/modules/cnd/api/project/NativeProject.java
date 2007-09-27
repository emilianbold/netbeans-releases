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
package org.netbeans.modules.cnd.api.project;

import java.io.File;
import java.util.List;

public interface NativeProject {
    /** 
     * Returns project object, 
     * i.e. Netbeans project object
     * org.netbeans.api.project.Project
     */
    public Object getProject();
    
     /**
     * Returns file path to project root
     * @return file path to project root
     */
    public String getProjectRoot();

    /**
     * Returns the display name of the project
     * @return display name of the project
     */
    public String getProjectDisplayName();

    /**
      * Returns a list of all source files in the project.
      * @return a list of all source files in the project. A source
      * file is a file directly compiled by the project.
      */
     public List<NativeFileItem> getAllSourceFiles();

     /**
      * Returns a list of all header files in the project.
      * @return a list of all header files in the project. A header
      * file is a file with the suffix .h.
      */
     public List<NativeFileItem> getAllHeaderFiles();

     /**
      * Adds a listener to changes when items are added to or removed from the project.
      * @param listener a listener to add
      */
     public void addProjectItemsListener(NativeProjectItemsListener listener);

     /**
      * Removes a listner.
      * @param listener a listener to remove
      */
     public void removeProjectItemsListener(NativeProjectItemsListener listener);
     
     /**
      * Finds a file item in the project.
      * @param file the file item to find
      * @return the file item if found. Othervise it returns null.
      */
     public NativeFileItem findFileItem(File file);
     
    /**
     * Returns a list <String> of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<String> getSystemIncludePaths();
    
    /**
     * Returns a list <String> of user defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<String> getUserIncludePaths();
    
    /**
     * Returns a list <String> of compiler defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getSystemMacroDefinitions();
    
    /**
     * Returns a list <String> of user defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getUserMacroDefinitions();
    
    /**
     * Returns a list <NativeProject> of libriries.
     * @return a list <NativeProject> of libriries.
     */
    public List<NativeProject> getDependences();
}
