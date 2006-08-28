package DataLoaderTests.DataObjectTest.modify.TextualObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class TextualObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public TextualObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/TextualObject.txt";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(TextualObject_modify.class);
   return suite;
 }
}