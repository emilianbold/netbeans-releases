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
