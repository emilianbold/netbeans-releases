package DataLoaderTests.DataObjectTest.others.JavaSourceObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class JavaSourceObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public JavaSourceObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/JavaSourceObject.java";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(JavaSourceObject_others.class);
   return suite;
 }
}