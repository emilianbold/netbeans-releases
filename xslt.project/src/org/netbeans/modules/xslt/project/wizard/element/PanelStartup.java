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

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.netbeans.api.project.Project;
import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
final class PanelStartup<T> extends Panel<T> {
    
  PanelStartup(Project project, Panel<T> parent) {
    super(project, parent);
    myTransformationPanel = new PanelWSDL<T>(getProject(), this);
    myProxyPanel = new PanelWSDLs<T>(getProject(), this);
  }

  @Override
  protected String getComponentName()
  {
    return NAME_TYPE;
  }

  @Override
  protected Panel<T> getNext()
  {
    if (myTransformation != null && myTransformation.isSelected()) {
      return myTransformationPanel;
    }
    else {
      return myProxyPanel;
    }
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    ButtonGroup group = new ButtonGroup();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 1.0;

    // (o) Request-Reply Service
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    myTransformation = createRadioButton(i18n("LBL_Transformation")); // NOI18N
    myTransformation.setSelected(true);
    panel.add(myTransformation, c);
    group.add(myTransformation);

    // text
    c.gridy++;
    c.insets = new Insets(
      SMALL_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    panel.add(createTextArea(TEXT_WIDTH, i18n("LBL_Transformation_Text")),c);//NOI18N

    // (o) Proxy Service
    c.gridy++;
    c.insets = new Insets(SMALL_INSET, 0, 0, 0);
    myProxy = createRadioButton(i18n("LBL_Proxy")); // NOI18N
    myProxy.setSelected(false);
    panel.add(myProxy, c);
    group.add(myProxy);

    // text
    c.gridy++;
    c.insets = new Insets(
      SMALL_INSET, MEDIUM_INSET + SMALL_INSET + TINY_INSET, TINY_INSET, 0);
    panel.add(createTextArea(TEXT_WIDTH, i18n("LBL_Proxy_Text")), c); // NOI18N

//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue));
    mainPanel.add(panel, cc);
  }

  private Panel<T> myTransformationPanel;
  private JRadioButton myTransformation;
  private Panel<T> myProxyPanel;
  private JRadioButton myProxy;
  private static final int TEXT_WIDTH = 40;
}
