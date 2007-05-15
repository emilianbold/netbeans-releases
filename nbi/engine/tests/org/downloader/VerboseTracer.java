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
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */
public class VerboseTracer extends ActionsTracer {
  List<Pair<String, String[]>> verboseActions = new LinkedList<Pair<String, String[]>>();
  
  public VerboseTracer(PumpingsQueue queue) {
    super(queue);
  }
  
  public void pumpingUpdate(String id) {
    super.pumpingUpdate(id);
    verboseActions.add(Pair.create("update", new String[] {id}));
  }
  
  public void pumpingStateChange(String id) {
    super.pumpingStateChange(id);
    final Pumping pumping = queue.getById(id);
    verboseActions.add(Pair.create("stateChange", new String[] {id, pumping.state().toString()}));
  }
  
  public void pumpingAdd(String id) {
    super.pumpingAdd(id);
    verboseActions.add(Pair.create("add", new String[] {id}));
  }
  
  public void pumpingDelete(String id) {
    super.pumpingDelete(id);
    verboseActions.add(Pair.create("delete", new String[] {id}));
  }
  
  public void queueReset() {
    super.queueReset();
    verboseActions.add(Pair.create("reset", new String[0]));
  }
  
  public void pumpsInvoke() {
    super.pumpsInvoke();
    verboseActions.add(Pair.create("invoke", new String[0]));
  }
  
  public void pumpsTerminate() {
    super.pumpsTerminate();
    verboseActions.add(Pair.create("terminate", new String[0]));
  }
}
