package DataLoaderTests.DataObjectTest.others.HTMLObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class HTMLObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public HTMLObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/HTMLObject.html";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(HTMLObject_others.class);
   return suite;
 }
}