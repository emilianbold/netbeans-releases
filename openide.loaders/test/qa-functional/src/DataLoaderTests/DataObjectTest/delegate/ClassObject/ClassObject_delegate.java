package DataLoaderTests.DataObjectTest.delegate.ClassObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ClassObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public ClassObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ClassObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ClassObject_delegate.class);
   return suite;
 }
}