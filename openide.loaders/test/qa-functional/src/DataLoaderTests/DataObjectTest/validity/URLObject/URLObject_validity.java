package DataLoaderTests.DataObjectTest.validity.URLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class URLObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public URLObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/URLObject.url";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(URLObject_validity.class);
   return suite;
 }
}