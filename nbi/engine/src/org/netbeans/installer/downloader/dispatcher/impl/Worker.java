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
package org.netbeans.installer.downloader.dispatcher.impl;

import org.netbeans.installer.downloader.dispatcher.Process;

/**
 * @author Danila_Dugurov
 */
public class Worker extends Thread {
  
  Process current;
  
  public Worker() {
    super();
    setDaemon(true);
  }
  
  //if worker busy return false
  public synchronized boolean setCurrent(Process newCurrent) {
    if (!isFree()) return false;
    this.current = newCurrent;
    notifyAll();
    return true;
  }
  
  public synchronized boolean isFree() {
    return current == null;
  }
  
  public void run() {
    while (true) {
      try {
        Thread.interrupted();
        synchronized (this) {
          if (current == null){
            wait();
            if (isFree()) continue;
          }
        }
        current.init();
        current.run();
      } catch (InterruptedException ignored) {
      } finally {
        synchronized (this) {
          current = null;
        }
      }
    }
  }
}