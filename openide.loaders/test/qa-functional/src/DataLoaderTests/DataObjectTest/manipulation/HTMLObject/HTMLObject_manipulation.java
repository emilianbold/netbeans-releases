package DataLoaderTests.DataObjectTest.manipulation.HTMLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class HTMLObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public HTMLObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/HTMLObject.html";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(HTMLObject_manipulation.class);
   return suite;
 }
}