package DataLoaderTests.DataObjectTest.manipulation.ClassObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ClassObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public ClassObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ClassObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ClassObject_manipulation.class);
   return suite;
 }
}