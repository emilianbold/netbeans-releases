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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.xml.validation.ValidateAction;
//import org.netbeans.modules.xml.validation.ui.ValidationOutputWindow;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Action to validate CASA.
 * 
 * @author jqian
 */
public class CasaValidateAction extends ValidateAction {

    private CasaWrapperModel model;

    public CasaValidateAction(CasaWrapperModel model) {
        super(model);

        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                if (CasaFactory.getCasaCustomizer().getBOOLEAN_DISABLE_VALIDATION()) {
                    return; // skip validation...
                }
long t1 = System.currentTimeMillis();
System.out.println("Validation Start: "+t1);
                RunAction runAction = new RunAction();
                runAction.run();

                List<ResultItem> validationResults =
                        runAction.getValidationResults();

//                    CasaValidationController controller = 
////                        ((CasaDataObject)getDataObject()).getLookup().lookup(
////                            CasaValidationController.class);
//                            ((CasaWrapperModel)getModel()).getValidationController();
                CasaValidationController controller =
                        model.getValidationController(); // FIXME

                // Send the complete validation results to the validation controller
                // so that clients can be notified.
                if (controller != null) {
                    controller.notifyCompleteValidationResults(validationResults);
                }
long t2 = System.currentTimeMillis();
System.out.println("Validation EndAt: "+ t2 + ", "+ (t2 -t1));
            }
        });
    }

    public class RunAction implements Runnable {

        private List<ResultItem> validationResults;

        public void run() {
            
            assert ! SwingUtilities.isEventDispatchThread();
            validationResults = model.validate(); 

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    InputOutput io = IOProvider.getDefault().getIO(
                            NbBundle.getMessage(ValidateAction.class,
                            "TITLE_XML_check_window"), false); // NOI18N
                    io.select();
                    
                    OutputWriter writer = io.getOut();
                    try {
                        writer.reset();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    writer.println(NbBundle.getMessage(ValidateAction.class,
                            "MSG_XML_valid_start")); // NOI18N

                    ValidationOutputWindow outputWindow = new ValidationOutputWindow();
                    outputWindow.displayValidationInformation(validationResults);

                    writer.print(NbBundle.getMessage(ValidateAction.class,
                            "MSG_XML_valid_end")); // NOI18N
                }
            });
        }

        public List<ResultItem> getValidationResults() {
            return validationResults;
        }
    }
}
