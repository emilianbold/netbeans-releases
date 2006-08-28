package DataLoaderTests.DataObjectTest.delegate.TextualObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class TextualObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public TextualObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/TextualObject.txt";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(TextualObject_delegate.class);
   return suite;
 }
}