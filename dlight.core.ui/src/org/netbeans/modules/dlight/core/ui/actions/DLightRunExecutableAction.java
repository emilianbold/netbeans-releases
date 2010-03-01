/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.core.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.dlight.api.execution.DLightSessionConfiguration;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.api.support.NativeExecutableTarget;
import org.netbeans.modules.dlight.api.support.NativeExecutableTargetConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;
import org.netbeans.modules.dlight.core.ui.components.SelectExecutableTargetDialog;
import org.netbeans.modules.dlight.spi.impl.DLightServiceInfo;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author mt154047
 */
public class DLightRunExecutableAction extends AbstractAction {
  //DLightAction {

  protected SelectExecutableTargetDialog dialog = null;
  private DLightSessionHandler session;

  public DLightRunExecutableAction() {
    super(NbBundle.getMessage(DLightRunExecutableAction.class, "DLightRunExecutableAction.Name"), // NOI18N
            ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/core/ui/resources/runProject24.png", false)); // NOI18N
    //putValue(Action.SMALL_ICON, "org/netbeans/modules/dlight/core/ui/resources/runProject.png");
  }

  public void actionPerformed(ActionEvent e) {
    if (dialog == null) {
      dialog = new SelectExecutableTargetDialog();
    }

    //Frame frame = dtraceSession.getUIHandler().getFrame();
    Frame frame = WindowManager.getDefault().getMainWindow();
    dialog.show(frame);
    int res = dialog.getResult();

    if (res == SelectExecutableTargetDialog.RESULT_OK) {
      String pname = dialog.getProgramName();
      String pargs = dialog.getProgramArguments();
      String pdir = dialog.getWorkingDirectory();
      String[] args = pargs.split(" "); // NOI18N

      if (pname != null) {


        NativeExecutableTargetConfiguration targetConf = new NativeExecutableTargetConfiguration(
                pname,
                args,
                null);


        targetConf.setWorkingDirectory(pdir);
        targetConf.putInfo(DLightServiceInfo.DLIGHT_RUN, "true");//NOI18N
        // Setup simple output convertor factory...
        //targetConf.setOutConvertorFactory(new SimpleOutputConvertorFactory());

        DLightConfiguration configuration = DLightConfigurationManager.getInstance().getConfigurationByName("DLight");//NOI18N
//        DLightConfigurationOptions options = configuration.getConfigurationOptions(false);
        NativeExecutableTarget target = new NativeExecutableTarget(targetConf);


        //WE are here only when Profile On RUn
        DLightSessionConfiguration sessionConfiguration = new DLightSessionConfiguration();
        sessionConfiguration.setDLightTarget(target);
        sessionConfiguration.setDLightConfiguration(configuration);
        sessionConfiguration.setSessionName(pname);
        final Future<DLightSessionHandler> handle = DLightToolkitManagement.getInstance().createSession(sessionConfiguration);

        DLightExecutorService.submit(new Runnable() {

            public void run() {
                try {
                    session = handle.get();
                    DLightToolkitManagement.getInstance().startSession(session);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, "DLight Session for " + target.toString()); // NOI18N

//        if (startActionFactory != null) {
//          ExecutableTarget target = new SimpleExecutableTarget(pname, pargs);
//          target.setWorkingDirectory(pdir);
//          StartDLightAbstractAction startAction = startActionFactory.createAction(target);
//          startAction.actionPerformed(null);
//        }
      }

    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
