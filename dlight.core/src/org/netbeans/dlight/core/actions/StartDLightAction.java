/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.dlight.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.execution.api.NativeExecutableTarget;
import org.netbeans.modules.dlight.execution.api.NativeExecutableTargetConfiguration;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;

public final class StartDLightAction implements ActionListener {

  public void actionPerformed(ActionEvent e) {
    DLightLogger.instance.info("StartDLightAction performed @ " + System.currentTimeMillis());
     String application = System.getProperty("dlight.application", "/export/home/Welcome/welcome");
    String[] arguments = System.getProperty("dlight.application.params", "1 2 3").split("[ \t]+");
    String[] environment = new String[]{};
    DLightLogger.instance.info("Set D-Light target! Application " + application);
      NativeExecutableTargetConfiguration conf = new NativeExecutableTargetConfiguration(application, arguments, environment);
    conf.setHost("localhost");
    conf.setSSHPort(2222);
    conf.setUser("masha");
    DLightTarget target = new NativeExecutableTarget(conf);
    DLightSession session = DLightManager.getDefault().createSession(target, "Gizmo");
    DLightManager.getDefault().startSession(session);
  }
}
