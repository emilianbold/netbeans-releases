package DataLoaderTests.DataObjectTest.validity.HTMLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class HTMLObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public HTMLObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/HTMLObject.html";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(HTMLObject_validity.class);
   return suite;
 }
}