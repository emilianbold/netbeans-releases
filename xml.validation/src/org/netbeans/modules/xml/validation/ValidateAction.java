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
    private static final Icon icon = new ImageIcon(Utilities.loadImage(
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
