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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;

/**
 *
 * @author Vladimir Kvashin
 */
public interface CsmProject extends CsmObject, CsmIdentifiable<CsmProject>, CsmQualifiedNamedElement, CsmValidable {

    CsmNamespace getGlobalNamespace();
    
    /*
     * Waits until each file of the project is parsed.
     * If all files file are already parsed, immediately returns.
     */
    void waitParse();
    
    /** Gets an object, which represents correspondent IDE project */
    Object getPlatformProject();

    /**
     * Finds namespace by its qualified name
     *
     * TODO: what if different projects contain namespaces with equal FQN?
     * Now we assume that these namespaces are represented via different instances.
     * Probably this is not correct
     */
    CsmNamespace findNamespace( String qualifiedName );
    
    /**
     * Finds compound classifier (clas or enum) by its qualified name
     */
    CsmClassifier findClassifier(String qualifiedName);
    
    /**
     * Finds declaration by its nuique name
     */
    CsmDeclaration findDeclaration(String uniqueName);
    
    /**
     * Finds declarations by its nuique name
     */
    Collection<CsmOffsetableDeclaration> findDeclarations(String uniqueName);

    /**
     * Finds file by its absolute path
     */
    CsmFile findFile(String absolutePath);

    /**
     * Gets the collection of source project files.
     */
    Collection<CsmFile> getSourceFiles();
    
    /**
     * Gets the collection of heaher project files.
     */
    Collection<CsmFile> getHeaderFiles();
    
    /**
     * Gets the collection of all (source and heaher) project files.
     */
    Collection<CsmFile> getAllFiles();
    
    /**
     * Gets the collection of libraries of the project.
     * Library can be either other project (which this project depends on)
     * or just a set of system include files
     * (most likely, the latter kind of project would correspond with 
     * one include directory, so there would be as many libraries as include 
     * path components)
     */
    Collection<CsmProject> getLibraries();
    
    /**
     * Returns true if the project is completely parsed
     * @param skipFile if null => all project files are checked;
     * if param is not null => project is stable even if skipFile not parsed     
     */
    boolean isStable(CsmFile skipFile);

    /**
     * return true for auto created projects for included standard headers.
     */
    boolean isArtificial();

    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure, 
     * just use it as in unique identifier
     */
    String getUniqueName();
}
