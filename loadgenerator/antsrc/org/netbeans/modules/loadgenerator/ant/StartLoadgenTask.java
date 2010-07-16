package org.netbeans.modules.loadgenerator.ant;

import java.util.Collection;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * @author Jaroslav Bachorik
 */
public class StartLoadgenTask extends Task {
  private String scriptPath;
  
  public void setPath(final String value) {
    scriptPath = value;
  }
  
  public void execute() throws BuildException {
    try {
      FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
      System.out.println("FileObject: " + fob.getPath());
      Lookup lookup = FileOwnerQuery.getOwner(fob).getLookup();
      EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
      
      Collection<Engine> engines = manager.findEngines(FileUtil.getExtension(scriptPath));
      for(Engine engine : engines) {
        manager.startProcess(engine.createProcess(scriptPath));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
