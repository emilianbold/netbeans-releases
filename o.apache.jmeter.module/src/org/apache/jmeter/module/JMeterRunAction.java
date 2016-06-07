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

