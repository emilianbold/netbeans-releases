package DataLoaderTests.DataObjectTest.manipulation.SwingFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class SwingFormObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public SwingFormObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/SwingFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(SwingFormObject_manipulation.class);
   return suite;
 }
}