/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.lib.registries;

import java.io.File;
import java.util.Properties;
import org.netbeans.installer.utils.helper.Platform;

public interface RegistriesManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ENGINE_JAR = "engine.jar";
    
    public static final String REGISTRY_XML = "registry.xml";
    public static final String BUNDLES_LIST = "bundles.list";
    
    public static final String BUNDLES = "bundles";
    
    public static final String COMPONENTS = "components";
    public static final String PRODUCTS = COMPONENTS + "/products";
    public static final String GROUPS = COMPONENTS + "/groups";
    
    public static final String TEMP = "temp";
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine(
            final File root) throws ManagerException;
    
    void updateEngine(
            final File root,
            final File archive) throws ManagerException;
    
    // components operations ////////////////////////////////////////////////////////
    void addPackage(
            final File root,
            final File archive,
            final String parentUid,
            final String parentVersion,
            final String parentPlatforms) throws ManagerException;
    
    void removeProduct(
            final File root,
            final String uid,
            final String version,
            final String platforms) throws ManagerException;
    
    void removeGroup(
            final File root,
            final String uid) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components,
            final Properties props,
            final Properties bundleProps) throws ManagerException;
    
    void deleteBundles(
            final File root) throws ManagerException;
    
    void generateBundles(
            final File root) throws ManagerException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    void initializeRegistry(
            final File root) throws ManagerException;
    
    File exportRegistry(
            final File root,
            final File destination,
            final String codebase) throws ManagerException;
    
    String generateComponentsJs(
            final File root) throws ManagerException;
}