/*
 * GotoTestTest.java
 * JUnit based test
 *
 * Created on July 23, 2007, 10:48 AM
 */

package org.netbeans.modules.ruby.rubyproject;

import java.awt.event.ActionEvent;
import java.io.File;
import junit.framework.TestCase;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class GotoTestTest extends RubyProjectTestBase {
    
    private RubyProject project;
    private GotoTest gotoTest;
    
    public GotoTestTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        project = createTestProject("GotoTest");
        assertNotNull(project);
        FileObject dir = project.getProjectDirectory();
        assertNotNull(dir);
        createFilesFromDesc(dir, "testfiles/gototestfiles");
        
        // Create some data files where the class -contents- are used to locate the test
        createFile(dir, "lib/hello.rb", "class Hello\ndef foo\nend\nend\n");
        createFile(dir, "test/world.rb", "class HelloTest\ndef foobar\nend\nend\n");
        
        gotoTest = new GotoTest();
    }
    
    private FileObject getProjFile(String file) {
        return project.getProjectDirectory().getFileObject(file);
    }
    
    private void assertIsProjFile(String file, FileObject fo) {
        String relative = getRelative(fo);
        
        assertEquals(relative, file);
    }

    private String getRelative(FileObject fo) {
        assertNotNull(fo);
        File path = FileUtil.toFile(fo);
        File projPath = FileUtil.toFile(project.getProjectDirectory());
        String relative = path.getAbsolutePath().substring(projPath.getAbsolutePath().length()+1);
        
        return relative;
    }
    
    public void testGotoTestUnit() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTest(getProjFile("lib/foo.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("test/test_foo.rb", loc.getFileObject());
    }

    public void testGotoTestUnit2() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTest(getProjFile("lib/bar.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("test/tc_bar.rb", loc.getFileObject());
    }

    // The ZenTest patterns are only checked if the ZenTest gem is installed
    //public void testGotoTestZenTest() {
    //    assertNotNull(project);
    //
    //    DeclarationLocation loc = gotoTest.findTest(getProjFile("app/controllers/my_controller.rb"), -1);
    //    assertNotSame(DeclarationLocation.NONE, loc);
    //    assertIsProjFile("test/controllers/my_controller_test.rb", loc.getFileObject());
    //}

    public void testGotoTestRspec() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTest(getProjFile("app/models/whatever.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("spec/models/whatever_spec.rb", loc.getFileObject());
    }

    public void testGotoTestRails() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTest(getProjFile("app/models/mymodel.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("test/unit/mymodel_test.rb", loc.getFileObject());
    }
    
    public void testEnsureDirection1() {
        // Make sure that looking for a test when we're in a test doesn't return the opposite
        // file
        assertNotNull(project);
        DeclarationLocation loc = gotoTest.findTest(getProjFile("test/test_foo.rb"), -1);
        assertSame(DeclarationLocation.NONE, loc);
    }

    public void testEnsureDirection2() {
        assertNotNull(project);
        DeclarationLocation loc = gotoTest.findTested(getProjFile("lib/foo.rb"), -1);
        assertSame(DeclarationLocation.NONE, loc);
    }

    public void testGotoTestedUnit() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTested(getProjFile("test/test_foo.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("lib/foo.rb", loc.getFileObject());
    }

    public void testGotoTestedUnit2() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTested(getProjFile("test/tc_bar.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("lib/bar.rb", loc.getFileObject());
    }

    // The ZenTest patterns are only checked if the ZenTest gem is installed
    //public void testGotoTestedZenTest() {
    //    assertNotNull(project);
    //
    //    DeclarationLocation loc = gotoTest.findTested(getProjFile("test/controllers/my_controller_test.rb"), -1);
    //    assertNotSame(DeclarationLocation.NONE, loc);
    //    assertIsProjFile("app/controllers/my_controller.rb", loc.getFileObject());
    //}

    public void testGotoTestedRspec() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTested(getProjFile("spec/models/whatever_spec.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("app/models/whatever.rb", loc.getFileObject());
    }

    public void testGotoTestedRails() {
        assertNotNull(project);
        
        DeclarationLocation loc = gotoTest.findTested(getProjFile("test/unit/mymodel_test.rb"), -1);
        assertNotSame(DeclarationLocation.NONE, loc);
        assertIsProjFile("app/models/mymodel.rb", loc.getFileObject());
    }

    // The code index doesn't yet work at test time so the index search for HelloTest won't work
    //public void testGotoTestedClass() {
    //    assertNotNull(project);
    //
    //    DeclarationLocation loc = gotoTest.findTested(getProjFile("lib/hello.rb"), -1);
    //    assertNotSame(DeclarationLocation.NONE, loc);
    //    assertIsProjFile("test/world.rb", loc.getFileObject());
    //}

    private String[] FILES = {
        "lib/foo.rb",
        "test/test_foo.rb",
        
        "lib/bar.rb",
        "test/tc_bar.rb",
        
        //"app/controllers/my_controller.rb",
        //"test/controllers/my_controller_test.rb",
        
        "app/models/whatever.rb",
        "spec/models/whatever_spec.rb",
        
        "app/models/mymodel.rb",
        "test/unit/mymodel_test.rb"
    };
    
    public void testFindOpposite() {
        int index = 0;
        for (; index < FILES.length; index += 2) {
            FileObject source = getProjFile(FILES[index]);
            FileObject test = getProjFile(FILES[index+1]);
            
            DeclarationLocation loc = gotoTest.findTest(source, -1);
            assertEquals(test, loc.getFileObject());

            loc = gotoTest.findTested(test, -1);
            assertEquals(source, loc.getFileObject());
            
            loc = gotoTest.findOpposite(test, -1);
            assertEquals(source, loc.getFileObject());
            loc = gotoTest.findOpposite(source, -1);
            assertEquals(test, loc.getFileObject());
        }
    }
}
