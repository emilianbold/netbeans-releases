package org.netbeans.installer.downloader.queue;

import org.netbeans.installer.Installer;
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
import org.netbeans.installer.utils.exceptions.UnexpectedExceptionError;

/**
 *
 * @author Danila_Dugurov
 */
public class DispatchedQueue extends QueueBase {
  
  private final ProcessDispatcher dispatcher = new RoundRobinDispatcher(100, 10);
  
  private final Map<String, Pump> pId2p = new HashMap<String, Pump>();
  
  public DispatchedQueue(File stateFile) {
    super(stateFile);
  }
  
   public synchronized void reset() {
    final boolean wasActive = dispatcher.isActive();
    if (wasActive) dispatcher.stop();
    pId2p.clear();
    id2Pumping.clear();
    if (wasActive) dispatcher.start();
    try {
      fire("queueReset");
    } catch(NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
  }
  
  public synchronized Pumping add(URL url) {
    final File downloads = new File(Installer.DEFAULT_LOCAL_DIRECTORY_PATH, "downloads");
    downloads.mkdirs();
    return add(url, downloads);
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
    try {
      fire("pumpingAdd", id);
      return newOne;
    } catch (NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
  }
  
  public synchronized Pumping delete(String id) {
    final PumpingImpl oldOne = id2Pumping.remove(id);
    if (oldOne == null) return null;
    dispatcher.terminate(pId2p.get(id));
    pId2p.remove(id);
    if (oldOne.state() != State.FINISHED)
      oldOne.reset();
    try {
      fire("pumpingDelete", id);
      return oldOne;
    } catch (NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
  }
  
  public synchronized void invoke() {
    if (dispatcher.isActive()) return;
    try {
      fire("pumpsInvoke");
    } catch(NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
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
    try {
      fire("pumpsTerminate");
    } catch(NoSuchMethodException ex) {
      throw new UnexpectedExceptionError("Listener contract was changed", ex);
    }
  }
  
  public synchronized boolean isActive() {
    return dispatcher.isActive();
  }
}