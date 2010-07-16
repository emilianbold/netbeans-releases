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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @author Vitaly Bychkov
 * @version 2007.02.01
 */
final class PanelProxy extends Panel {
    private static final long serialVersionUID = 1L;
    
  PanelProxy(
    Project project,
    Panel parent,
    Operation ourOperation,
    Operation partnerOperation)
  {
    super(project, parent);
    myOperationImplementPanel = new PanelTransformation(project, this, 
            ourOperation == null ? null : ourOperation.getInput(), 
            partnerOperation == null ? null : partnerOperation.getInput(), false, true);
    myOperationImplement = ourOperation;
    
    myOperationCallPanel = new PanelTransformation(project, this,
            partnerOperation == null ? null : partnerOperation.getOutput(), 
            ourOperation == null ? null : ourOperation.getOutput(), 
            false, false);
    myOperationCall = partnerOperation;
 }

  @Override
  protected String getComponentName()
  {
    return NAME_XSLT;
  }

    @Override
    protected void finishEditing() {
        if (myOperationImplementPanel != null) {
            myOperationImplementPanel.finishEditing();
        }
        if (myOperationCallPanel != null) {
            myOperationCallPanel.finishEditing();
        }
    }

  @Override
  protected String getError()
  {
    String separateErrors = getError(myOperationImplementPanel.getError(), myOperationCallPanel.getError());
    if (separateErrors != null) {
        return separateErrors;
    }

    String opParamError = checkOpParameters();
    if (opParamError != null) {
        return opParamError;
    }
    
    String generalError = checkUniqueTransformNames();
    if (generalError != null) {
        return generalError;
    }
    
    return generalError;
  }

  private String checkOpParameters() {
    String implOpName = myOperationImplement == null ? "" : myOperationImplement.getName();
    implOpName = implOpName == null ? "" : implOpName;

    String callOpName = myOperationCall == null ? "" : myOperationCall.getName();
    callOpName = callOpName == null ? "" : callOpName;
    
    if (!check(myOperationImplementPanel.getInput())) {
        return i18n( "ERR_Operation_With_Input_Is_Required" ,implOpName); // NOI18N
    }
    if (!check(myOperationImplementPanel.getOutput())) {
        return i18n( "ERR_Operation_With_Output_Is_Required" ,callOpName); // NOI18N
    }
    
    if (myOperationCallPanel.isTransformEnabled()) {
        if (!check(myOperationCallPanel.getInput())) {
            return i18n( "ERR_Operation_With_Input_Is_Required" ,callOpName); // NOI18N
        }
        if (!check(myOperationCallPanel.getOutput())) {
            return i18n( "ERR_Operation_With_Output_Is_Required" ,implOpName); // NOI18N
        }
    }
    return null;
  }
  
  private boolean check(OperationParameter parameter) {
    return
      parameter != null &&
      parameter.getMessage() != null &&
      parameter.getMessage().get() != null;
  }

  private String checkUniqueTransformNames() {
    List<TransformationItem> implDataModel = (List<TransformationItem>) myOperationImplementPanel.getResult();  
    List<TransformationItem> outDataModel = (List<TransformationItem>) myOperationCallPanel.getResult();  

    String result = null;
    if (implDataModel != null) {
        for (TransformationItem item : implDataModel) {
            result = isUniqueTransformName(outDataModel, item.getName());
            if (result != null) {
                return result;
            }
        }
    }
    
    if (outDataModel != null) {
        for (TransformationItem item : outDataModel) {
            result = isUniqueTransformName(implDataModel, item.getName());
            if (result != null) {
                return result;
            }
        }
    }
    
    return result;
  }

    @Override
    public void readSettings(WizardDescriptor descriptor) {
        super.readSettings(descriptor);
        myOperationCallPanel.readSettings(descriptor);
        myOperationImplementPanel.readSettings(descriptor);
    }
  
  @Override
  public void storeSettings(WizardDescriptor descriptor) {
    super.storeSettings(descriptor);
    myOperationImplementPanel.storeSettings(descriptor);
    myOperationCallPanel.storeSettings(descriptor);
    
    if (myOperationCallPanel.isTransformEnabled()) {
        descriptor.putProperty(CHOICE, CHOICE_FILTER_REQUEST_REPLY);
    } else {
        descriptor.putProperty(CHOICE, CHOICE_FILTER_ONE_WAY);
    }
    
    descriptor.putProperty(IMPL_OPERATION, myOperationImplement);
    descriptor.putProperty(CALLED_OPERATION, myOperationCall);
  }

  @Override
  protected void createPanel(JPanel mainPanel, GridBagConstraints cc)
  {
    JPanel panel = new JPanel(new GridBagLayout());
    
    GridBagConstraints c = new GridBagConstraints();
    c.gridy++;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;

    // we implement
    c.insets = new Insets(SMALL_SIZE,0, 0, 0);
    panel.add(createSeparator(""), c); // NOI18N

    c.gridy++;
    c.insets = new Insets(TINY_SIZE, 0, 0, 0);
    myOperationImplementPanel.createPanel(panel, c);

    // we call
    c.gridy++;
    c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
    panel.add(createSeparator(""), c); // NOI18N
    
    c.gridy++;
    c.insets = new Insets(TINY_SIZE, 0, 0, 0);
    myOperationCallPanel.createPanel(panel, c);

    mainPanel.add(panel, cc);
    mainPanel.getAccessibleContext().setAccessibleDescription(i18n("ACSD_LBL_NewBridgeService3"));   
    mainPanel.revalidate();
  }

  @Override
  protected void update()
  {
    myOperationImplementPanel.update();
    myOperationCallPanel.update();

    myOperationImplementPanel.setRequirement(
//      myOperationImplement.isTransformEnabled(), myOperationCall.isTransformEnabled());
      true, true);

    myOperationCallPanel.setRequirement(
//      myOperationImplement.isTransformEnabled(), myOperationCall.isTransformEnabled());
      myOperationCallPanel.isTransformEnabled(), myOperationCallPanel.isTransformEnabled());
  }

  private Operation myOperationImplement;
  private Operation myOperationCall;
  private PanelTransformation myOperationImplementPanel;
  private PanelTransformation myOperationCallPanel;
}
