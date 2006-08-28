package DataLoaderTests.DataObjectTest.delegate.HTMLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class HTMLObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public HTMLObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/HTMLObject.html";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(HTMLObject_delegate.class);
   return suite;
 }
}