/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
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
            prjDir.getFileObject("test/fixtures"),
            prjDir.getFileObject("test/functional"),
            prjDir.getFileObject("test/integration"),
            prjDir.getFileObject("test/mocks"),
            prjDir.getFileObject("test/unit"),
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
