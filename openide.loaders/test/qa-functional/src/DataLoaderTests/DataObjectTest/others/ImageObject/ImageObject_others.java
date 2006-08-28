package DataLoaderTests.DataObjectTest.others.ImageObject;
import junit.framework.*;
import org.netbeans.junit.*;
public class ImageObject_others extends DataLoaderTests.DataObjectTest.DataObjectTest_others{
 public ImageObject_others(java.lang.String testName){
   super(testName);
   NAME = "/DataObjectTest/ImageObject.jpg";
 }
public static Test suite() {
   NbTestSuite suite = new NbTestSuite(ImageObject_others.class);
   return suite;
 }
}