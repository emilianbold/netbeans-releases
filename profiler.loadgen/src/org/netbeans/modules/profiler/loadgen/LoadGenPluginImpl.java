/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.loadgen;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.loadgenerator.api.EngineManagerException;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.netbeans.modules.loadgenerator.spi.ProcessInstanceListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.LoadGenPlugin.class)
public class LoadGenPluginImpl implements LoadGenPlugin {

  private static final Logger LOGGER = Logger.getLogger(LoadGenPluginImpl.class.getName());

  private class RunningScript implements ProcessInstanceListener {

    private LoadGenPlugin.Callback callback;
    private WeakReference<ProcessInstance> processInstance;
    private long timeout;

    public RunningScript(LoadGenPlugin.Callback callback, ProcessInstance pi) {
      this.callback = callback;
      this.processInstance = new WeakReference<ProcessInstance>(pi);
      this.timeout = 0L;

      registerAsListener();
      runningScripts.add(this);
    }

    public void generatorStarted(ProcessInstance provider) {
      if (provider.equals(getProcessInstance())) {
        // stop timeout counter
        callback.afterStart(Result.SUCCESS);
      }
    }

    public void generatorStarted(ProcessInstance provider, String logPath) {
      if (provider.equals(getProcessInstance())) {
        // stop timeout counter
        callback.afterStart(Result.SUCCESS);
      }
    }

    public void generatorStopped(ProcessInstance provider) {
      if (provider.equals(getProcessInstance())) {
        callback.afterStop(Result.SUCCESS);
        cleanup();
      }
    }

    public void instanceInvalidated(ProcessInstance instance) {
      cleanup();
    }

    public ProcessInstance getProcessInstance() {
      return processInstance.get();
    }

    private void cleanup() {
      ProcessInstance pi = processInstance.get();
      if (pi != null) {
        pi.removeListener(this);
      }
      runningScripts.remove(this);
    }

    private void registerAsListener() {
      ProcessInstance pi = processInstance.get();
      if (pi != null) {
        pi.addListener(this);
      }
    }
  }

  private final Set<RunningScript> runningScripts;



  /** Creates a new instance of LoadGenPluginImpl */
  public LoadGenPluginImpl() {
    runningScripts = new HashSet<RunningScript>();
  }

  /**
   *
   * @param project
   * @return
   *
   * @Override
   */
  public Collection<FileObject> listScripts(Lookup.Provider project) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Collection<Engine> engines = manager.findEngines();

    if (project != null) {
        // Find all supported scripts in project folder
        Set<String> allExtensions = new HashSet<String>();
        for (Engine engine : engines) {
          allExtensions.addAll(engine.getSupportedExtensions());
        }
        return findScripts((Project)project, allExtensions);
    } else {
        // Return all registered scripts in Services | Load Generators
        List<FileObject> scripts = new ArrayList();
        for (Engine engine : engines) {
            List<? extends ProcessInstance> processes = engine.getProcesses();
            for (ProcessInstance process : processes) {
                String script = process.getCurrentScript();
                File f = script == null ? null : new File(script);
                FileObject fo = f == null ? null : FileUtil.toFileObject(f);
                if (fo != null) scripts.add(fo);
            }
        }
        return scripts;
    }
  }

  /**
   *
   * @param scriptPath
   * @return
   *
   * @Override
   */
  public void start(String scriptPath, LoadGenPlugin.Callback callback) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Collection<Engine> engines = manager.findEngines(FileUtil.getExtension(scriptPath));
    if (engines.isEmpty()) {
      callback.afterStart(Result.FAIL);
      return;
    }
    ProcessInstance newProcess = engines.iterator().next().createProcess(scriptPath);
    RunningScript l = new RunningScript(callback, newProcess);
    try {
      manager.startProcess(newProcess);
    } catch (EngineManagerException ex) {
      LOGGER.warning(ex.getMessage());
      callback.afterStart(Result.FAIL);
      l.instanceInvalidated(newProcess);
    }
  }

  /**
   *
   * @return
   *
   * @Override
   */
  public boolean isRunning() {
    return !runningScripts.isEmpty();
  }

  /**
   * @Override
   */
  public void stop() {
    Set<ProcessInstance> piSet = new HashSet<ProcessInstance>(runningScripts.size());
    for (RunningScript script : runningScripts) {
      piSet.add(script.getProcessInstance());
    }
    stopProcesses(piSet);
  }

  /**
   *
   * @param scriptPath
   *
   * @Override
   */
  public void stop(String scriptPath) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    try {
      //      callbackMap.put(scriptPath, callback);
      Collection<ProcessInstance> processes = manager.findProcesses(scriptPath);
      stopProcesses(processes);
    } catch (EngineManagerException ex) {
      LOGGER.warning(ex.getMessage());
    }
  }

  /**
   * @Override
   */
  public Set<String> getSupportedExtensions() {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    if (manager == null) {
      return new HashSet<String>();
    }
    Set<String> extensions = new HashSet<String>();
    for (Engine engine : manager.findEngines()) {
      extensions.addAll(engine.getSupportedExtensions());
    }
    return extensions;
  }

  private void stopProcesses(Collection<ProcessInstance> processes) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);

    for (ProcessInstance process : processes) {
      try {
        manager.stopProcess(process, true);
      } catch (EngineManagerException ex) {
        LOGGER.warning(ex.getMessage());
      }
    }
  }

  private static Collection<FileObject> findScripts(final Project project, final Set<String> extensions) {
    Collection<FileObject> scripts = new ArrayList<FileObject>();
    FileObject root = project.getProjectDirectory();
    Enumeration<? extends FileObject> children = root.getChildren(true);
    while (children.hasMoreElements()) {
      FileObject child = children.nextElement();
      if (child.isData()) {
        String extLow = child.getExt().toLowerCase();
        String extUp = extLow.toUpperCase();
        if (extensions.contains(extLow) || extensions.contains(extUp)) {
          scripts.add(child);
        }
      }
    }
    return scripts;
  }

  /**
   * Provides a list of source roots for the given project.
   *
   * @param project The project
   * @return an array of FileObjects that are the source roots for this project
   */
  private static FileObject[] getSourceRoots(final Project project) {
    return getSourceRoots(project, true);
  }

  /**
   * Provides a list of source roots for the given project.
   *
   * @param project The project
   * @param traverse Include subprojects
   * @return an array of FileObjects that are the source roots for this project
   */
  private static FileObject[] getSourceRoots(final Project project, final boolean traverse) {
    Set<FileObject> set = new HashSet<FileObject>();
    Set<Project> projects = new HashSet<Project>();

    projects.add(project);
    getSourceRoots(project, traverse, projects, set);
    return set.toArray(new FileObject[0]);
  }

  private static void getSourceRoots(final Project project, final boolean traverse, Set<Project> projects, Set<FileObject> roots) {
    final Sources sources = ProjectUtils.getSources(project);
    final SourceGroup[] sgs = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    for (int i = 0; i < sgs.length; i++) {
      roots.add(sgs[i].getRootFolder());
    }

    if (traverse) {
      // process possible subprojects
      //mkleint: see subprojectprovider for official contract, see #210465
      SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
      if (spp != null) {
        for (Iterator it = spp.getSubprojects().iterator(); it.hasNext();) {
          Project p = (Project) it.next();
          if (projects.add(p)) {
            getSourceRoots(p, traverse, projects, roots);
          }
        }
      }
    }
  }
}
