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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.maven;

import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;

public class HudsonProviderImplTest extends NbTestCase {

    public HudsonProviderImplTest(String n) {
        super(n);
    }

    static {
        HudsonProviderImpl.TEST = true;
    }
    
    private Project mockProject(String pomXml) throws Exception {
        clearWorkDir();
        final FileObject d = FileUtil.toFileObject(getWorkDir());
        if (pomXml != null) {
            TestFileUtils.writeFile(d, "pom.xml", pomXml);
        }
        return new Project() {
            public FileObject getProjectDirectory() {
                return d;
            }
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
    }

    public void testFindAssociation1() throws Exception {
        assertNull(new HudsonProviderImpl().findAssociation(mockProject(null)));
    }

    /* XXX CatalogModelFactory.default.getCatalogModel == null
    public void testFindAssociation2() throws Exception {
        assertNull(new HudsonProviderImpl().findAssociation(mockProject("<project/>")));
    }

    public void testFindAssociation3() throws Exception {
        Association a = new HudsonProviderImpl().findAssociation(mockProject(
                "<project><ciManagement><system>hudson</system><url>https://hudson.geomatys.fr/job/GeoAPI/</url></ciManagement></project>"));
        assertNotNull(a);
        assertEquals("https://hudson.geomatys.fr/", a.getServerUrl());
        assertEquals("GeoAPI", a.getJobName());
    }

    public void testRecordAssociation() throws Exception {
        Project p = mockProject("<project/>");
        new HudsonProviderImpl().recordAssociation(p, new Association("http://nowhere.net/", "foo bar"));
        assertEquals("<project><ciManagement><system>hudson</system><url>http://nowhere.net/job/foo%bar/</url></ciManagement></project>",
                p.getProjectDirectory().getFileObject("pom.xml").asText().replaceAll("\\S+", ""));
        assertEquals("http://nowhere.net/job/foo%bar/", String.valueOf(new HudsonProviderImpl().findAssociation(p)));
    }
     */

}
