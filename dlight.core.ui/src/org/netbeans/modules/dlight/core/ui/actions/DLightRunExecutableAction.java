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
import javax.swing.AbstractAction;
import org.netbeans.modules.dlight.core.ui.components.SelectExecutableTargetDialog;
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

  public DLightRunExecutableAction() {
    super(NbBundle.getMessage(DLightRunExecutableAction.class, "DLightRunExecutableAction.Name"), // NOI18N
            ImageUtilities.loadImageIcon("org/netbeaans/modules/dlight/core/ui/resources/indicators_small.png", false)); // NOI18N
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

      if (pname != null) {

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
