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
package org.netbeans.modules.java.editor.codegen;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.java.editor.codegen.ui.GenerateCodePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class GenerateCodeAction extends BaseAction {

    public static final String generateCode = "generate-code";

    public GenerateCodeAction(){
        super(generateCode);
        putValue(ExtKit.TRIMMED_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("generate-code-trimmed")); // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getBundle(GenerateCodeAction.class).getString("desc-generate-code")); // NOI18N
        putValue(POPUP_MENU_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("popup-generate-code")); // NOI18N
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            FileObject file = od.getPrimaryFile();
            final JavaSource js = JavaSource.forFileObject(file);
            
            final GenerateCodePanel panel = new GenerateCodePanel();
            
            DialogDescriptor dd = new DialogDescriptor(panel, "Generate Code");
            
            Dialog d = DialogDisplayer.getDefault().createDialog(dd);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        panel.intialize(js, target.getCaretPosition());
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            });

            d.setVisible(true);

            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                GenerateData data = panel.getData();

                if (data != null) {
                    data.generate();
                }
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

}
