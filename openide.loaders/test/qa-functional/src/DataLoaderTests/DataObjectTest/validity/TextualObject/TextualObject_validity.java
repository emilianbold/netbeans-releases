package DataLoaderTests.DataObjectTest.validity.TextualObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class TextualObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public TextualObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/TextualObject.txt";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(TextualObject_validity.class);
   return suite;
 }
}