package DataLoaderTests.DataObjectTest.manipulation.SecurityJApplet;
import junit.framework.*;
import org.netbeans.junit.*;
public class SecurityJApplet_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public SecurityJApplet_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SecurityJApplet.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SecurityJApplet_manipulation.class);
   return suite;
 }
}