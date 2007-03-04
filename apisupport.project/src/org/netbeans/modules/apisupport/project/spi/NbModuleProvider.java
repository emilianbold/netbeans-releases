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
 */

package org.netbeans.modules.apisupport.project.spi;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 * Interface to be implemented by NetBeans module projects. 
 *
 * @see org.netbeans.api.project.Project#getLookup
 * @author Martin Krauskopf, Milos Kleint
 * @since org.netbeans.modules.apisupport.project 1.15
 */
public interface NbModuleProvider {

    // These are just for convenience:
    NbModuleType STANDALONE = NbModuleType.STANDALONE;
    NbModuleType SUITE_COMPONENT = NbModuleType.SUITE_COMPONENT;
    NbModuleType NETBEANS_ORG = NbModuleType.NETBEANS_ORG;
    
    /** Used for a type-safe enumeration of NetBeans module types. */
    enum NbModuleType {
        STANDALONE,
        SUITE_COMPONENT,
        NETBEANS_ORG
    }
    
    /** Returns type of this NetBeans module. 
     * @return STANDALONE SUITE_COMPONENT or NETBEANS_ORG
     */
    NbModuleType getModuleType();
    
    /**
     * Returns the specification version of the module
     * @return specification version of the module
     */ 
    String getSpecVersion();
    
    /**
     * Returns the codenamebase of the module
     * @return module's codenamebase 
     */ 
    String getCodeNameBase();
    
    
    /** Returns a relative path to a project's source directory. 
     * @return relative path to sources..
     */
    String getSourceDirectoryPath();
    /**
     * relative path to the directory which contains/is to contain resources, META-INF/services folder or layer file for example
     * @param inTests 
     * @return relative path from project root to resource root.
     */
    String getResourceDirectoryPath(boolean inTests);

    /**
     *  returns the relative path to the main project file (eg. nbproject/project.xml)
     * @return relative path from project root to the main project file.
     */
    String getProjectFilePath();
    
    /**
     * returns root directory with sources.
     * @return sources root FileObject
     */
    FileObject getSourceDirectory();
    
    /**
     * returns the location of the module's manifest
     * @return manifest FileObject.
     */ 
    FileObject getManifestFile();
    
    /**
     * add/updates the given dependency to the project
     */ 
    boolean addDependency(
            final String codeNameBase, final String releaseVersion,
            final SpecificationVersion version, final boolean useInCompiler) throws IOException;
    
    /**
     * checks the declared version of the given dependency
     * @param codenamebase 
     * @return 
     */ 
    SpecificationVersion getDependencyVersion(String codenamebase) throws IOException;
    
    /**
     * get the NetBeans platform for the module
     * @return location of the root directory of NetBeans platform installation
     */ 
    File getActivePlatformLocation();
    
    
}
