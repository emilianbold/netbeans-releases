/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.classpath;

import com.sun.org.apache.xerces.internal.util.URI;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class ModuleClassPathsTest extends NbTestCase {
    
    private SourceRoots src;
    private ClassPath systemModules;
    
    public ModuleClassPathsTest(@NonNull final String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject prjDir = FileUtil.createFolder(workDir, "TestProject");    //NOI18N
        assertNotNull(prjDir);
        final FileObject srcDir = FileUtil.createFolder(prjDir, "src");    //NOI18N
        assertNotNull(srcDir);
        final Project prj = TestProject.createProject(prjDir, srcDir, null);
        assertNotNull(prj);
        src = Optional.ofNullable(prj.getLookup().lookup(TestProject.class))
                .map((p) -> p.getSourceRoots())
                .orElse(null);
        systemModules = Optional.ofNullable(TestUtilities.getJava9Home())
                .map((f)-> {
                    try {
                        return org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(BaseUtilities.toURI(f).toURL());
                    } catch (MalformedURLException e) {
                        return null;
                    }})
                .orElse(null);
    }
    
   
    public void testModuleInfoBasedCp() {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
    }
}
