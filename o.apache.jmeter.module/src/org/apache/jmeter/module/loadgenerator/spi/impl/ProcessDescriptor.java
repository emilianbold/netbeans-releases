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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProcessDescriptor implements Cloneable {
  public static final String RUNNING = "running";
  private JMeterEngine engine = null;
  private String scriptPath = null;
  private String displayName = null;
  private boolean running = false;
  private int threadsCount = 0;
  private int interleave = 0;
  private int rampup = 0;
  private boolean nbReady = false;
  private String processName = "";
  
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  
  /**
   * Creates a new instance of ProcessDescriptor
   */
  public ProcessDescriptor(final JMeterEngine engine, final String scriptPath, final String displayName) {
    this(engine, scriptPath, displayName, false);
  }
  
  public ProcessDescriptor(final JMeterEngine engine, final String scriptPath, final String displayName, final boolean running) {
    this.engine = engine;
    this.scriptPath = scriptPath;
    this.displayName = displayName;
//    this.running = running;
  }

  public void start() throws JMeterEngineException {
    engine.runTest();
  }
  
  public void stop() {
    engine.stopTest();
  }
  
  public JMeterEngine getEngine() {
    return engine;
  }

  public String getScriptPath() {
    return scriptPath;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    pcs.firePropertyChange(RUNNING, this.running, running);
    this.running = running;
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public Object clone() {
    return new ProcessDescriptor(engine, scriptPath, displayName);
  }
  
  @Override
  public boolean equals(Object another) {
    if (another == null)
      return false;
    if (!(another instanceof ProcessDescriptor))
      return false;
    
    ProcessDescriptor desc = (ProcessDescriptor)another;
    
    return scriptPath.equals(desc.scriptPath);
  }

  public boolean isNbReady() {
    return nbReady;
  }

  public void setNbReady(boolean nbReady) {
    this.nbReady = nbReady;
  }

  public int getThreadsCount() {
    return threadsCount;
  }

  public void setThreadsCount(int threadsCount) {
    this.threadsCount = threadsCount;
  }
  
  public int getInterleave() {
    return interleave;
  }

  public void setInterleave(int interleave) {
    this.interleave = interleave;
  }

  public int getRampup() {
    return rampup;
  }

  public void setRampup(int rampup) {
    this.rampup = rampup;
  }

  @Override
  public int hashCode() {
    return scriptPath.hashCode();
  }
  
  public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
  }
  
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }
  
  public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }
  
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
