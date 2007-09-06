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
import java.util.Comparator;
import org.netbeans.modules.ruby.railsprojects.ui.FoldersListSettings;
import org.openide.filesystems.FileObject;

/**
 * @author Tor Norbye
 */
public class SourceRootsTest extends RailsProjectTestBase {
    
    public SourceRootsTest(String testName) {
        super(testName);
    }
    
    private RailsProject constructRailsProject(String dataFile) throws Exception {
        RailsProject project = createTestProject("RubyProject", "app/.svn/dummy.txt");
        // Create file from data folder
        createFilesFromDesc(project.getProjectDirectory(), dataFile);
        return project;
    }

    private void checkView(String file, boolean logical) throws Exception {
        FoldersListSettings.getDefault().setLogicalView(logical);
        RailsProject project = constructRailsProject(file);
        assertNotNull(project);


        StringBuilder sb = new StringBuilder();
        SourceRoots roots = project.getSourceRoots();
        if (roots != null) {
            sb.append("rootnames:\n");
            String[] rootNames = roots.getRootNames();
            if (rootNames != null) {
                for (String s : rootNames) {
                    sb.append("  ");
                    sb.append(s);
                    sb.append("\n");
                }
            }
            sb.append("rootProperties:\n");
            String[] rootProperties = roots.getRootProperties();
            if (rootProperties != null) {
                for (String s : rootProperties) {
                    sb.append("  ");
                    sb.append(s);
                    sb.append("\n");
                }
            }
            sb.append("rootFiles:\n");
            FileObject[] rootFiles = roots.getRoots();
            if (rootFiles != null) {
                Arrays.sort(rootFiles, new Comparator<FileObject>() {
                    public int compare(FileObject f1, FileObject f2) {
                        return f1.getNameExt().compareTo(f2.getNameExt());
                    }
                });
                for (FileObject f : rootFiles) {
                    sb.append("  ");
                    String s = f.getNameExt();
                    sb.append(s);
                    sb.append("\n");
                }
            }
            sb.append("extraFiles:\n");
            FileObject[] extraFiles = roots.getExtraFiles();
            if (extraFiles != null) {
                Arrays.sort(extraFiles, new Comparator<FileObject>() {
                    public int compare(FileObject f1, FileObject f2) {
                        return f1.getNameExt().compareTo(f2.getNameExt());
                    }
                });
                for (FileObject f : extraFiles) {
                    sb.append("  ");
                    String s = f.getNameExt();
                    sb.append(s);
                    sb.append("\n");
                }
            }
        }

        assertDescriptionMatches(file, sb.toString(), false, (logical ? ".logical" : ".files") + ".roots");
    }
    
    public void testLogicalView() throws Exception {
        checkView("testfiles/plain_rails.txt", true);
    }

    public void testPhysicalView() throws Exception {
        checkView("testfiles/plain_rails.txt", false);
    }
    
    public void testLogicalView2() throws Exception {
        checkView("testfiles/rails_with_extra.txt", true);
    }

    public void testPhysicalView2() throws Exception {
        checkView("testfiles/rails_with_extra.txt", false);
    }
}
