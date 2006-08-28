package DataLoaderTests.DataObjectTest.delegate.URLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class URLObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public URLObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/URLObject.url";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(URLObject_delegate.class);
   return suite;
 }
}