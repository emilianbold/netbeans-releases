package DataLoaderTests.DataObjectTest.delegate.JSPObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JSPObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public JSPObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JSPObject.jsp";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JSPObject_delegate.class);
   return suite;
 }
}