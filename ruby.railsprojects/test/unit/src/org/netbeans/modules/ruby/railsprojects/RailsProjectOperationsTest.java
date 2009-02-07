/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.railsprojects;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.openide.filesystems.FileObject;

public class RailsProjectOperationsTest extends RailsProjectTestBase {

    public RailsProjectOperationsTest(String testName) {
        super(testName);
    }

    public void testMetadataFiles() throws Exception {
        RailsProject project = createTestPlainProject();
        project.open();
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled",
                ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));

        FileObject prjDir = project.getProjectDirectory();

        prjDir.createData(".cvsignore");

        FileObject[] expectedMetadataFiles = {
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles),
                ProjectOperations.getMetadataFiles(project));

    }

    public void testDataFiles() throws Exception {
        RailsProject project = createTestPlainProject();
        project.open();
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled",
                ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));

        FileObject prjDir = project.getProjectDirectory();

        prjDir.createData(".cvsignore");

        FileObject[] expectedMetadataFiles = {
            prjDir.getFileObject("app"),
            prjDir.getFileObject("app/controllers"),
            prjDir.getFileObject("app/helpers"),
            prjDir.getFileObject("app/models"),
            prjDir.getFileObject("app/views"),
            prjDir.getFileObject("components"),
            prjDir.getFileObject("config"),
            prjDir.getFileObject("db"),
            prjDir.getFileObject("doc"),
            prjDir.getFileObject("lib"),
            prjDir.getFileObject("log"),
            prjDir.getFileObject("public"),
            prjDir.getFileObject("script"),
            prjDir.getFileObject("spec"),
            prjDir.getFileObject("test"),
            prjDir.getFileObject("tmp"),
            prjDir.getFileObject("vendor"),
            prjDir.getFileObject("README"),
            prjDir.getFileObject("Rakefile"),
        };

        Comparator<FileObject> cmp = new Comparator<FileObject>() {
            public int compare(FileObject f1, FileObject f2) {
                return f1.getPath().compareTo(f2.getPath());
            }
        };
        List<FileObject> actual = ProjectOperations.getDataFiles(project);
        List<FileObject> expected = Arrays.asList(expectedMetadataFiles);
        Collections.sort(actual, cmp);
        Collections.sort(expected, cmp);
        assertEquals("correct metadata files", expected, actual);
    }
}
