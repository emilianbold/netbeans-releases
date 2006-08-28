package DataLoaderTests.DataObjectTest.manipulation.TextualObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class TextualObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public TextualObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/TextualObject.txt";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(TextualObject_manipulation.class);
   return suite;
 }
}