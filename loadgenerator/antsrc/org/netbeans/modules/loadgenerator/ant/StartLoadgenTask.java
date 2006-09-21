/*
 * TestTask.java
 *
 * Created on July 29, 2006, 11:33 PM
 */

package org.netbeans.modules.loadgenerator.ant;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

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
    // To log something:
    // log("Some message");
    // log("Serious message", Project.MSG_WARN);
    // log("Minor message", Project.MSG_VERBOSE);
    
    // To signal an error:
    // throw new BuildException("Problem", location);
    // throw new BuildException(someThrowable, location);
    // throw new BuildException("Problem", someThrowable, location);
    
    // You can call other tasks too:
    // Zip zip = (Zip)project.createTask("zip");
    // zip.setZipfile(zipFile);
    // FileSet fs = new FileSet();
    // fs.setDir(baseDir);
    // zip.addFileset(fs);
    // zip.init();
    // zip.setLocation(location);
    // zip.execute();
  }
  
}
