/*
 * Issue84411Test.java
 * JUnit based test
 *
 * Created on September 9, 2006, 9:01 AM
 */

package regression;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import junit.framework.TestCase;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author tball
 * @author Tomas Zezula
 */
public class Issue219426Test extends TestCase {
    
    public Issue219426Test(String testName) {
        super(testName);
    }
       
    public void test84411() throws Exception {
        final InputStream classData =
            getClass().getResourceAsStream("datafiles/left-square.class");  //NOI18N
        try {
            final ClassFile c = new ClassFile(classData);
            fail("Invalid class name [, exception expected.");    //NOI18N
        } catch (InvalidClassFormatException e) {
            //OK, expected
        } finally {
            classData.close();
        }
    }

}
