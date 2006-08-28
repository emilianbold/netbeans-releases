package DataLoaderTests.DataObjectTest.validity.SwingFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class SwingFormObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public SwingFormObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SwingFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SwingFormObject_validity.class);
   return suite;
 }
}