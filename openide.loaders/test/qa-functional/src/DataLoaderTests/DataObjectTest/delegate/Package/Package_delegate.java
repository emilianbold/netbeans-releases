package DataLoaderTests.DataObjectTest.delegate.Package;
import junit.framework.*;
import org.netbeans.junit.*;
public class Package_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public Package_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/Package.";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(Package_delegate.class);
   return suite;
 }
}