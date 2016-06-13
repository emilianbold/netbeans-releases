/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
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
