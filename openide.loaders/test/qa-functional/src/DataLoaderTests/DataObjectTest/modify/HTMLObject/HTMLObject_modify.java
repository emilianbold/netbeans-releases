package DataLoaderTests.DataObjectTest.modify.HTMLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class HTMLObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public HTMLObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/HTMLObject.html";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(HTMLObject_modify.class);
   return suite;
 }
}