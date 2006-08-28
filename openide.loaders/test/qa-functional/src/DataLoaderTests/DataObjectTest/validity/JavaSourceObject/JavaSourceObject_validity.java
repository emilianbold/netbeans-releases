package DataLoaderTests.DataObjectTest.validity.JavaSourceObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JavaSourceObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public JavaSourceObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JavaSourceObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JavaSourceObject_validity.class);
   return suite;
 }
}