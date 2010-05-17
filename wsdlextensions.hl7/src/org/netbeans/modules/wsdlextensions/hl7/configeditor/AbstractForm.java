/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.Form.FormModel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.AbstractPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.SelectableOperationPanel;

/**
 *
 * @author Vishnuvardhan P.R
 * @author Jun Qian
 */
public class AbstractForm extends AbstractPanel {

    private final Model model;

    public AbstractForm(final Model model, String templateConst) {
        super(templateConst);
        this.model = model;  
    }
    
    public Model getModel(){
        return this.model;
    }

    public void syncAbstractPanel_ToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException{
        if (!(destModel instanceof AbstractForm.Model)) {
            return;
        }
        if (!(srcModel instanceof AbstractForm.Model)) {
            return;
        }
        AbstractForm.Model dest = (AbstractForm.Model) destModel;
        AbstractForm.Model src = (AbstractForm.Model) srcModel;
        
        dest.setOperationNames(src.getOperationNames());
        dest.setMessageTypes(src.getMessageTypes());
        dest.setInputEOTs(src.getInputEOTs());
        dest.setOutputEOTs(src.getOutputEOTs());
    }

    public HL7Error validateMe() {
        HL7Error error = new HL7Error();
        return error;
    }

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    public void commit() {
        if (!SwingUtilities.isEventDispatchThread()) {
            Utils.dispatchToSwingThread("commit()", new Runnable() {
                public void run() {
                    commit();
                }
            });
            return;
        }

        List<String> opNames = new ArrayList<String>();
        List<ElementOrType> inputEOTs = new ArrayList<ElementOrType>();
        List<ElementOrType> outputEOTs = new ArrayList<ElementOrType>();
        List<String> msgTypes = new ArrayList<String>();

        for (SelectableOperationPanel opPanel : getSelectableOperationPanels()) {
            opNames.add(opPanel.getOperationName());
            msgTypes.add(opPanel.getMessageType());
            inputEOTs.add(opPanel.getRequestEOT());
            outputEOTs.add(opPanel.getResponseEOT());
        }

        model.setOperationNames(opNames);
        model.setMessageTypes(msgTypes);
        model.setInputEOTs(inputEOTs);
        model.setOutputEOTs(outputEOTs);
    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        List<String> getOperationNames();
        void setOperationNames(List<String> opNames);

        List<String> getMessageTypes();
        void setMessageTypes(List<String> msgTypes);
        
        List<ElementOrType> getInputEOTs();
        void setInputEOTs(List<ElementOrType> inputEOT);
        
        List<ElementOrType> getOutputEOTs();
        void setOutputEOTs(List<ElementOrType> outputEOT);
    }
}
