package DataLoaderTests.DataObjectTest.validity.Package;
import junit.framework.*;
import org.netbeans.junit.*;
public class Package_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public Package_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/Package.";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(Package_validity.class);
   return suite;
 }
}