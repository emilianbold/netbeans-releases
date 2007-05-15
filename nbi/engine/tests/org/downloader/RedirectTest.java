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
 //   System.out.println(redirURL);
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
