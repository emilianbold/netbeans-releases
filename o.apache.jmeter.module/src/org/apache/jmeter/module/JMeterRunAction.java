package org.apache.jmeter.module;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class JMeterRunAction extends CookieAction {
  
  protected void performAction(Node[] activatedNodes) {
    DataObject c = (DataObject) activatedNodes[0].getCookie(DataObject.class);
    try {
      File script = FileUtil.toFile(c.getPrimaryFile());
//      JMeterIntegrationEngine.getDefault().runTestPlan(script.getCanonicalPath());
      final String path = script.getCanonicalPath();
      
      EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
      Collection<Engine> loadgens = manager.findEngines(FileUtil.getExtension(path));
      if (loadgens.size() == 1) {
        Engine provider = loadgens.iterator().next();
        
        manager.startProcess(provider.createProcess(path));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected int mode() {
    return CookieAction.MODE_EXACTLY_ONE;
  }
  
  public String getName() {
    return NbBundle.getMessage(JMeterRunAction.class, "CTL_JMeterRunAction");
  }
  
  protected Class[] cookieClasses() {
    return new Class[] {
      DataObject.class
    };
  }
  
  protected void initialize() {
    super.initialize();
    // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
    putValue("noIconInMenu", Boolean.TRUE);
  }
  
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  protected boolean enable(Node[] node) {
    boolean retValue;
    if (node.length < 1)
      return false;
    
    retValue = super.enable(node);
    if (node == null || node[0] == null)
      return retValue;
    
    DataObject c = (DataObject) node[0].getCookie(DataObject.class);    
    FileObject primaryFile = c != null ? c.getPrimaryFile() : null;
    /* according to http://www.netbeans.org/issues/show_bug.cgi?id=94823 
     * using "c" without checking for NULL causes NPE randomly; this check should prevent NPE
     */
    if (primaryFile == null) return false;
    
    try {
      File script = FileUtil.toFile(primaryFile);
//      JMeterIntegrationEngine.getDefault().runTestPlan(script.getCanonicalPath());
      if (script == null) return false; // #183255 - asking for a non-existing file?
      final String path = script.getCanonicalPath();
      
      EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
      Collection<Engine> loadgens = manager.findEngines(FileUtil.getExtension(path));
      for(Engine provider : loadgens) {
        retValue = retValue && provider.isReady() && (provider.getProcessByName(path) == null || !provider.getProcessByName(path).isRunning()) ;
      }
    } catch (IOException ex) {}
    
    return retValue;
  }

  protected boolean asynchronous() {
    return false;
  }
  
  
}

