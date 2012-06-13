/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.index.ArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.openide.util.test.TestFileUtils;

public class NexusRepositoryIndexerImplTest extends NexusTestBase {

    public NexusRepositoryIndexerImplTest(String n) {
        super(n);
    }

    public void testFilterGroupIds() throws Exception {
        install(File.createTempFile("whatever", ".txt", getWorkDir()), "test", "spin", "1.1", "txt");
        nrii.indexRepo(info);
        assertEquals("[test]", nrii.filterGroupIds("", Collections.singletonList(info)).getResults().toString());
    }

    public void testFind() throws Exception {
        installPOM("test", "plugin", "0", "maven-plugin");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "plugin.jar"), "META-INF/maven/plugin.xml:<plugin><goalPrefix>stuff</goalPrefix></plugin>"), "test", "plugin", "0", "maven-plugin");
        nrii.indexRepo(info);
        QueryField qf = new QueryField();
        qf.setField(ArtifactInfo.PLUGIN_PREFIX);
        qf.setValue("stuff");
        qf.setOccur(QueryField.OCCUR_MUST);
        qf.setMatch(QueryField.MATCH_EXACT);
        assertEquals("[test:plugin:0:test]", nrii.find(Collections.singletonList(qf), Collections.singletonList(info)).getResults().toString());
    }

    public void testLastUpdated() throws Exception { // #197670
        installPOM("test", "art", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "art.jar"), "stuff:whatever"), "test", "art", "0", "jar");
        File empty = TestFileUtils.writeFile(new File(getWorkDir(), "empty"), "# placeholder\n");
        install(empty, "test", "art", "0", "pom.lastUpdated");
        install(empty, "test", "art", "0", "jar.lastUpdated");
        nrii.indexRepo(info);
        List<NBVersionInfo> versions = nrii.getVersions("test", "art", Collections.singletonList(info)).getResults();
        assertEquals(1, versions.size());
        NBVersionInfo v = versions.get(0);
        assertEquals("test:art:0:test", v.toString());
        assertEquals("jar", v.getPackaging());
        assertEquals("jar", v.getType());
    }
    
}
