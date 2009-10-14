/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.util.ProjectUtilities;
import org.netbeans.modules.xml.catalogsupport.util.ProjectWSDL;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.30
 */
final class PanelWebService<T> extends Panel<T> {
    
  PanelWebService(Project project, Panel<T> parent) {
    this(project, parent, null);
  }

  PanelWebService(Project project, Panel<T> parent, String alternativeLabel) {
    super(project, parent);
    myFileLabelString = alternativeLabel;
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
    return PanelUtil.getWSDLModel(myFile);
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
    c.insets = new Insets(TINY_SIZE, 0, TINY_SIZE, 0);
    if (myFileLabelString == null) {
        myFileLabelString = i18n("LBL_Web_Service_File");
    }
    myFileLabel = createLabel(myFileLabelString); // NOI18N
    a11y(myFileLabel, "ACSN_LBL_Web_Service_File", "ACSD_LBL_Web_Service_File");
    panel.add(myFileLabel, c);

    // wsdl
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    myWSDL = new JComboBox();
    myWSDL.setRenderer(new Renderer());
    myFileLabel.setLabelFor(myWSDL);
    panel.add(myWSDL, c);

    // [browse]
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
    myBrowse = createButton(
      new ButtonAction(
        i18n("LBL_Browse_WSDL"), // NOI18N
        i18n("TLT_Browse_WSDL")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          printInformation( // to do m
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
    List<ProjectWSDL> wsdls = ProjectUtilities.getProjectWSDLRecursively(getProject());

    for (ProjectWSDL wsdl : wsdls) {
      myWSDL.addItem(wsdl);
    }
  }

  private FileObject getWSDL() {
    if (myWSDL.getItemCount() == 0) {
      return null;
    }
    return ((ProjectWSDL) myWSDL.getSelectedItem()).getFile();
  }

  private String myFileLabelString;
  private JButton myBrowse;
  private JComboBox myWSDL;
  private JLabel myFileLabel;
  private FileObject myFile;
}
