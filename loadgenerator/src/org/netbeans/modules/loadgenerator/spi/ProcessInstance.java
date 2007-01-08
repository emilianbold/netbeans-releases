package org.netbeans.modules.loadgenerator.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.loadgenerator.api.impl.ManagerOutputWindowRegistry;
import org.netbeans.modules.loadgenerator.utils.NullOutputWriter;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
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
    isModifiedFlag = false;
    isDeletedFlag = false;
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
    setCurrentScript(scriptFileName);
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
    if (currentScriptFile != null) {
      currentScriptFile.removeFileChangeListener(fcl);
      currentScriptFile = null;
    }
    if (value != null) {
      currentScriptFile = FileUtil.toFileObject(new File(value));
      currentScriptFile.addFileChangeListener(fcl);
    }
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
  
  public boolean isModified() {
    return isModifiedFlag;
  }
  
  public boolean isDeleted() {
    return isDeletedFlag;
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
  private FileObject currentScriptFile;
  private OutputWriter writer;
  private Engine factory;
  private boolean isNewFlag = true, isModifiedFlag = false, isDeletedFlag = false;
  
  final private FileChangeListener fcl = new FileChangeListener() {
    public void fileFolderCreated(FileEvent fe) {
      // IGNORE
    }
    
    public void fileDataCreated(FileEvent fe) {
      // IGNORE
    }
    
    public void fileChanged(FileEvent fe) {
      setModified();
    }
    
    public void fileDeleted(FileEvent fe) {
      fe.getFile().removeFileChangeListener(this);
      currentScriptFile = null;
      currentScript = null;
      if (!isRunning()) {
        ManagerOutputWindowRegistry.getDefault().close(ProcessInstance.this);
      } else {
        setDeleted();
      }
    }
    
    public void fileRenamed(FileRenameEvent fe) {
      currentScript = fe.getFile().getPath();
      if (!isRunning()) {
        ManagerOutputWindowRegistry.getDefault().close(ProcessInstance.this);
        ManagerOutputWindowRegistry.getDefault().open(ProcessInstance.this);
      } else {
        setModified();
      }
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
      // IGNORE
    }
  };
  
  
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
  
  protected synchronized void publishInvalidated() {
    Collection<ProcessInstanceListener> immutableListeners = new ArrayList<ProcessInstanceListener>(listeners);
    for(ProcessInstanceListener listener : immutableListeners) {
      listener.instanceInvalidated(this);
    }
  }
  
  protected OutputWriter getWriter() {
    return writer != null ? writer : NULLWRITER;
  }
  
  private void setFactory(final Engine factory) {
    pcs.firePropertyChange(FACTORY, this.factory, factory);
    this.factory = factory;
  }
  
  private void setModified() {
    isModifiedFlag = true;
    publishInvalidated();
  }
  
  private void setDeleted() {
    isDeletedFlag = true;
    isModifiedFlag = false;
    publishInvalidated();
  }
}
