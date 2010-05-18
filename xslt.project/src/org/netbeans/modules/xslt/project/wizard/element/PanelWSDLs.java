/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import javax.swing.JPanel;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.WizardDescriptor;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @author Vladimir Yaroslavskiy
 * @version 2007.02.02
 */
final class PanelWSDLs extends Panel {
    
  PanelWSDLs(Project project, Panel parent) {
    super(project, parent);
    myWebServiceImplement = new PanelXsltWebService(project, this);
    myWebServiceCall = new PanelWebService(project, this);
 }

  @Override
  protected String getComponentName()
  {
    return NAME_WSDL;
  }

  @Override
  protected Panel getNext()
  {
    return new PanelProxy(getProject(), this,
      (Operation) myWebServiceImplement.getResult(),
      (Operation) myWebServiceCall.getResult());
  }

  @Override
  public boolean isValid() {
      return !isInitializedUI || getError() == null;
  }
  
  @Override
  public void readSettings(WizardDescriptor object) {
    super.readSettings(object);
    myWebServiceImplement.readSettings(object);
    myWebServiceCall.readSettings(object);
  }

  @Override
  public void storeSettings(WizardDescriptor object) {
    super.storeSettings(object);
    myWebServiceImplement.storeSettings(object);
    myWebServiceCall.storeSettings(object);
  }

  @Override
  protected String getError() {
      String error = getError(myWebServiceImplement.getError(), myWebServiceCall.getError());
      if (error == null && isExistLongRunningError()) {
          error = getLongRunningError();
          setExistLongRunningError(error != null);
      }
      return error;
  }

    private String checkOperations() {
        Operation callOperation = myWebServiceCall.getWsdlOperation();
        Operation implOperation = myWebServiceImplement.getWsdlOperation();
        if (callOperation == null || implOperation == null) {
            return null;
        }
        return callOperation.equals(implOperation) 
                ? i18n("ERR_ReqursiveWsdlOperationInvocation") : null; // NOI18N
    }

    @Override
    protected String getLongRunningError() {
        String error = checkOperations();
        return error;
    }
    
  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc) {
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
    c.insets = new Insets(TINY_SIZE, HUGE_SIZE * 2, TINY_SIZE, 0);
    myWebServiceImplement.createPanel(panel, c);

    // we call
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createSeparator(i18n("LBL_We_Call")), c); // NOI18N

    c.gridy++;
    c.insets = new Insets(TINY_SIZE, HUGE_SIZE * 2, TINY_SIZE, 0);
    myWebServiceCall.createPanel(panel, c);

    mainPanel.add(panel, cc);
    mainPanel.getAccessibleContext().setAccessibleDescription(i18n("ACSD_LBL_NewBridgeService2"));
    
    isInitializedUI = true;    
  }

  private PanelWebService myWebServiceImplement;
  private PanelWebService myWebServiceCall;
  private boolean isInitializedUI = false;
}
