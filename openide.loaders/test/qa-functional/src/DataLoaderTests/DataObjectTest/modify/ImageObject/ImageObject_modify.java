package DataLoaderTests.DataObjectTest.modify.ImageObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ImageObject_modify extends DataLoaderTests.DataObjectTest.DataObjectTest_modify{
 public ImageObject_modify(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ImageObject.jpg";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ImageObject_modify.class);
   return suite;
 }
}