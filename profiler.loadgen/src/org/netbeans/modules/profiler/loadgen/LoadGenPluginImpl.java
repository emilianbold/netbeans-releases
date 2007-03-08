

package org.netbeans.modules.profiler.loadgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.netbeans.modules.loadgenerator.spi.ProcessInstanceListener;
import org.openide.filesystems.FileUtil;

import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public class LoadGenPluginImpl implements LoadGenPlugin, ProcessInstanceListener {
  Set<ProcessInstance> runningProcesses;
  /** Creates a new instance of LoadGenPluginImpl */
  public LoadGenPluginImpl() {
    runningProcesses = new HashSet<ProcessInstance>();
  }
  
  /**
   *
   * @param project
   * @return
   */
  public Collection<FileObject> listScripts(Project project) {
    Collection<FileObject> scripts = new ArrayList<FileObject>();
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Set<String> allExtensions = new HashSet<String>();
    Collection<Engine> engines = manager.findEngines();
    
    for(Engine engine : engines) {
      allExtensions.addAll(engine.getSupportedExtensions());
    }
    return findScripts(project, allExtensions);
  }
  
  /**
   *
   * @param scriptPath
   * @return
   */
  public boolean start(String scriptPath) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Collection<Engine> engines = manager.findEngines(FileUtil.getExtension(scriptPath));
    if (engines.isEmpty()) return false;
    ProcessInstance newProcess = engines.iterator().next().createProcess(scriptPath);
    newProcess.addListener(this);
    manager.startProcess(newProcess);
    return true;
  }
  
  /**
   *
   * @return
   */
  public boolean isRunning() {
    return !runningProcesses.isEmpty();
  }
  
  /**
   *
   */
  public void stop() {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    for(ProcessInstance process : runningProcesses) {
      manager.stopProcess(process, true);
    }
  }
  
  /**
   *
   * @param scriptPath
   */
  public void stop(String scriptPath) {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    for(ProcessInstance process : runningProcesses) {
      if (process.getCurrentScript().equalsIgnoreCase(scriptPath)) {
        manager.stopProcess(process, true);
      }
    }
  }
  
  private static Collection<FileObject> findScripts(final Project project, final Set<String> extensions) {
    Collection<FileObject> scripts = new ArrayList<FileObject>();
    for(FileObject root : getSourceRoots(project)) {
      Enumeration<? extends FileObject> children = root.getChildren(true);
      while(children.hasMoreElements()) {
        FileObject child = children.nextElement();
        if (child.isData()) {
          String extLow = child.getExt().toLowerCase();
          String extUp = extLow.toUpperCase();
          if (extensions.contains(extLow) || extensions.contains(extUp)) {
            scripts.add(child);
          }
        };
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
    Set/*<FileObject>*/ set = new HashSet/*<FileObject>*/ ();
    Set/*<Project>*/ projects = new HashSet();
    
    projects.add(project);
    getSourceRoots(project,traverse,projects,set);
    return (FileObject[])set.toArray(new FileObject[0]);
  }
  
  private static void getSourceRoots(final Project project, final boolean traverse, Set projects, Set roots) {
    final Sources sources = ProjectUtils.getSources(project);
    final SourceGroup[] sgs = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    for (int i = 0; i < sgs.length; i++) {
      roots.add(sgs[i].getRootFolder());
    }
    
    if (traverse) {
      // process possible subprojects
      SubprojectProvider spp = (SubprojectProvider)project.getLookup().lookup(SubprojectProvider.class);
      if (spp != null) {
        for (Iterator it = spp.getSubprojects().iterator(); it.hasNext();) {
          Project p = (Project)it.next();
          if (projects.add(p)) {
            getSourceRoots(p, traverse,projects,roots);
          }
        }
      }
    }
  }
  
  public void generatorStarted(ProcessInstance provider) {
    runningProcesses.add(provider);
  }
  
  public void generatorStarted(ProcessInstance provider, String logPath) {
    runningProcesses.add(provider);
  }
  
  public void generatorStopped(ProcessInstance provider) {
    provider.removeListener(this);
    runningProcesses.remove(provider);
  }
  
  public void instanceInvalidated(ProcessInstance instance) {
    instance.removeListener(this);
    runningProcesses.remove(instance);
  }
}
