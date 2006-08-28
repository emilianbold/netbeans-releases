package DataLoaderTests.DataObjectTest.others.URLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class URLObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public URLObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/URLObject.url";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(URLObject_others.class);
   return suite;
 }
}