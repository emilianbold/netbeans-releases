package DataLoaderTests.DataObjectTest.validity.ClassObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ClassObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public ClassObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ClassObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ClassObject_validity.class);
   return suite;
 }
}