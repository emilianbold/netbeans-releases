/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;

public class NbMavenProjectImplTest extends NbTestCase {

    public NbMavenProjectImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    public void testPackagingTypeSpecificLookup() throws Exception {
        assertLookupObject("[base, jar]", "jar");
        assertLookupObject("[base, war]", "war");
        assertLookupObject("[base]", "ear");
        // Now test dynamic changes to packaging:
        FileObject pd = FileUtil.toFileObject(getWorkDir()).getFileObject("prj-war");
        Project prj = ProjectManager.getDefault().findProject(pd);
        ((NbMavenProjectImpl) prj).attachUpdater();
        TestFileUtils.writeFile(pd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj-war</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        assertEquals("[base, jar]", prj.getLookup().lookup(I.class).m());
    }
    private void assertLookupObject(String result, String packaging) throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject pd = wd.createFolder("prj-" + packaging);
        TestFileUtils.writeFile(pd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj-" + packaging + "</artifactId>"
                + "<packaging>" + packaging + "</packaging><version>1.0</version></project>");
        assertEquals(result, ProjectManager.getDefault().findProject(pd).getLookup().lookup(I.class).m());
    }
    public interface I {
        String m();
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven")
    public static class BasePackagingImpl implements I {
        public @Override String m() {
            return "base";
        }
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven/jar")
    public static class JarPackagingImpl implements I {
        public @Override String m() {
            return "jar";
        }
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven/war")
    public static class WarPackagingImpl implements I {
        public @Override String m() {
            return "war";
        }
    }
    @LookupMerger.Registration(projectType="org-netbeans-modules-maven")
    public static class Merger implements LookupMerger<I> {
        public @Override Class<I> getMergeableClass() {
            return I.class;
        }
        public @Override I merge(final Lookup lookup) {
            return new I() {
                public @Override String m() {
                    Set<String> results = new TreeSet<String>();
                    for (I i : lookup.lookupAll(I.class)) {
                        results.add(i.m());
                    }
                    return results.toString();
                }
            };
        }
    }

}
