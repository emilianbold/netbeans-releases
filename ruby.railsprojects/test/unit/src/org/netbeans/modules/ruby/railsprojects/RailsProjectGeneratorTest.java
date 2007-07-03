/*
 * RegexpOutputRecognizerTest.java
 * JUnit based test
 *
 * Created on March 5, 2007, 3:53 PM
 */
package org.netbeans.modules.ruby.railsprojects;



import junit.framework.TestCase;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer.FileLocation;
import org.netbeans.modules.ruby.rubyproject.execution.RegexpOutputRecognizer;

/**
 *
 * @author Tor Norbye
 */
public class RailsProjectGeneratorTest extends TestCase {
    
    public RailsProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWindows103139() {
        // \r\n's seems to trip it up
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "      create  app/helpers/application_helper.rb\r";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/helpers/application_helper.rb", location.file);
        s = "      create  public/javascripts/prototype.js\r\n";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("public/javascripts/prototype.js", location.file);
    }    

    public void testCreate() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "      create  app/helpers/application_helper.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/helpers/application_helper.rb", location.file);
    }    

    public void testIdentical() {
        // \r\n's seems to trip it up
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "   identical  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    

    public void testForce() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "       force  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    
    public void testSkip() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String s = "        skip  app/controllers/foo_controller.rb";
        location = recognizer.processLine(s);
        assertNotNull(location);
        assertEquals("app/controllers/foo_controller.rb", location.file);
    }    

    // Exists is used only for dirs, which we don't hyperlink -- XXX should we? And select/open in projects view?
//    public void testExists() {
//        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
//        FileLocation location;
//
//        String s = "      exists  app/views/foo.rb";
//        location = recognizer.processLine(s);
//        assertNotNull(location);
//        assertEquals("app/views/foo.rb", location.file);
//    }    
    
    public void testFiletypes() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        FileLocation location;

        String[] extensions = new String[] {"rb", "mab", "rjs", "rxml", "rake", "erb", "builder", "rhtml", "yml", "js", "html", "cgi", "fcgi", "txt", "png", "gif", "css"};
        for (String ext : extensions) {
            String s = "      create  app/helpers/application_helper." + ext;
            location = recognizer.processLine(s);
            assertNotNull("Failed to recognize " + s, location);
            assertEquals("app/helpers/application_helper." + ext, location.file);
        }
    }    

    public void testErrors() {
        RegexpOutputRecognizer recognizer = RailsProjectGenerator.RAILS_GENERATOR;
        // Make sure it doesn't recognize stuff which -shouldn't- be recognized
        assertNull(recognizer.processLine("foo"));
        assertNull(recognizer.processLine("create"));
        assertNull(recognizer.processLine("create foo.rb"));
        assertNull(recognizer.processLine("\"       force  app/controllers/foo_controller.rb\""));
        assertNull(recognizer.processLine("       Force  app/controllers/foo_controller.rb"));
        assertNull(recognizer.processLine(""));
        assertNull(recognizer.processLine("foo.rb"));
    }

}
