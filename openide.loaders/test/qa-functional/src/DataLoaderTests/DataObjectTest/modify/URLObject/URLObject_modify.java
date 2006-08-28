package DataLoaderTests.DataObjectTest.modify.URLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class URLObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public URLObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/URLObject.url";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(URLObject_modify.class);
   return suite;
 }
}