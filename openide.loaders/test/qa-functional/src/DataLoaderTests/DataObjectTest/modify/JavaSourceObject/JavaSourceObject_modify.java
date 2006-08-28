package DataLoaderTests.DataObjectTest.modify.JavaSourceObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JavaSourceObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public JavaSourceObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JavaSourceObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JavaSourceObject_modify.class);
   return suite;
 }
}