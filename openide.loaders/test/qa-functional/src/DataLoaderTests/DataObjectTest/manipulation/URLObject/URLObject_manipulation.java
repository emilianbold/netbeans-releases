package DataLoaderTests.DataObjectTest.manipulation.URLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class URLObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public URLObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/URLObject.url";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(URLObject_manipulation.class);
   return suite;
 }
}