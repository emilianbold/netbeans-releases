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
import java.util.ArrayList;
import java.util.Collection;
import org.apache.jmeter.module.integration.*;
import org.apache.jmeter.util.JMeterUtils;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterEngine extends Engine {
  private static final Collection<String> EXTENSIONS = new ArrayList<String>();
  
  static {
    EXTENSIONS.add("jmx");
  }
  
  public Collection<String> getSupportedExtensions() {
    return EXTENSIONS;
  }
  
  public synchronized ProcessInstance prepareInstance(final String scriptPath) {
    ProcessInstance instance = new JMeterProcess(this);
    return instance;
  }
  
  public Image getIcon() {
    return JMeterUtils.getImage("feather.gif").getImage();
  }
  
  public String getDisplayName() {
    return "JMeter";
  }
  
  public boolean isReady() {
    return true;
  }
}
