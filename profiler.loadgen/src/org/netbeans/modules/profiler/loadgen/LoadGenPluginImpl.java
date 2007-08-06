

package org.netbeans.modules.profiler.loadgen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
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
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.netbeans.modules.loadgenerator.spi.ProcessInstanceListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
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
  public Collection<FileObject> listScripts(Project project) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Set<String> allExtensions = new HashSet<String>();
    Collection<Engine> engines = manager.findEngines();

    for (Engine engine : engines) {
      allExtensions.addAll(engine.getSupportedExtensions());
    }
    return findScripts(project, allExtensions);
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
