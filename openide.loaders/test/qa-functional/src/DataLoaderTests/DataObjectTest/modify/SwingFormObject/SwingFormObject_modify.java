package DataLoaderTests.DataObjectTest.modify.SwingFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class SwingFormObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public SwingFormObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SwingFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SwingFormObject_modify.class);
   return suite;
 }
}