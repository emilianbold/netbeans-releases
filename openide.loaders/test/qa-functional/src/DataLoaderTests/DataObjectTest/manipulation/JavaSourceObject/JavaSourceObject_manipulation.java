package DataLoaderTests.DataObjectTest.manipulation.JavaSourceObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JavaSourceObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public JavaSourceObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JavaSourceObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JavaSourceObject_manipulation.class);
   return suite;
 }
}