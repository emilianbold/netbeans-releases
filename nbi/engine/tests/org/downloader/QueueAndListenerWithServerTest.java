package org.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadConfig;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.downloader.services.EmptyQueueListener;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class QueueAndListenerWithServerTest extends WithServerTestCase {
  
  public void testFailedDwonload() throws MalformedURLException {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final VerboseTracer listener = new VerboseTracer(queue);
    EmptyQueueListener notifier = new EmptyQueueListener() {
      public void pumpingStateChange(String id) {
        System.out.println(queue.getById(id).state());
        if (queue.getById(id).state() == Pumping.State.FAILED) {
          synchronized (queue) {
            queue.notifyAll();
          }
        }
      }
    };
    queue.addListener(notifier);
    queue.invoke();
    queue.add(new URL("http://www.oblom.com:8080/oblom.data"));
    synchronized (queue) {
      try {
        queue.wait();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    queue.terminate();
    assertEquals(2 + DownloadConfig.ATTEMPT_COUNT * 2 + 2, listener.verboseActions.size());
    assertEquals("invoke", listener.verboseActions.get(0).getFirst());
    assertEquals("add", listener.verboseActions.get(1).getFirst());
    for (int i = 2 ; i < 2 + DownloadConfig.ATTEMPT_COUNT * 2; i+=2) {
      assertEquals("stateChange", listener.verboseActions.get(i).getFirst());
      assertEquals(Pumping.State.CONNECTING.toString(), listener.verboseActions.get(i).getSecond()[1]);
      assertEquals("stateChange", listener.verboseActions.get(i + 1).getFirst());
      assertEquals(Pumping.State.WAITING.toString(), listener.verboseActions.get(i+1).getSecond()[1]);
    }
    assertEquals(Pumping.State.FAILED.toString(), listener.verboseActions.get(2 + DownloadConfig.ATTEMPT_COUNT * 2).getSecond()[1]);
    assertEquals("terminate", listener.verboseActions.get(2 + DownloadConfig.ATTEMPT_COUNT * 2 + 1).getFirst());
  }
}
