package DataLoaderTests.DataObjectTest.delegate.SecurityJApplet;
import junit.framework.*;
import org.netbeans.junit.*;
public class SecurityJApplet_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public SecurityJApplet_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SecurityJApplet.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SecurityJApplet_delegate.class);
   return suite;
 }
}