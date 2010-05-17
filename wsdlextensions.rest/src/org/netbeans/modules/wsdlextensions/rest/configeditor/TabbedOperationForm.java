/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.rest.configeditor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wsdlextensions.rest.RESTMethod;
import org.netbeans.modules.wsdlextensions.rest.configeditor.Form.FormModel;
import org.netbeans.modules.wsdlextensions.rest.configeditor.panels.SelectableOperationPanel;
import org.netbeans.modules.wsdlextensions.rest.configeditor.panels.TabbedOperationPanel;

/**
 *
 * @author jqian
 */
public class TabbedOperationForm extends TabbedOperationPanel {

    private final Model model;

    public TabbedOperationForm(final Model model, String templateConst) {
        super(templateConst);
        this.model = model;
    }

    public Model getModel(){
        return this.model;
    }

    public void syncTabbedOperationPanel_ToFrom(Form.FormModel destModel, Form.FormModel srcModel)
            throws ModelModificationException{
        if (!(destModel instanceof TabbedOperationForm.Model)) {
            return;
        }
        if (!(srcModel instanceof TabbedOperationForm.Model)) {
            return;
        }
        TabbedOperationForm.Model dest = (TabbedOperationForm.Model) destModel;
        TabbedOperationForm.Model src = (TabbedOperationForm.Model) srcModel;

        for (RESTMethod method : RESTMethod.values()) {
            dest.setOperationNames(src.getOperationNames(method), method);
            dest.setInputEOTs(src.getInputEOTs(method), method);
            dest.setOutputEOTs(src.getOutputEOTs(method), method);
            dest.setProperties(src.getProperties(method), method);
        }
    }

    public RESTError validateMe() {
        RESTError error = new RESTError();
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

        for (RESTMethod method : RESTMethod.values()) {

            List<String> opNames = new ArrayList<String>();
            List<ElementOrType> inputEOTs = new ArrayList<ElementOrType>();
            List<ElementOrType> outputEOTs = new ArrayList<ElementOrType>();
            List<EditableProperties> properties = new ArrayList<EditableProperties>();

            List<SelectableOperationPanel> opPanels = getSelectableOperationPanels(method);
            if (opPanels != null) {
                for (SelectableOperationPanel opPanel : opPanels) {
                    opNames.add(opPanel.getOperationName());
                    inputEOTs.add(opPanel.getRequestEOT());
                    outputEOTs.add(opPanel.getResponseEOT());
                    properties.add(opPanel.getValidatableProperties());
                }
            }

            model.setOperationNames(opNames, method);
            model.setInputEOTs(inputEOTs, method);
            model.setOutputEOTs(outputEOTs, method);
            model.setProperties(properties, method);
        }
    }

    /**
     * Data model that this view/panel can understand. Implement this interface
     * to supply this panel with content.
     */
    public interface Model extends FormModel {

        List<String> getOperationNames(RESTMethod method);
        void setOperationNames(List<String> opNames, RESTMethod method);

        List<ElementOrType> getInputEOTs(RESTMethod method);
        void setInputEOTs(List<ElementOrType> inputEOT, RESTMethod method);

        List<ElementOrType> getOutputEOTs(RESTMethod method);
        void setOutputEOTs(List<ElementOrType> outputEOT, RESTMethod method);

        List<EditableProperties> getProperties(RESTMethod method);
        void setProperties(List<EditableProperties> properties, RESTMethod method);
    }
}
