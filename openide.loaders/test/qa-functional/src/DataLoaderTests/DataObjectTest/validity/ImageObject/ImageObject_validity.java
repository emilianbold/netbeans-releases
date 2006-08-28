package DataLoaderTests.DataObjectTest.validity.ImageObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ImageObject_validity extends DataLoaderTests.DataObjectTest.DataObjectTest_validity{
 public ImageObject_validity(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ImageObject.jpg";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ImageObject_validity.class);
   return suite;
 }
}