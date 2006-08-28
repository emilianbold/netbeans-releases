package DataLoaderTests.DataObjectTest.modify.ClassObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ClassObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public ClassObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ClassObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ClassObject_modify.class);
   return suite;
 }
}