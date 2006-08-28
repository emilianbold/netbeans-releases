package DataLoaderTests.DataObjectTest.others.ClassObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ClassObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public ClassObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ClassObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ClassObject_others.class);
   return suite;
 }
}