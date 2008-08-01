/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.bluej.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.options.BlueJSettings;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class ClassPathProviderImpl implements ClassPathProvider {

    private BluejProject project;
    
    private ClassPath boot;
    private ClassPath source;
    private ClassPath compile;
    private ClassPath[] boots;
    private ClassPath[] sources;
    private ClassPath[] compiles;
    
    private CPImpl cpimpl;
    
    
    /** Creates a new instance of ClassPathProviderImpl */
    public ClassPathProviderImpl(BluejProject prj) {
        project = prj;
    }
    
    public CPImpl getBluejCPImpl() {
        return cpimpl;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else {
            return null;
        }
    }

    private ClassPath getBootClassPath() {
        if (boot == null) {
            boot = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        return boot;
    }

    private ClassPath getSourcepath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        if (source == null) {
            source = ClassPathSupport.createClassPath(new FileObject[] { project.getProjectDirectory() });
        }
        return source;
    }

    private ClassPath getRunTimeClasspath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        return null;
    }

    private ClassPath getCompileTimeClasspath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        if (compile == null) {
            // do we need ant cp as it is?
                ClassPath antcp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.getAntProjectHelper().getStandardPropertyEvaluator(), 
                new String[] {"javac.classpath"}));  // NOI18N
            cpimpl = new CPImpl(project);
            ClassPath bluejcp = ClassPathFactory.createClassPath(cpimpl);
            compile = ClassPathSupport.createProxyClassPath( new ClassPath[] {antcp, bluejcp} );
        } 
        return compile;
    }
    
    public ClassPath[] getCompileTimeClasspath() {
        if (compiles == null) {
            compiles = new ClassPath[] { getCompileTimeClasspath(project.getProjectDirectory()),
                                         //make source path, becuase it's equal with the built output path..
                                         ClassPathSupport.createClassPath(new FileObject[] { project.getProjectDirectory() })};
        }
        return compiles;
    }
    
    public ClassPath[] getSourcePath() {
        if (sources == null) {
            sources = new ClassPath[] { getSourcepath(project.getProjectDirectory()) };
        }
        return sources;
    }
    
    public ClassPath[] getBootPath() {
        if (boots == null) {
            boots = new ClassPath[] { getBootClassPath() };
        }
        return boots;
    }
    
}
