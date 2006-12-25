package org.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.downloader.services.EmptyQueueListener;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class RedirectTest extends WithServerTestCase {
  
  public void testWithRedirect() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final DownloadListener listener = new EmptyQueueListener() {
      public void pumpingStateChange(String id) {
        final Pumping pumping = queue.getById(id);
        System.out.println("pumping url: " + pumping.declaredURL());
        System.out.println("pumping real url: " + pumping.realURL());
        System.out.println("pumping file " + pumping.outputFile() + " " + pumping.state());
        if (pumping.state() == Pumping.State.FINISHED) {
          assertEquals(pumping.length(), TestDataGenerator.testFileSizes[0]);
          assertEquals(pumping.realURL(), TestDataGenerator.testUrls[0]);
          synchronized (RedirectTest.this) {
            RedirectTest.this.notify();
          }
        } else if (pumping.state() == Pumping.State.FAILED) {
          synchronized (RedirectTest.this) {
            RedirectTest.this.notify();
          }
          fail();
        }
      }
    };
    queue.addListener(listener);
    URL redirURL = null;
    try {
      redirURL = new URL("http://localhost:" + WithServerTestCase.PORT + "/redirect/" + TestDataGenerator.testFiles[0]);
    } catch (MalformedURLException ex) {
      fail();
    }
    System.out.println(redirURL);
    queue.invoke();
    synchronized (this) {
      queue.add(redirURL, MyTestCase.testOutput);
      try {
        wait();
      } catch (InterruptedException ex) {
        fail();
      }
    }
    queue.terminate();
  }
}
