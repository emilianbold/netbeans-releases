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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */
public class ActionsTracer implements DownloadListener {
  
  List<Pair<String,String>> actions = new LinkedList<Pair<String,String>>();

  protected PumpingsQueue queue;
  
  protected ActionsTracer(PumpingsQueue queue) {
    this.queue = queue;
    queue.addListener(this);
  }
  
  public void pumpingUpdate(String id) {
    actions.add(Pair.create("update", id));
  }
  
  public void pumpingStateChange(String id) {
    actions.add(Pair.create("stateChange", id));
  }
  
  public void pumpingAdd(String id) {
    actions.add(Pair.create("add", id));
  }
  
  public void pumpingDelete(String id) {
    actions.add(Pair.create("delete", id));
  }
  
  public void queueReset() {
    actions.add(Pair.create("reset", ""));
  }
  
  public void pumpsInvoke() {
    actions.add(Pair.create("invoke", ""));
  }
  
  public void pumpsTerminate() {
    actions.add(Pair.create("terminate", ""));
  }
}
