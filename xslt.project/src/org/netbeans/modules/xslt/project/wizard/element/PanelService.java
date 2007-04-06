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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
final class PanelService<T> extends Panel<T> {
    
  PanelService(Project project, Panel<T> parent, WSDLModel model) {
    super(project, parent);
    myOperation = new PanelOperation<T>(
      project, parent, model, getXslFileName(getXslFileNumber(1)), false, true);
  }

  @Override
  protected String getComponentName()
  {
    return NAME_XSLT;
  }

  @Override
  protected String getError()
  {
    return myOperation.getError();
  }

  public void storeSettings(Object object) {
    WizardDescriptor descriptor = (WizardDescriptor) object;
    myOperation.storeSettings(object);
    descriptor.putProperty(CHOICE, CHOICE_REQUEST_REPLY);
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    myOperation.createPanel(panel, c);
    mainPanel.add(panel, cc);
  }

  private Panel<T> myOperation;
}
