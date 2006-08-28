package DataLoaderTests.DataObjectTest.modify.Package;
import junit.framework.*;
import org.netbeans.junit.*;
public class Package_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public Package_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/Package.";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(Package_modify.class);
   return suite;
 }
}