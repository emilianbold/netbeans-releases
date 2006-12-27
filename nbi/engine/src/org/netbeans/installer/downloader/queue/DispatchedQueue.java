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
package org.netbeans.installer.downloader.queue;

import static org.netbeans.installer.downloader.DownloadConfig.DISPATCHER_POOL;
import static org.netbeans.installer.downloader.DownloadConfig.DISPATCHER_QUANTUM;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.Pumping;
import static org.netbeans.installer.downloader.Pumping.State;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;
import org.netbeans.installer.downloader.impl.Pump;
import org.netbeans.installer.downloader.dispatcher.impl.RoundRobinDispatcher;
import org.netbeans.installer.downloader.impl.PumpingImpl;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Danila_Dugurov
 */
public class DispatchedQueue extends QueueBase {
  
  private final ProcessDispatcher dispatcher = new RoundRobinDispatcher(DISPATCHER_QUANTUM, DISPATCHER_POOL);
  
  private final Map<String, Pump> pId2p = new HashMap<String, Pump>();
  
  public DispatchedQueue(File stateFile) {
    super(stateFile);
  }
  
  public synchronized void reset() {
    final boolean wasActive = dispatcher.isActive();
    if (wasActive) dispatcher.stop();
    for (String id: id2Pumping.keySet().toArray(new String[0])) {
      delete(id);
    }
    if (wasActive) dispatcher.start();
    fire("queueReset");
  }
  
  public synchronized Pumping add(URL url) {
    return add(url, DownloadManager.instance.defaultFolder());
  }
  
  public synchronized Pumping add(URL url, File folder) {
    final PumpingImpl newOne = new PumpingImpl(url, folder, this);
    final String id = newOne.getId();
    id2Pumping.put(id, newOne);
    if (dispatcher.isActive()) {
      final Pump pump = new Pump(newOne);
      pId2p.put(id, pump);
      dispatcher.schedule(pump);
    }
    fire("pumpingAdd", id);
    return newOne;
  }
  
  public synchronized Pumping delete(String id) {
    final PumpingImpl oldOne = id2Pumping.remove(id);
    if (oldOne == null) return null;
    dispatcher.terminate(pId2p.get(id));
    fire("pumpingDelete", id);
    pId2p.remove(id);
    if (oldOne.state() != State.FINISHED)
      oldOne.reset();
    return oldOne;
  }
  
  public synchronized void invoke() {
    if (dispatcher.isActive()) return;
    fire("pumpsInvoke");
    for (Pumping pumping : toArray()) {
      if (pumping.state() != State.FINISHED) {
        final Pump newOne = new Pump(pumping);
        pId2p.put(pumping.getId(), newOne);
        dispatcher.schedule(newOne);
      }
    }
    dispatcher.start();
  }
  
  public synchronized void terminate() {
    if (!dispatcher.isActive()) return;
    dispatcher.stop();
    dump();
    fire("pumpsTerminate");
  }
  
  public synchronized boolean isActive() {
    return dispatcher.isActive();
  }
}