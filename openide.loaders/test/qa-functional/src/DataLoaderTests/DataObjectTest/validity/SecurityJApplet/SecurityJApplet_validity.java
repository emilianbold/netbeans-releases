package DataLoaderTests.DataObjectTest.validity.SecurityJApplet;
import junit.framework.*;
import org.netbeans.junit.*;
public class SecurityJApplet_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public SecurityJApplet_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SecurityJApplet.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SecurityJApplet_validity.class);
   return suite;
 }
}