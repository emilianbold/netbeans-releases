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

import org.netbeans.installer.downloader.dispatcher.Process;

/**
 *
 * @author Danila_Dugurov
 */
public class DummyProcess implements Process {
  
  public Thread worker = null;
  
  private boolean interrupted = false;
  
  private boolean isProcessed = false;
  
  private long workingStartTime = 0;
  private long workingEndTime = 0;
  
  public Thread getWorker() {
    return worker;
  }
  
  public boolean isProcessed() {
    return isProcessed;
  }
  
  public long workingStartTime() {
    return workingStartTime;
  }
  
  public long workingEndTime() {
    return workingEndTime;
  }
  
  public void init() {
    workingStartTime = System.currentTimeMillis();
    isProcessed = true;
    worker = Thread.currentThread();
  }
  
  public void run() {
    while (!interrupted) {
      int TwoPlusTwo = 0;
      TwoPlusTwo = 2 + 2;
      //bla bla bla some work..
      try {
        Thread.sleep(1000);
      } catch (InterruptedException exit) {
        break;
      }
    }
    isProcessed = false;
    interrupted = false;
    workingEndTime = System.currentTimeMillis();
  }
  
  public void terminate() {
    System.out.println("here");
    interrupted = true;
    workingEndTime = System.currentTimeMillis();
    worker.interrupt();
  }
}
