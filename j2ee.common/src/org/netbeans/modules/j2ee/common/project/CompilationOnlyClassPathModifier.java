/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.common.project;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;

/**
 * Extended classpath modifier which adds classpath items to project classpath
 * and at the same time it exclude such items from being packaged into
 * project's build artifact.
 *
 * @since org.netbeans.modules.j2ee.common/1 1.53
 * @deprecated use regular classpath modifier with classpath type
 *    JavaClassPathConstants.COMPILE_ONLY instead
 */
public interface CompilationOnlyClassPathModifier {
    
    /**
     * Adds libraries into the project's classpath if the libraries are not already included. These libraries
     * will not be included in the created archive file.
     * @param libraries to be added
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileLibraries(Library[] libraries) throws IOException;
    
    /**
     * Removes libraries from the project's classpath if the libraries were not already removed.
     * @param libraries to be removed
     * @return true in case the classpath was changed (at least one library was removed from the classpath),
     * the value false is returned when all the libraries were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileLibraries(Library[] libraries) throws IOException;
    
    /**
     * Adds artifacts (e.g. subprojects) into the project's classpath if the artifacts are not already included.
     * These artifacts will not be included in the created archive file.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements) throws IOException;
    
    /**
     * Removes artifacts (e.g. subprojects) from the project's classpath if the artifacts were not already removed.
     * @param artifacts to be removed
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed (at least one artifact was removed from the classpath),
     * the value false is returned when all the artifacts were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements) throws IOException;
    
    /**
     * Adds archive files or folders into the project's classpath if the archive files or folders are not already
     * included. These archive files or folders will not be included in the created archive file.
     * @param roots to be added
     * @return true in case the classpath was changed (at least one archive file or folder was added to the classpath),
     * the value false is returned when all the archive files or folders are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileRoots(URL[] roots) throws IOException;
    
    /**
     * Removes archive files or folders from the project's classpath if the archive files or folders were not already
     * removed.
     * @param roots to be removed
     * @return true in case the classpath was changed (at least one archive file or folder was removed from the classpath),
     * the value false is returned when all the archive files or folders were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileRoots(URL[] roots) throws IOException;
}
