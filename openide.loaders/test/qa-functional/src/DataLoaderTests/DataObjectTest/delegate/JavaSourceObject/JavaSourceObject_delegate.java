package DataLoaderTests.DataObjectTest.delegate.JavaSourceObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JavaSourceObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public JavaSourceObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JavaSourceObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JavaSourceObject_delegate.class);
   return suite;
 }
}