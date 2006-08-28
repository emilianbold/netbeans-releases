package DataLoaderTests.DataObjectTest.validity.AWTFormObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class AWTFormObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public AWTFormObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/AWTFormObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(AWTFormObject_validity.class);
   return suite;
 }
}