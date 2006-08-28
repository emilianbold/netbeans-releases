package DataLoaderTests.DataObjectTest.manipulation.JSPObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JSPObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public JSPObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JSPObject.jsp";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JSPObject_manipulation.class);
   return suite;
 }
}