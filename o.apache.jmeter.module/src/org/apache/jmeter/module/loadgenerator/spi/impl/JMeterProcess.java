/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
