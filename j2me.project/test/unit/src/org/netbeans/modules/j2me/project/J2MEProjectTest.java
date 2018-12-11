 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2me.project.api.J2MEProjectBuilder;
import org.netbeans.modules.j2me.project.api.PropertyEvaluatorProvider;
import org.netbeans.modules.j2me.project.api.UpdateHelperProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.test.MockLookup;

/**
 *
 */
public class J2MEProjectTest extends NbTestCase {

    private File projectFolder;

    public J2MEProjectTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File workDir = getWorkDir();
        MockLookup.setLayersAndInstances(new TestUtils.MockJavaPlatformProvider(
             new File(workDir, "j2me")));
        
        projectFolder = new File (workDir, "testProject");  //NOI18N
        projectFolder.mkdirs();
        assertTrue(projectFolder.isDirectory());
    }



    @Test
    public void testFriendAPI() throws IOException {
        final JavaPlatform mePlatform = TestUtils.findMEPlatform();
        assertNotNull("No ME Platform", mePlatform);    //NOI18N
        final JavaPlatform sePlatform = TestUtils.findSEPlatfrom();
        assertNotNull("No SE Platform", sePlatform);    //NOI18N
        final AntProjectHelper aph = J2MEProjectBuilder.forDirectory(
                projectFolder,
                "Test Project",     //NOI18N
                mePlatform).
            addDefaultSourceRoots().
            setSDKPlatform(sePlatform).
            build();
        assertNotNull(aph);
        final Project project = FileOwnerQuery.getOwner(aph.getProjectDirectory());
        assertNotNull(project);
        final J2MEProject j2meProject = project.getLookup().lookup(J2MEProject.class);
        assertNotNull(j2meProject);
        final UpdateHelperProvider uhp = project.getLookup().lookup(UpdateHelperProvider.class);
        assertNotNull(uhp);
        assertTrue(j2meProject.getUpdateHelper() == uhp.getUpdateHelper());
        final PropertyEvaluatorProvider pep = project.getLookup().lookup(PropertyEvaluatorProvider.class);
        assertNotNull(pep);
        assertTrue(j2meProject.evaluator() == pep.getPropertyEvaluator());
    }

}
