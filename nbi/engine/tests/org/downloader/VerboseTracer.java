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
