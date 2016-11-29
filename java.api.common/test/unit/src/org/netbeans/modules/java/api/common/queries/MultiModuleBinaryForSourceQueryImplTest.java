/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.queries;

import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import java.util.Arrays;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleBinaryForSourceQueryImplTest extends NbTestCase {
    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleBinaryForSourceQueryImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src1 = wd.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        src2 = wd.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        final Project prj = TestProject.createProject(wd, null, null);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
    }

    public void testQuery() {
        assertTrue(mtu.updateModuleRoots(src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);

        final MultiModuleBinaryForSourceQueryImpl q = new MultiModuleBinaryForSourceQueryImpl(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                model,
                testModel,
                new String[]{
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_CLASSES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[]{});

        assertNull(q.findBinaryRoots(src1.toURL()));
        assertNull(q.findBinaryRoots(src2.toURL()));

        BinaryForSourceQuery.Result r = q.findBinaryRoots(mod1a.toURL());
        assertNotNull(r);
        System.out.println(Arrays.toString(r.getRoots()));
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1a.getParent().getNameExt()),
                    mtu.distFor(mod1a.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod1b.toURL());
        assertNotNull(r);
        System.out.println(Arrays.toString(r.getRoots()));
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1b.getParent().getNameExt()),
                    mtu.distFor(mod1b.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod2c.toURL());
        assertNotNull(r);
        System.out.println(Arrays.toString(r.getRoots()));
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2c.getParent().getNameExt()),
                    mtu.distFor(mod2c.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod1d.toURL());
        assertNotNull(r);
        System.out.println(Arrays.toString(r.getRoots()));
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod1d.getParent().getNameExt()),
                    mtu.distFor(mod1d.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));

        r = q.findBinaryRoots(mod2d.toURL());
        assertNotNull(r);
        System.out.println(Arrays.toString(r.getRoots()));
        assertEquals(
                Arrays.asList(
                    mtu.buildFor(mod2d.getParent().getNameExt()),
                    mtu.distFor(mod2d.getParent().getNameExt())
                ),
                Arrays.asList(r.getRoots()));
    }
}
