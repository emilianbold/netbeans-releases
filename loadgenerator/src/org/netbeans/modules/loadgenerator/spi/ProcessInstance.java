package org.netbeans.modules.loadgenerator.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.loadgenerator.utils.NullOutputWriter;
import org.openide.util.WeakListeners;
import org.openide.windows.OutputWriter;

/**
 * Provides the basic scaffolding for a ILoadGeneratorInstance implementation
 *
 *
 * @author Jaroslav Bachorik
 */
public abstract class ProcessInstance {
  public static final String FACTORY = ProcessInstance.class.getName() + "#FACTORY";
  public final static String STATE = ProcessInstance.class.getName() + "#STATE";
  
  private static final OutputWriter NULLWRITER = new NullOutputWriter();
  
  private Engine factory;
  private boolean isNewFlag = true;
  
  
  public ProcessInstance(final Engine factory) {
    pcs = new PropertyChangeSupport(this);
    listeners = new ArrayList<ProcessInstanceListener>();
    listenerMap = Collections.synchronizedMap(new HashMap<ProcessInstanceListener, ProcessInstanceListener>());
    
    setFactory(factory);
  }
  
  public synchronized boolean isNew() {
    return isNewFlag;
  }
  
  public synchronized void touch() {
    isNewFlag = false;
  }
   
  void attachFactory(final Engine factory) {
    setFactory(factory);
  }
  
  void detachFactory() {
    setFactory(null);
  }
  
  /**
   * Registers a new listener
   * @param listener The listener instance to register
   */
  public void addListener(final ProcessInstanceListener listener) {
    ProcessInstanceListener weak = WeakListeners.create(ProcessInstanceListener.class, listener, this);
    if (!listeners.contains(weak)) {
      listeners.add(weak);
      listenerMap.put(listener, weak);
    }
  }
  
  // <editor-fold defaultstate="collapsed" desc="PropertyChange support">
  public void addPropertyChangeListener(final PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }
  
  public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(propertyName, pcl);
  }
  
  public void removePropertyChangeListener(final PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(pcl);
  }
  
  public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(propertyName, pcl);
  }
  // </editor-fold>
  
  /**
   * Unregisters a listener
   * @param listener The listener to unregister
   */
  public void removeListener(final ProcessInstanceListener listener) {
    ProcessInstanceListener weak = listenerMap.get(listener);
    if (weak != null) {
      listeners.remove(weak);
      listenerMap.remove(listener);
    }
  }
  
  public Engine getFactory() {
    return factory;
  }
  
  public void start(final String scriptFileName) {
    currentScript = scriptFileName;
    performStart(scriptFileName);
  }
  
  public void start() {
    if (currentScript != null) {
      performStart(currentScript);
    }
  }
  
  public void stop(final boolean force) {
    if (isRunning()) {
      performStop(force);
    } else {
      publishStart(); // fake start; just to be sure we are stopping the engine in a consistent state
      publishStop();
    }
  }
  
  public String getCurrentScript() {
    return currentScript;
  }
  
  public void setCurrentScript(final String value) {
    currentScript = value;
  }
  
  /**
   * Attaches an OutputWriter instance to load generator
   */
  public void attachWriter(final OutputWriter writer) {
    this.writer = writer;
  }
  
  /**
   * Detaches the previously set OutputWriter
   */
  public void detachWriter() {
    this.writer = null;
  }
  
  public abstract void performStart(final String scriptFileName);
  public abstract void performStop(final boolean force);
  
  /**
   * Indicates the running status of a ProcessInstance instance
   */
  public abstract boolean isRunning();
  
  /**
   * Returns a descriptive name for the particular process instance
   * @return Returns a descriptive name
   */
  public abstract String getDisplayName();
  
  /**
   * Returns the icon representing the load generator process if it exists
   * @return Returns the icon representing the load generator or null
   */
  public abstract Image getIcon();
  
  /************* Private implementation ******************/
  private Collection<ProcessInstanceListener> listeners;
  private Map<ProcessInstanceListener, ProcessInstanceListener> listenerMap;
  private PropertyChangeSupport pcs;
  private String currentScript;
  private OutputWriter writer;
  
  protected synchronized void publishStart() {
    for(ProcessInstanceListener listener : listeners) {
      listener.generatorStarted(this);
    }
    pcs.firePropertyChange(STATE, false, true);
  }
  
  protected synchronized void publishStart(final String logPath) {
    for(ProcessInstanceListener listener : listeners) {
      listener.generatorStarted(this, logPath);
    }
    pcs.firePropertyChange(STATE, false, true);
  }
  
  protected synchronized void publishStop() {
    Collection<ProcessInstanceListener> immutableListeners = new ArrayList<ProcessInstanceListener>(listeners);
    for(ProcessInstanceListener listener : immutableListeners) {
      listener.generatorStopped(this);
    }
    pcs.firePropertyChange(STATE, true, false);
  }
  
  protected OutputWriter getWriter() {
    return writer != null ? writer : NULLWRITER;
  }
  
  private void setFactory(final Engine factory) {
    pcs.firePropertyChange(FACTORY, this.factory, factory);
    this.factory = factory;
  }
}
