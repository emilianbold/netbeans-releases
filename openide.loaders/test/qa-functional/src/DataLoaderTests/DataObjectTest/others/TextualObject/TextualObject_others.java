package DataLoaderTests.DataObjectTest.others.TextualObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class TextualObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public TextualObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/TextualObject.txt";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(TextualObject_others.class);
   return suite;
 }
}