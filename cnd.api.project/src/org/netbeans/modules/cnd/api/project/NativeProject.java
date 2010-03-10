/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.api.project;

import java.io.File;
import java.io.IOException;
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
     * Returns file paths to source roots
     * @return file paths to source roots
     */
    public List<String> getSourceRoots();

    /**
     * Returns the display name of the project
     * @return display name of the project
     */
    public String getProjectDisplayName();

//    /**
//      * Returns a list of all source files in the project.
//      * @return a list of all source files in the project. A source
//      * file is a file directly compiled by the project.
//      */
//     public List<NativeFileItem> getAllSourceFiles();
//
//     /**
//      * Returns a list of all header files in the project.
//      * @return a list of all header files in the project. A header
//      * file is a file with the suffix .h.
//      */
//     public List<NativeFileItem> getAllHeaderFiles();

     /**
      * Returns a list of all files in the project.
      * @return a list of all files in the project.
      */
     public List<NativeFileItem> getAllFiles();

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
    
    /**
     * Add task which will be run then <NativeProject> is ready to provide Code Model data
     */
    public void runOnCodeModelReadiness(Runnable task);

    /**
     * Execute a command from user's PATH in the context of the native project
     * @param executable Executable name (not path)
     * @param env Additional environment variables
     * @param args Arguments
     * @return NativeExitStatus
     */
    public NativeExitStatus execute(final String executable, final String[] env, final String... args) throws IOException;

    /**
     * Return the name of the development platform (Solaris-x86, Solaris-sparc, MacOSX, Windows, Linux-x86)
     * @return development platform name
     */
    public String getPlatformName();
}
