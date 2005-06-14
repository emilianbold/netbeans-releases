/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.apisupport.project.ManifestManager;

/**
 * One known module.
 * Falls into four categories:
 * <ol>
 * <li>Modules inside netbeans.org (with source).
 * <li>Modules with source in an external suite.
 * <li>Standalone external modules with source.
 * <li>JARs from a NB binary. May or may not have an associated source dir.
 * </ol>
 */
public interface ModuleEntry extends Comparable {
    
    /**
     * Get a relative source path inside netbeans.org CVS.
     * Note that if the entry is from netbeans.org CVS yet was scanned as a
     * side effect of loading an external module that defined netbeans.dest.dir,
     * this will be null - such secondary entries are essentially based on the
     * actual JAR, only adding in a non-null {@link #getSourceLocation}.
     * @return e.g. java/project, or null for external modules
     */
    String getNetBeansOrgPath();
    
    /**
     * Get the source location of this module, if there is one.
     */
    File getSourceLocation();
    
    /**
     * Get a code name base.
     * @return e.g. org.netbeans.modules.java.project
     */
    String getCodeNameBase();
    
    /**
     * Get the directory to which the module is built.
     * @return e.g. .../nbbuild/netbeans/ide5
     */
    File getClusterDirectory();
    
    /**
     * Get the module JAR file.
     * @return e.g. .../nbbuild/netbeans/ide5/modules/org-netbeans-modules-java-project.jar
     */
    File getJarLocation();
    
    /**
     * Get any added class path entries, as a path suffix (may be empty).
     */
    String getClassPathExtensions();
    
    /**
     * Path which resolves to the root of the NetBeans binary installation used for this module.
     */
    File getDestDir();
    
    /**
     * Returns either the module release version or <code>null</code> if
     * there isn't any.
     */
    String getReleaseVersion();
    
    /**
     * Returns either the module specification version or <code>null</code>
     * if there isn't any.
     */
    String getSpecificationVersion();
    
    /**
     * Returns array of provided tokens by the module. Can be empty.
     */
    String[] getProvidedTokens();
    
    /**
     * Get localized name of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up. If the
     * localized name is not found <code>getCodeNameBase()</code> is
     * returned.
     */
    String getLocalizedName();
    
    /**
     * Get category of this module. Implementations should use lazy-loading
     * from localizing bundle to keep performance up.
     */
    String getCategory();
    
    /**
     * Get long description of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up.
     */
    String getLongDescription();
    
    /**
     * Get short description of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up.
     */
    String getShortDescription();
    
    /**
     * Get array of public packages exported by this module entry.
     */
    ManifestManager.PackageExport[] getPublicPackages();
    
    /**
     * Get a set of class names defined in this module's public packages.
     */
    Set/*<String>*/ getPublicClassNames();
    
    /**
     * Check whether this module is marked as deprecated.
     */
    boolean isDeprecated();
    
}
