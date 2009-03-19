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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.io.IOException;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;

/**
 * Class to manage displaying validation information in the Output window.
 * @author Praveen Savur
 */
public class ValidationOutputWindow {
    OutputWriter normalWriter;
    OutputWriter errorWriter;

    private String warningMsgType = null;

    private String errorMsgType = null;

    /**
     * Creates a new instance of ValidationOutputWindow
     */
    public ValidationOutputWindow() {
        initialise();
    }


    /**
     * Display Validation Results in the output window.
     * @param validationInformation validation information that has to be displayed.
     */
    public void displayValidationInformation(List<ResultItem> validationInformation) {

        for(ResultItem resultItem: validationInformation) {
            ResultType resultType = resultItem.getType();

            Component component = resultItem.getComponents();

            try {
                if(resultType.equals(ResultType.ERROR))
                    showError(resultItem);
                else if(resultType.equals(ResultType.WARNING))
                    showWarning(resultItem);
                else if(resultType.equals(ResultType.ADVICE))
                    showAdvice(resultItem);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            normalWriter.println("");

        }

        printCountSummary(validationInformation);
    }




    /**
     *  Initialise streams.
     */
    private void initialise() {
        InputOutput io = IOProvider.getDefault().getIO(NbBundle.
                getMessage(ValidationOutputWindow.class, "TITLE_XML_check_window"), false);

        normalWriter = io.getOut();
        errorWriter = io.getErr();

        warningMsgType = NbBundle.
                getMessage(ValidationOutputWindow.class, "MSG_WARNING");

        errorMsgType = NbBundle.
                getMessage(ValidationOutputWindow.class, "MSG_ERROR");

    }


    /**
     * Display Error information.
     */
    private void showError(ResultItem resultItem)
    throws IOException {
        showMessage(errorMsgType, resultItem, true);
    }


    /**
     *  Display warning information.
     */
    private void showWarning(ResultItem resultItem)
    throws IOException {
        showMessage(warningMsgType, resultItem, false);
    }

    private void showMessage(String errorTypeStr, ResultItem resultItem, boolean importance)
    throws IOException {
        OutputListener listener = new ValidationOutputListener(resultItem);
        int lineNumber = getLineNumber(resultItem);
        int columnNumber = getColumnNumber(resultItem);
        if(lineNumber == -1 || columnNumber == -1) {
            errorWriter.println(getFileName(resultItem) + ": " + NbBundle.getMessage(ValidationOutputWindow.class,
                    "MSG_Position_Unavailable"),
                    listener, importance);
        } else {
            errorWriter.println(getFileName(resultItem) + ":" + lineNumber
                    + "," + columnNumber,
                    listener, importance);
        }
        errorWriter.println(errorTypeStr + " " + resultItem.getDescription());
    }



    /**
     *  Display advice information.
     */
    private void showAdvice(ResultItem resultItem)
    throws IOException {
        normalWriter.println(getFileName(resultItem) + " :" +
                getLineNumber(resultItem) + "," + getColumnNumber(resultItem));
        normalWriter.println(resultItem.getDescription());
    }


    /**
     *  Get filename from ResultItem.
     */
    private String getFileName(ResultItem resultItem) {
        String fileName;

        assert resultItem.getModel() != null: "Model associated with ResultItem is null"; // NOI18N
        fileName = ((FileObject) resultItem.getModel().getModelSource().
                getLookup().lookup(FileObject.class)).getPath();

        return fileName;
    }


    /**
     *  Get styled document from Component.
     */
    private StyledDocument getStyledDocument(Component component) {
        int position = 0;
        DataObject dobj = null;

        try {
            Model model = component.getModel();

            // Model can be null, if component has been deleted from model
            // while validation was still in progress.
            if(model == null)
                return null;
            dobj = DataObject.find((FileObject) model.
                    getModelSource().getLookup().lookup(FileObject.class));
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        CloneableEditorSupport editor = (CloneableEditorSupport)dobj.
                getCookie(org.openide.cookies.EditorCookie.class);
        StyledDocument doc = editor.getDocument();

        return doc;
    }


    /**
     *  Get line number from ResultItem.
     */
    private int getLineNumber(ResultItem resultItem) {
        int lineNumber;

        if(resultItem.getComponents() != null) {
            StyledDocument doc = getStyledDocument(resultItem.getComponents());
            // Happens if model is modified during validation.
            if(doc==null)
                return -1;
            int position = getPosition(resultItem.getComponents());
            lineNumber = NbDocument.findLineNumber(doc, position) + 1;
        } else {
            lineNumber = resultItem.getLineNumber();
        }
        return lineNumber;
    }


    /**
     *  Get column number from ResultItem.
     */
    private int getColumnNumber(ResultItem resultItem) {
        int columnNumber;

        if(resultItem.getComponents() != null) {
            StyledDocument doc = getStyledDocument(resultItem.getComponents());
            // Happens if model is modified during validation.
            if(doc==null)
                return -1;
            int position = getPosition(resultItem.getComponents());
            columnNumber = NbDocument.findLineColumn(doc, position);
        } else {
            columnNumber = resultItem.getColumnNumber();
        }
        return columnNumber;
    }


    /**
     * Get Position from component.
     */
    private int getPosition(Component component) {
        int position = 0;

        // TODO: Is this valid.
        if(component instanceof DocumentComponent) {
            position = ((DocumentComponent)component).findPosition();
        }
        return position;
    }


    private void printCountSummary(List<ResultItem> validationInformation) {
        int warnings = 0;
        int errors = 0;

        for(ResultItem resultItem: validationInformation) {
            if(resultItem.getType().equals(ResultType.ERROR))
                errors ++;
            else if(resultItem.getType().equals(ResultType.WARNING))
                warnings++;
        }

        normalWriter.println(errors + " Error(s),  " + warnings +" Warning(s).");
    }



    /**
     *  Class to handle callbacks from URL's in the output window.
     */
    private final class ValidationOutputListener
            implements org.openide.windows.OutputListener {

        FileObject fileObject;
        ResultItem resultItem;

        public ValidationOutputListener(ResultItem resultItem) {
            fileObject = (FileObject) resultItem.getModel().
                    getModelSource().getLookup().lookup(FileObject.class);
            this.resultItem = resultItem;
        }

        public void outputLineSelected(org.openide.windows.OutputEvent ev) {
        }

        public void outputLineAction(org.openide.windows.OutputEvent ev) {

            try {
                DataObject dataObject = DataObject.find(fileObject);

                if(dataObject == null)
                    return;

                ShowCookie editorCookie = (ShowCookie)
                dataObject.getCookie(ShowCookie.class);

                if(editorCookie != null)
                    editorCookie.show(resultItem);

            } catch (IOException ex){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        public void outputLineCleared(org.openide.windows.OutputEvent ev) {
        }
    }
}
