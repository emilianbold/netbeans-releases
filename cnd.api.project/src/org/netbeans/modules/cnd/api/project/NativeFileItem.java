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

import java.util.List;
import java.io.File;

public interface NativeFileItem {
    public enum Language {
	C, CPP, FORTRAN, C_HEADER, OTHER
    }
    
    public enum LanguageFlavor {
    	GENERIC,
        SUN_C, GNU_C,
        SUN_CPP, GNU_CPP,
        SUN_FORTRAN_77, SUN_FORTRAN_90, SUN_FORTRAN_95, GNU_FORTRAN
    }
    
    /**
     * Returns the native project this file item belongs to.
     * @return the native project
     */
    public NativeProject getNativeProject();

    /**
     * Returns the file associated with this file item.
     * @return the file associated with this file item. There is no guarantee that the file actually exists.
     */
    public File getFile();

    /**
     * Returns a list <String> of compiler defined include paths used when compiling this file item.
     * @return a list <String> of compiler defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<String> getSystemIncludePaths();
    
    /**
     * Returns a list <String> of user defined include paths used when compiling this file item.
     * @return a list <String> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<String> getUserIncludePaths();
    
    /**
     * Returns a list <String> of compiler defined macro definitions used when compiling this file item.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getSystemMacroDefinitions();
    
    /**
     * Returns a list <String> of user defined macro definitions used when compiling this file item.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getUserMacroDefinitions();
    
    /**
     * Returns the language of the file. 
     * @return the language of the file
     */
    public Language getLanguage();
    
    /**
     * Returns the language flavor of the file or GENERIC if unknown.
     * @return the language flavor (or GENERIC) of the file
     */
    public LanguageFlavor getLanguageFlavor();
    
    /**
     * Returns true if file excluded from build.
     * @return true if file excluded from build.
     */
    public boolean isExcluded();
}
