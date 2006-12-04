package org.netbeans.modules.loadgenerator;

import org.netbeans.modules.loadgenerator.nodes.ManagerNode;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {  
  @Override
  public void restored() {
    // By default, do nothing.
    // Put your startup code here.
    ManagerNode.getInstance().setEngineLookup(Lookup.getDefault().lookupResult(Engine.class));
  }
  
  @Override
  public void uninstalled() {
    ManagerNode.getInstance().setEngineLookup(null);
  }
}
