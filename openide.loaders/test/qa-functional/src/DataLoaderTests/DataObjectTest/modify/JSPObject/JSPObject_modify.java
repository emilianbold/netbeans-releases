package DataLoaderTests.DataObjectTest.modify.JSPObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JSPObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public JSPObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JSPObject.jsp";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JSPObject_modify.class);
   return suite;
 }
}