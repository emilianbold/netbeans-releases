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

package org.apache.jmeter.module.nodes;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.module.cookies.JMeterCookie;
import org.apache.jmeter.module.cookies.JMeterVisualizerCookie;
import org.apache.jmeter.module.exceptions.InitializationException;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterElementNode extends AbstractNode {
  private List<TestElement> elements;
  private TestElement rootElement;
  private String testPlan;
  private JMeterIntegrationEngine integration;
  
  private Component customizer;
  
  public JMeterElementNode(final TestElement root, final HashTree tree) {
    super(new JMeterElementChildren(tree));
    try {
      integration = JMeterIntegrationEngine.getDefault();
      rootElement = root;
    } catch (InitializationException e) {
      integration = null;
      rootElement = null;
    }
    getCookieSet().add(new JMeterCookie(root));
    if (root instanceof ResultCollector) {
      getCookieSet().add(new JMeterVisualizerCookie(root));
    }
  }
  
  public String getName() {
    return rootElement != null ? rootElement.getPropertyAsString(TestElement.NAME) : "...";
  }
  
  public String getDisplayName() {
    return getName();
  }
  
  public Component getCustomizer() {
    customizer.addPropertyChangeListener("ancestor", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() == null) {
          final String oldName = rootElement.getPropertyAsString(TestElement.NAME);
          ((JMeterGUIComponent)customizer).modifyTestElement(rootElement);
          fireDisplayNameChange(oldName, rootElement.getPropertyAsString(TestElement.NAME));
          customizer.removePropertyChangeListener("ancestor", this);
        }
      }
    });
    ((JMeterGUIComponent)customizer).configure(rootElement);
    return customizer;
  }
  
  public boolean hasCustomizer() {
    customizer = integration.getElementCustomizer(rootElement);
    if (customizer == null) {
      customizer = super.getCustomizer();
    }
    
    return customizer != null;
  }
  
  public Image getOpenedIcon(int i) {
    return integration.getElementIcon(rootElement);
  }
  
  public Image getIcon(int i) {
    return integration.getElementIcon(rootElement);
  }
}
