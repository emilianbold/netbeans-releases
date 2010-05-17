/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.apache.jmeter.module.loadgenerator.spi.impl;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.module.exceptions.InitializationException;
import org.apache.jmeter.module.integration.*;
import org.apache.jmeter.util.JMeterUtils;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.openide.ErrorManager;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterProcess extends ProcessInstance

{

  private AtomicBoolean runningState = new AtomicBoolean(Boolean.FALSE);

  private ProcessDescriptor runningProcess = null;
  private Semaphore runningProcessSemaphore = new Semaphore(1);


  private PropertyChangeListener processStateChangeListener = new PropertyChangeListener

  () {

    public void propertyChange(PropertyChangeEvent evt) {
      final boolean state = ((Boolean) evt.getNewValue()).booleanValue();

      try {
        runningProcessSemaphore.acquire();
        runningState.set(state);

        if (state) {
          getWriter().println("JMeter test plan running");
          publishStart(getEngine().getLogPath());
        } else {
          getWriter().println("JMeter test plan stopped");
          publishStop();
          runningProcess.removePropertyChangeListener(ProcessDescriptor.RUNNING, this);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        runningProcessSemaphore.release();
      }
    }
  };

  public JMeterProcess(final Engine factory) {
    super(factory);
  }

  public boolean isRunning() {
    return runningState.get();
  }

  public String getDisplayName() {
    if (getCurrentScript() == null) {
      return "";
    }
    String filename = getCurrentScript();
    filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
    return filename;
  }

  public Image getIcon() {
    return JMeterUtils.getImage("beaker.gif").getImage();
  }

  private void setRunning(final boolean value) {
    runningState.set(value);
  }

  private JMeterIntegrationEngine getEngine() {
    try {
      return JMeterIntegrationEngine.getDefault();
    } catch (InitializationException e) {
      ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
    }
    return null;
  }

  public void performStart(final String scriptFileName) {
    try {
      runningProcessSemaphore.acquire();
      if (runningState.compareAndSet(false, true)) {
        getWriter().reset();
        getWriter().print("Starting JMeter subsystem... ");
        runningProcess = getEngine().prepareTest(scriptFileName);

        getWriter().println("Done");
        if (runningProcess != null) {
          runningProcess.addPropertyChangeListener(ProcessDescriptor.RUNNING, processStateChangeListener);
          getEngine().clearLog();
          getWriter().println("Starting JMeter test plan named " + runningProcess.getDisplayName() + " (" + scriptFileName + ")");
          getWriter().println("Simulating " + runningProcess.getThreadsCount() + " users with ramp-up time of " + runningProcess.getRampup() + "s");
          runningProcess.start();
        } else {
          throw new JMeterEngineException("Can't start JMeter script " + scriptFileName);
        }
      }
    } catch (JMeterEngineException e) {
      ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getMessage());
    } catch (IOException e) {
      ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getMessage());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } finally {
      runningProcessSemaphore.release();
    }
  }

  public void performStop(final boolean force) {
    try {
      runningProcessSemaphore.acquire();
      if (runningProcess != null && runningState.compareAndSet(true, false)) {
        getWriter().println("Stopping JMeter test plan");
        runningProcess.stop();
      } else {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Stopping a non-running instance");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } finally {
      runningProcessSemaphore.release();
    }
  }
}
