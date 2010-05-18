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
 * License.  When distributing the software, include this License Header
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.api.io.serialization;

import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public final class DocumentErrorHandlerSupport {
    /**
     * Visuals content of DocumentErrorHandler. All messages (wrning and errors) are displayed in
     * informational dialog window.
     * @return Object result of decision in dialog window error dialog: -1 Cancel, 0 - Ok, 2 Cancel, null - no errors
     * @param errorHandler
     * @param fileName
     */
    public static void showDocumentErrorHandlerDialog(final DocumentErrorHandler errorHandler, final FileObject file) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (errorHandler.getErrors().isEmpty() && errorHandler.getWarnings().isEmpty()) {
                    return;
                }
                StringBuffer warnings = new StringBuffer();

                if (!errorHandler.getWarnings().isEmpty()) {
                    warnings.append("<HTML>  " + NbBundle.getMessage(DocumentErrorHandlerSupport.class, "MSG_DialogWarning") + "<FONT COLOR=BLUE> <UL>"); //NOI18N
                    for (String warning : errorHandler.getWarnings()) {
                        warnings.append("<LI>"); //NOI18N
                        warnings.append(warning);
                        warnings.append("</LI>"); //NOI18N
                    }
                    warnings.append("</UL>"); //NOI18N
                    warnings.append("</FONT COLOR"); //NOI18N
                }
                StringBuffer errors = new StringBuffer();
                if (!errorHandler.getErrors().isEmpty()) {
                    errors.append("<HTML> " + NbBundle.getMessage(DocumentErrorHandlerSupport.class, "MSG_DialogError") + "<FONT COLOR=BLUE> <UL>"); //NOI18N
                    for (String error : errorHandler.getErrors()) {
                        errors.append("<LI>"); //NOI18N
                        errors.append(error);
                        errors.append("</LI>"); //NOI18N
                    }
                    errors.append("</UL>"); //NOI18N
                    errors.append("</FONT COLOR"); //NOI18N
                }
                String title = NbBundle.getMessage(DocumentErrorHandlerSupport.class, "MSG_DialogTitle") + " " + file.getName(); //NOI18N
                String closeButton = NbBundle.getMessage(DocumentErrorHandlerSupport.class,"MSG_close_button"); //NOI18N
                String openButton = NbBundle.getMessage(DocumentErrorHandlerSupport.class,"MSG_open_button");
                DialogDescriptor descriptor;
                if (!errorHandler.getErrors().isEmpty()) {
                    String message = errors.toString() + warnings.toString() + NbBundle.getMessage(DocumentErrorHandlerSupport.class, "MSG_Error"); //NOI18N
                    descriptor = new DialogDescriptor(message, title, true, new Object[]{closeButton}, null, 0, HelpCtx.DEFAULT_HELP, null);
                } else {
                    String message = errors.toString() + warnings.toString() + NbBundle.getMessage(DocumentErrorHandlerSupport.class, "MSG_Warning"); //NOI18N
                    descriptor = new DialogDescriptor(message, title, true, new Object[]{openButton, closeButton}, null, 0, HelpCtx.DEFAULT_HELP, null);
                }
                try {
                    Object result = DialogDisplayer.getDefault().notify(descriptor);
                    if (result != openButton) {
                        IOSupport.getCloneableEditorSupport(DataObject.find(file)).close();
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
}
