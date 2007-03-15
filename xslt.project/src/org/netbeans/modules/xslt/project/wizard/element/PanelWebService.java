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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;

import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.30
 */
final class PanelWebService<T> extends Panel<T> {
    
  PanelWebService(Project project, Panel<T> parent) {
    super(project, parent);
  }

  @Override
  protected String getError()
  {
    myFile = getWSDL();

    if (myFile == null) {
      return i18n("ERR_Web_Service_Is_Required"); // NOI18N
    }
    return null;
  }

  @Override
  protected Object getResult()
  {
    return Util.getWSDLModel(myFile);
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    // label
    c.gridy++;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(TINY_INSET, 0, TINY_INSET, 0);
    myFileLabel = createLabel(i18n("LBL_Web_Service_File")); // NOI18N
    panel.add(myFileLabel, c);

    // wsdl
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myWSDL = new JComboBox();
    myWSDL.setRenderer(new Renderer());
    myFileLabel.setLabelFor(myWSDL);
    panel.add(myWSDL, c);

    // [browse]
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(TINY_INSET, SMALL_INSET, TINY_INSET, 0);
    myBrowse = createButton(
      i18n("TLT_Browse_WSDL"), // NOI18N
      new AbstractAction(i18n("LBL_Browse_WSDL")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          printInformation( // todo m
            "Dialog will be implemented by xml team," + // NOI18N
            " see issue 93596."); // NOI18N
        }
      }
    );
//  panel.add(myBrowse, c);
    mainPanel.add(panel, cc);
    update();
  }

  private ItemListener createItemListener(final boolean existing) {
    return new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
        setEnabled(existing);
      }
    };
  }

  @Override
  protected void setEnabled(boolean enabled)
  {
    myWSDL.setEnabled(enabled);
    myBrowse.setEnabled(enabled);
    myFileLabel.setEnabled(enabled);
  }

  @Override
  protected void update()
  {
    myWSDL.removeAllItems();
    FileObject [] files = Util.getWSDLFiles(getProject());

    for (FileObject file : files) {
      myWSDL.addItem(file);
    }
  }

  private FileObject getWSDL() {
    return (FileObject) myWSDL.getSelectedItem();
  }

  private JButton myBrowse;
  private JComboBox myWSDL;
  private JLabel myFileLabel;
  private FileObject myFile;
}
