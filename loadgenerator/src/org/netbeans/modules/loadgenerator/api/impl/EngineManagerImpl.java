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
 */

package org.netbeans.modules.loadgenerator.api.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.netbeans.modules.loadgenerator.spi.ProcessInstanceListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public class EngineManagerImpl implements EngineManager, ProcessInstanceListener {
  private static EngineManager instance = null;
  
  final private Map<ProcessInstance, ProgressHandle> prgrsHandles = Collections.synchronizedMap(new HashMap<ProcessInstance, ProgressHandle>());
  final private ManagerOutputWindowRegistry registry = ManagerOutputWindowRegistry.getDefault();
  
  private Collection<ProcessInstance> runningInstances;
  private static String lastUsedScript = null;
  
  public void generatorStarted(ProcessInstance provider) {
    try {
      prgrsHandles.get(provider).finish();
      prgrsHandles.remove(provider);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (!runningInstances.contains(provider)) {
      runningInstances.add(provider);
    }
    
  }
  public void generatorStarted(final ProcessInstance provider, final String logPath) {
    try {
      prgrsHandles.get(provider).finish();
      prgrsHandles.remove(provider);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (!runningInstances.contains(provider)) {
      runningInstances.add(provider);
    }
  }
  
  public void generatorStopped(final ProcessInstance provider) {
    try {
      ProgressHandle ph = prgrsHandles.get(provider);
      if (ph != null) {
        ph.finish();
        prgrsHandles.remove(provider);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    ((ProcessInstance)provider).removeListener(this);
    runningInstances.remove(provider);
    
    ManagerOutputWindow lgmgrWin = registry.find(provider);
    if (lgmgrWin != null) {
      lgmgrWin.detach(provider);
    }
  }
  
  
  public void instanceInvalidated(ProcessInstance instance) {
    if (lastUsedScript != null) {
      lastUsedScript = instance.getCurrentScript();
    }
    if (!instance.isRunning()) {
      registry.close(instance);
    }
  }
  
  /**
   * Creates a new instance of EngineManagerImpl
   */
  public EngineManagerImpl() {
    runningInstances = new ArrayList<ProcessInstance>();
  }
  
  public Collection<Engine> findEngines() {
    Collection<Engine> providers = new ArrayList<Engine>();
    Lookup.Result<Engine> result = Lookup.getDefault().lookupResult(Engine.class);
    for(Engine provider : result.allInstances()) {
      providers.add(provider);
    }
    
    return providers;
  }
  
  public Collection<Engine> findEngines(final String extension) {
    Collection<Engine> providers = new ArrayList<Engine>();
    Collection<? extends Engine> result = Lookup.getDefault().lookupAll(Engine.class);
    
    for(Engine provider : result) {
      if (provider.getSupportedExtensions().contains(extension)) {
        providers.add(provider);
      }
    }
    
    return providers;
  }
  
  public void startProcess(final ProcessInstance instance) {
    if (instance.isRunning()) {
      ErrorManager.getDefault().notify(ErrorManager.ERROR, new Throwable("Provider is busy"));
      return;
    }
    
    ProgressHandle phandle = ProgressHandleFactory.createHandle("Starting load generator", new Cancellable() {
      public boolean cancel() {
        prgrsHandles.get(instance).finish();
        stopProcess(instance, true);
        return true;
      }
    });
    
    phandle.setInitialDelay(0);
    phandle.start();
    phandle.switchToIndeterminate();
    
    prgrsHandles.put(instance, phandle);
    
    ((ProcessInstance)instance).addListener(this);
    
    // open the main management window
    ManagerOutputWindow mngrWin = registry.open(instance);
    
    instance.start();
    lastUsedScript = instance.getCurrentScript();
  }
  
  public ProcessInstance startNewProcess(final Engine provider) {
    ProcessInstance runnableInstance = null;
    
    final JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileFilter(new FileFilter() {
      public boolean accept(File f) {
        if (f.isDirectory())
          return true;
        return provider.getSupportedExtensions().contains(FileUtil.getExtension(f.getAbsolutePath()));
      }
      public String getDescription() {
        return "Supported scripts";
      }
    });
    if (lastUsedScript != null) {
      chooser.setCurrentDirectory(new File(lastUsedScript));
    }
    int retValue = chooser.showOpenDialog(null);
    if (retValue == JFileChooser.APPROVE_OPTION) {
      try {
        runnableInstance = provider.createProcess(chooser.getSelectedFile().getCanonicalPath());
        startProcess(runnableInstance);
      } catch (IOException ex) {
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
      }
    }
    
    return runnableInstance;
  }
  
  public void stopProcess(final ProcessInstance provider, final boolean force) {
    if (!provider.isRunning()) {
      ErrorManager.getDefault().notify(ErrorManager.WARNING, new Throwable("Stopping a non-running provider"));
    }
    
    ProgressHandle phandle = ProgressHandleFactory.createHandle("Stopping load generator");
    phandle.setInitialDelay(0);
    phandle.start();
    phandle.switchToIndeterminate();
    
    prgrsHandles.put(provider, phandle);
    
    provider.stop(force);
  }
}
