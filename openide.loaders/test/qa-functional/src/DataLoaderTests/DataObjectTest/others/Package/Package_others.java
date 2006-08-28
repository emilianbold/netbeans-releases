package DataLoaderTests.DataObjectTest.others.Package;
import junit.framework.*;
import org.netbeans.junit.*;
public class Package_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public Package_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/Package.";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(Package_others.class);
   return suite;
 }
}