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
