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

package org.netbeans.modules.xml.validation;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.modules.xml.validation.ui.ValidationOutputWindow;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;



/**
 * Validates XML file sending results to output window.
 *
 */
public class ValidateAction extends AbstractAction {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ACCELERATOR = "alt shift F9"; // NOI18N
    private static final Icon icon = new ImageIcon(ImageUtilities.loadImage(
            "org/netbeans/modules/xml/validation/resources/validation.png")); 
    private static final String label = NbBundle.getMessage(
            ValidateAction.class,"NAME_Validate_XML");
    private Model model;
    
    
    public ValidateAction(Model model) {
        super(label, icon); 
	putValue(NAME, label);
        putValue(SHORT_DESCRIPTION, label);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ACCELERATOR));
        this.model = model;
    }
    
    
    public void actionPerformed(ActionEvent event) {
        RequestProcessor.getDefault().post(new RunAction());
    }
    
    
    public class RunAction implements Runnable{
        
        private List<ResultItem> validationResults;
        
        public void run() {
            InputOutput io = IOProvider.getDefault().getIO(NbBundle.
                    getMessage(ValidationOutputWindow.class,
                    "TITLE_XML_check_window"), false);
            try {
                io.getOut().reset();
            } catch(IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            io.getOut().println(NbBundle.getMessage(
                    ValidateAction.class,"MSG_XML_valid_start"));
            io.select();
            
            ValidationOutputWindowController validationController =
                    new ValidationOutputWindowController();
            validationResults = validationController.validate(model);
            
            io.getOut().print(NbBundle.getMessage(
                    ValidateAction.class,"MSG_XML_valid_end"));
            io.select();
        }
        
        public List<ResultItem> getValidationResults() {
            return validationResults;
        }
    }
}
