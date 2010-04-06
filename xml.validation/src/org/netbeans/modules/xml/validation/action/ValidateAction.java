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
package org.netbeans.modules.xml.validation.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.openide.filesystems.FileObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.validation.ui.Output;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.08.31
 */
public class ValidateAction extends AbstractAction {

    public ValidateAction(Controller controller) {
        this(null, null, controller);
    }

    public ValidateAction(Model model) {
        this(model, null, null);
    }

    public ValidateAction(Model model, ResultType resultType) {
        this(model, resultType, null);
    }

    private ValidateAction(Model model, ResultType resultType, Controller controller) {
        super(null, icon(ValidateAction.class, "validate")); // NOI18N
        String name = i18n(ValidateAction.class, "LBL_Validate_XML"); // NOI18N
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK));
        myModel = model;
        myResultType = resultType;
        myController = controller;
    }
    
    public void actionPerformed(ActionEvent event) {
        if (myController == null) {
            InputOutput io = IOProvider.getDefault().getIO(i18n(ValidateAction.class, "LBL_XML_Check_Window"), false); // NOI18N
            
            try {
                io.getOut().reset();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            String extension = "XML"; //NOI18N
            try {
                FileObject file = myModel.getModelSource().getLookup().lookup(FileObject.class);
                extension = file.getExt().toUpperCase();
            } catch (Exception ex) {
                //if fails, it'll be XML
            }

            io.getOut().println(i18n(ValidateAction.class, "MSG_Validation_Started", extension)); // NOI18N
            io.select();

            myResults = new Output().validate(myModel, myResultType);
            
            io.getOut().print(i18n(ValidateAction.class, "MSG_Validation_Finished", extension)); // NOI18N
            io.select();
        }
        else {
            myController.startValidation();
        }
    }

    public List<ResultItem> getValidationResults() {
        return myResults;
    }

    private Model myModel;
    private Controller myController;
    private ResultType myResultType;
    private List<ResultItem> myResults;
}
