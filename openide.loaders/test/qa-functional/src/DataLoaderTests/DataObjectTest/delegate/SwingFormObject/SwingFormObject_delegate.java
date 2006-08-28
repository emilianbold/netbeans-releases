package DataLoaderTests.DataObjectTest.delegate.SwingFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class SwingFormObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public SwingFormObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SwingFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SwingFormObject_delegate.class);
   return suite;
 }
}