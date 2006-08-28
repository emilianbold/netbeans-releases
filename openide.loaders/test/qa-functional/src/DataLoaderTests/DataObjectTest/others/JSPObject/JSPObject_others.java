package DataLoaderTests.DataObjectTest.others.JSPObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JSPObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public JSPObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JSPObject.jsp";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JSPObject_others.class);
   return suite;
 }
}