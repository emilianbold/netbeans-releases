package DataLoaderTests.DataObjectTest.manipulation.ImageObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ImageObject_manipulation extends DataLoaderTests.DataObjectTest.DataObjectTest_manipulation{
 public ImageObject_manipulation(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ImageObject.jpg";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ImageObject_manipulation.class);
   return suite;
 }
}