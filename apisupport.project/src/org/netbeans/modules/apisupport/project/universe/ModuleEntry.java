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
     * @return e.g. .../nbbuild/netbeans/ide6
     */
    File getClusterDirectory();
    
    /**
     * Get the module JAR file.
     * @return e.g. .../nbbuild/netbeans/ide6/modules/org-netbeans-modules-java-project.jar
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
     * @return array of public packages. May be empty but not <code>null</code>.
     */
    ManifestManager.PackageExport[] getPublicPackages();
    
    /**
     * Get a set of names of all <em>nonempty</em> packages this module
     * contains.
     */
    Set<String> getAllPackageNames();
    
    /**
     * Get array of friends of this module.
     */
    boolean isDeclaredAsFriend(String cnb);
    
    /**
     * Get a set of class names defined in this module's public packages.
     */
    Set<String> getPublicClassNames();
    
    /**
     * Check whether this module is marked as deprecated.
     */
    boolean isDeprecated();

    /**
     * Get a list of code name bases for modules which this module has (runtime) dependencies on.
     */
    String[] getRunDependencies();

}
