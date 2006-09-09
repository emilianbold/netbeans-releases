/*
 * Issue84411Test.java
 * JUnit based test
 *
 * Created on September 9, 2006, 9:01 AM
 */

package regression;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author tball
 */
public class Issue84411Test extends TestCase {
    
    public Issue84411Test(String testName) {
        super(testName);
    }
    
    /**
     * Test whether the SwitchData.class from Java 6 build 71 can be read 
     * successfully.  Issue 84411 reported that an IndexOutOfBoundsException
     * was thrown due to an invalid name_attribute_index in one of that 
     * class's Code attributes.
     */
    public void test84411() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/SwitchData.class");
        ClassFile classFile = new ClassFile(classData);
        classFile.toString();
    }
}
