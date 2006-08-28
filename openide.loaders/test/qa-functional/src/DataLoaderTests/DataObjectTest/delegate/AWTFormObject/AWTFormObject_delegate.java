package DataLoaderTests.DataObjectTest.delegate.AWTFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class AWTFormObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public AWTFormObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/AWTFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(AWTFormObject_delegate.class);
   return suite;
 }
}