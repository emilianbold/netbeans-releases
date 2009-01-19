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

package org.netbeans.dlight.webstack.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.execution.api.DLightTargetListener;
import org.netbeans.modules.nativeexecution.ExecutionEnvironment;

/**
 * Runs script in background which will look for apache installation and
 * php source root installation
 * @author mt154047
 */
public class DeploymentPlatformTarget implements DLightTarget {

  private boolean isStarted = false;
  private List<DLightTargetListener> listeners = Collections.synchronizedList(new ArrayList<DLightTargetListener>());

  public DeploymentPlatformTarget() {
  }

  public void addTargetListener(DLightTargetListener listener) {
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void removeTargetListener(DLightTargetListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  public void start() {
    //we will startdemon task, which will search for everything
    //needed for WebStack
    isStarted = true;
    DLightTargetListener[] ls = listeners.toArray(new DLightTargetListener[0]);
    for (DLightTargetListener l : ls) {
      l.targetStarted(this);
    }
  }

  public void terminate() {
    //  throw new UnsupportedOperationException("Not supported yet.");
    isStarted = false;
    DLightTargetListener[] ls = listeners.toArray(new DLightTargetListener[0]);
    for (DLightTargetListener l : ls) {
      l.targetFinished(this, 0);
    }
  }

  public State getState() {
    return isStarted ? State.RUNNING : State.STOPPED;
  }

  public boolean canBeSubstituted() {
    return false;
  }

  public void substitute(String cmd, String[] args) {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  public ExecutionEnvironment getExecEnv() {
    String user_name = System.getProperty("dlight.webstack.user", "masha");
    String host_name = System.getProperty("dlight.webstack.host", "localhost");
    int port_number = Integer.valueOf(System.getProperty("dlight.webstack.port_number", "2222"));
   // return new ExecutionEnvironment("masha", "129.159.126.238",  2222);
     return new ExecutionEnvironment(user_name, host_name,  port_number);
  }
}
