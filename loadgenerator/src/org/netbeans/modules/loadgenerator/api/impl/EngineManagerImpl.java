/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.loadgenerator.api.impl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.api.EngineManagerException;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.loadgenerator.api.EngineManager.class)
public class EngineManagerImpl implements EngineManager, ProcessInstanceListener {

  private static final Logger LOGGER = Logger.getLogger(EngineManagerImpl.class.getName());

  private static EngineManager instance = null;

  private final Map<ProcessInstance, List<ProgressHandle>> prgrsHandles = Collections.synchronizedMap(new HashMap<ProcessInstance, List<ProgressHandle>>());
  private final ManagerOutputWindowRegistry registry = ManagerOutputWindowRegistry.getDefault();

  private final Collection<ProcessInstance> runningInstances;
  private static String lastUsedScript = null;

  public void generatorStarted(ProcessInstance provider) {
    try {
      finishHandle(provider);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!runningInstances.contains(provider)) {
      runningInstances.add(provider);
    }
  }

  public void generatorStarted(final ProcessInstance provider, final String logPath) {
    try {
      if (prgrsHandles == null || provider == null) {
        return;
      }
      finishHandle(provider);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!runningInstances.contains(provider)) {
      runningInstances.add(provider);
    }
  }

  public void generatorStopped(final ProcessInstance provider) {
    try {
      finishHandle(provider);
    } catch (Exception e) {
      e.printStackTrace();
    }

    provider.removeListener(this);
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
    for (Engine provider : result.allInstances()) {
      providers.add(provider);
    }

    return providers;
  }

  public Collection<Engine> findEngines(final String extension) {
    Collection<Engine> providers = new ArrayList<Engine>();
    Collection<? extends Engine> result = Lookup.getDefault().lookupAll(Engine.class);

    for (Engine provider : result) {
      if (provider.getSupportedExtensions().contains(extension)) {
        providers.add(provider);
      }
    }

    return providers;
  }

  // @throws EngineManagerException
  public void startProcess(final ProcessInstance instance) throws EngineManagerException {
    if (instance.isRunning()) {
      throw new EngineManagerException(MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/api/impl/Bundle").getString("ProviderBusyMessage"),  instance.getDisplayName()));
    }

    ProgressHandle phandle = ProgressHandleFactory.createHandle(java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/api/impl/Bundle").getString("Starting_load_generator"), new Cancellable

    () {

      public boolean cancel() {
        if (prgrsHandles == null || instance == null) {
          return true;
        }
        try {
          stopProcess(instance, true);
        } catch (EngineManagerException ex) {
          LOGGER.warning(ex.getMessage());
        }
        return true;
      }
    });

    try {
      phandle.setInitialDelay(0);
      phandle.start();
      phandle.switchToIndeterminate();

      storeHandle(instance, phandle);

      instance.addListener(this);

      // open the main management window
      ManagerOutputWindow mngrWin = registry.open(instance);

      instance.start();
    } catch (Exception e) {
      phandle.finish();
      prgrsHandles.remove(phandle);
    }
    lastUsedScript = instance.getCurrentScript();
  }

  // @throws EngineManagerException
  public ProcessInstance startNewProcess(final Engine provider) throws EngineManagerException {
    ProcessInstance runnableInstance = null;

    final JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileFilter(new FileFilter

    () {

      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }
        return provider.getSupportedExtensions().contains(FileUtil.getExtension(f.getAbsolutePath()));
      }

      public String getDescription() {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/api/impl/Bundle").getString("Supported_scripts");
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

  // @throws EngineManagerException
  public void stopProcess(final ProcessInstance provider, final boolean force) throws EngineManagerException {
    if (!provider.isRunning()) {
      throw new EngineManagerException(MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/api/impl/Bundle").getString("Stopping_a_non-running_provider"), provider.getDisplayName()));
    }

    ProgressHandle phandle = ProgressHandleFactory.createHandle(java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/api/impl/Bundle").getString("Stopping_load_generator"));
    try {
      phandle.setInitialDelay(0);
      phandle.start();
      phandle.switchToIndeterminate();

      storeHandle(provider, phandle);

      provider.stop(force);
    } catch (Exception e) {
      phandle.finish();
      prgrsHandles.remove(phandle);
    }
  }

  // @throws EngineManagerException
  public void stopProcess(final String scriptPath, final boolean force) throws EngineManagerException {
    Collection<ProcessInstance> processes = findProcesses(scriptPath);
    for (ProcessInstance instance : processes) {
      stopProcess(instance, force);
    }
  }

  public Collection<ProcessInstance> findProcesses(final String scriptPath) throws EngineManagerException {
    Collection<ProcessInstance> processes = new ArrayList<ProcessInstance>();
    for (ProcessInstance instance : runningInstances) {
      if (instance.getCurrentScript().equals(scriptPath)) {
        processes.add(instance);
      }
    }
    return processes;
  }

  private void storeHandle(ProcessInstance provider, ProgressHandle handle) {
    List<ProgressHandle> handles = prgrsHandles.get(provider);
    if (handles == null) {
      handles = new ArrayList<ProgressHandle>();
      prgrsHandles.put(provider, handles);
    }
    handles.add(handle);
  }

  private void finishHandle(ProcessInstance provider) {
    List<ProgressHandle> handles = prgrsHandles.get(provider);
    if (handles != null && !handles.isEmpty()) {
      handles.get(0).finish();
      handles.remove(0);
      if (handles.isEmpty()) {
        prgrsHandles.remove(provider);
      }
    }
  }
}
