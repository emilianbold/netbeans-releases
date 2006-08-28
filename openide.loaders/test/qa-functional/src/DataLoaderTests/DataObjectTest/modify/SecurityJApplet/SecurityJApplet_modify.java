package DataLoaderTests.DataObjectTest.modify.SecurityJApplet;
import junit.framework.*;
import org.netbeans.junit.*;
public class SecurityJApplet_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public SecurityJApplet_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SecurityJApplet.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SecurityJApplet_modify.class);
   return suite;
 }
}