package DataLoaderTests.DataObjectTest.validity.JSPObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JSPObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public JSPObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JSPObject.jsp";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JSPObject_validity.class);
   return suite;
 }
}