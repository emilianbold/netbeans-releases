package org.netbeans.modules.projectimport.eclipse;

import java.util.Collection;
import junit.framework.TestCase;

/**
 * @author Martin Krauskopf
 */
public class UserLibraryParserTest extends TestCase {
    
    public UserLibraryParserTest(String testName) {
        super(testName);
    }
    
    public void testGetJars_75112() throws Exception {
        String xmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<userlibrary systemlibrary=\"true\" version=\"1\">\n" +
                "\t<archive sourceattachment=\"/space/java/0_lib\" path=\"/space/java/0_lib/cb2.jar\">\n" +
                "\t\t<attributes>\n" +
                "\t\t\t<attribute value=\"/space/java/0_lib\" name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\"/>\n" +
                "\t\t</attributes>\n" +
                "\t</archive>\n" +
                "\t<archive path=\"/space/java/0_lib/commons-collections-2.1.jar\"/>\n" +
                "\t<archive path=\"/space/java/0_lib/commons-digester-1.4.1.jar\"/>\n" +
                "</userlibrary>\n";
        Collection jars = UserLibraryParser.getJars(xmlDoc);
        assertEquals("three classpath entries", 3, jars.size());
    }
    
}
