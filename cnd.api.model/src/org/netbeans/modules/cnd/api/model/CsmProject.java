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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;

/**
 *
 * @author Vladimir Kvashin
 */
public interface CsmProject extends CsmNamedElement, CsmValidable {

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
    CsmNamespace findNamespace( CharSequence qualifiedName );
    
    /**
     * Finds compound classifier (clas or enum) by its qualified name
     */
    CsmClassifier findClassifier(CharSequence qualifiedName);

    /**
     * Finds all compound classifier (clas, struct, union, enum, typedef, classforward) by its qualified name
     */
    Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName);
    
    /**
     * Finds declaration by its nuique name
     */
    CsmDeclaration findDeclaration(CharSequence uniqueName);
    
    /**
     * Finds declarations by its nuique name
     */
    Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName);

    /**
     * Finds file by object that can be absolute path or native file item
     */
    public abstract CsmFile findFile(Object absolutePathOrNativeFileItem, boolean snapShot);

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

}
