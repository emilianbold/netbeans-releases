/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
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
