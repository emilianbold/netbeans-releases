package DataLoaderTests.DataObjectTest.manipulation.Package;
import junit.framework.*;
import org.netbeans.junit.*;
public class Package_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public Package_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/Package.";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(Package_manipulation.class);
   return suite;
 }
}