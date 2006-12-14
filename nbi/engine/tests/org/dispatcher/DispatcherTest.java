/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.dispatcher;

import org.MyTestCase;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;
import org.netbeans.installer.downloader.dispatcher.impl.RoundRobinDispatcher;

/**
 *
 * @author Danila_Dugurov
 */
public class DispatcherTest extends MyTestCase {
  
  public void testRunStop() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(500, 1);
    assertFalse(dispatcher.isActive());
    dispatcher.start();
    assertTrue(dispatcher.isActive());
    dispatcher.stop();
    assertFalse(dispatcher.isActive());
    
    assertFalse(dispatcher.isActive());
    dispatcher.start();
    assertTrue(dispatcher.isActive());
    dispatcher.stop();
    assertFalse(dispatcher.isActive());
  }
  
  public void testSingleProcessAddAndTerminate() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    dispatcher.schedule(dummy);
    shortSleep();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(1, dispatcher.waitingCount());
    assertFalse(dummy.isProcessed());
    dispatcher.start();
    longSleep();
    assertTrue(dummy.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dummy.terminate();
    longSleep();
    assertFalse(dummy.isProcessed());
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
  }
  
  public void testSingleProcessAddAndDispatcherTerminate() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    dispatcher.schedule(dummy);
    dispatcher.start();
    longSleep();
    assertTrue(dummy.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dispatcher.stop();
    assertFalse(dummy.isProcessed());
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
  }
  
  private void longSleep() {
    try {
      Thread.sleep(300);
    } catch (InterruptedException ex) {//skip
    }
  }
  
  private void shortSleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ex) {//skip
    }
  }
}
