package DataLoaderTests.DataObjectTest.others.SecurityJApplet;
import junit.framework.*;
import org.netbeans.junit.*;
public class SecurityJApplet_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public SecurityJApplet_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SecurityJApplet.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SecurityJApplet_others.class);
   return suite;
 }
}