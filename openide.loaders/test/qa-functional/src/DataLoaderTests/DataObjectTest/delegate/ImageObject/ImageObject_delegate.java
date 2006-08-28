package DataLoaderTests.DataObjectTest.delegate.ImageObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ImageObject_delegate extends DataLoaderTests.DataObjectTest.DataObjectTest_delegate{
 public ImageObject_delegate(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ImageObject.jpg";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ImageObject_delegate.class);
   return suite;
 }
}