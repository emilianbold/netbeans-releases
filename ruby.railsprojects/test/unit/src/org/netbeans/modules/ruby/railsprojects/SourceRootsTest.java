/*
 * SourceRootsTest.java
 * JUnit based test
 *
 * Created on September 1, 2007, 8:15 AM
 */

package org.netbeans.modules.ruby.railsprojects;

import junit.framework.TestCase;
import org.netbeans.modules.ruby.railsprojects.ui.FoldersListSettings;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class SourceRootsTest extends RailsProjectTestBase {
    
    public SourceRootsTest(String testName) {
        super(testName);
    }
    
    private RailsProject constructRailsProject(String dataFile) throws Exception {
        RailsProject project = createTestProject();
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
