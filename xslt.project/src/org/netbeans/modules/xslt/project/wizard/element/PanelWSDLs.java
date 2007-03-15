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
import java.awt.Insets;
import javax.swing.JPanel;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.02.02
 */
final class PanelWSDLs<T> extends Panel<T> {
    
  PanelWSDLs(Project project, Panel<T> parent) {
    super(project, parent);
    myWebServiceImplement = new PanelWebService<T>(project, parent);
    myWebServiceCall = new PanelWebService<T>(project, parent);
 }

  @Override
  protected String getComponentName()
  {
    return NAME_WSDL;
  }

  @Override
  protected Panel<T> getNext()
  {
    return new PanelProxy<T>(getProject(), this,
      (WSDLModel) myWebServiceImplement.getResult(),
      (WSDLModel) myWebServiceCall.getResult());
  }

  @Override
  protected String getError()
  {
    return getError(myWebServiceImplement.getError(), myWebServiceCall.getError());
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;

    // we implement
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createSeparator(i18n("LBL_We_Implement")), c); // NOI18N

    c.gridy++;
    c.insets = new Insets(
      TINY_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    myWebServiceImplement.createPanel(panel, c);

    // we call
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createSeparator(i18n("LBL_We_Call")), c); // NOI18N

    c.gridy++;
    c.insets = new Insets(
      TINY_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    myWebServiceCall.createPanel(panel, c);

    mainPanel.add(panel, cc);
  }

  private Panel<T> myWebServiceImplement;
  private Panel<T> myWebServiceCall;
}
