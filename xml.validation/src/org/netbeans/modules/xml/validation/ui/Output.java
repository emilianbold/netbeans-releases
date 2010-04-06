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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.validation.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.windows.InputOutput;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.08.31
 */
public class Output {

    public List<ResultItem> validate(Model model) {
        return validate(model, null);
    }

    public List<ResultItem> validate(Model model, ResultType type) {
        if (model !=null && !model.inSync()) {
            try {
                model.sync();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        Validation validation = new Validation();
        validation.validate(model, ValidationType.COMPLETE);

        List<ResultItem> validationResult = validation.getValidationResult();
        List<ResultItem> result;

        if (type == null) {
            result = validationResult;
        }
        else {
            result = new ArrayList<ResultItem>();

            for (ResultItem item : validationResult) {
                if (item.getType() == type) {
                    result.add(item);
                }
            }
        }
        printGuidanceInformation(result);

        return result;
    }
    
    private void printGuidanceInformation(List<ResultItem> results) {
        InputOutput io = IOProvider.getDefault().getIO(i18n(Output.class, "LBL_XML_Check_Window"), false); // NOI18N
        
        normalWriter = io.getOut();
        errorWriter = io.getErr();

        for (ResultItem resultItem: results) {
            ResultType resultType = resultItem.getType();
            Component component = resultItem.getComponents();
            
            try {
                if (resultType.equals(ResultType.ERROR)) {
                    showError(resultItem);
                }
                else if (resultType.equals(ResultType.WARNING)) {
                    showWarning(resultItem);
                }
                else if (resultType.equals(ResultType.ADVICE)) {
                    showAdvice(resultItem);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            normalWriter.println();
        }
        printCountSummary(results);
    }

    private void showError(ResultItem resultItem) throws IOException {
        showMessage(i18n(Output.class, "MSG_Error"), resultItem); // NOI18N
    }
    
    private void showWarning(ResultItem resultItem) throws IOException {
        showMessage(i18n(Output.class, "MSG_Warning"), resultItem); // NOI18N
    }
    
    private void showAdvice(ResultItem resultItem) throws IOException {
        normalWriter.println(getFileName(resultItem) + " :" + getLineNumber(resultItem) + "," + getColumnNumber(resultItem)); // NOI18N
        normalWriter.println(resultItem.getDescription());
    }

    private void showMessage(String errorTypeStr, ResultItem resultItem) throws IOException {
        OutputListener listener = new ValidationOutputListener(resultItem);
        int lineNumber = getLineNumber(resultItem);
        int columnNumber = getColumnNumber(resultItem);

        if (lineNumber == -1 || columnNumber == -1) {
            errorWriter.println(getFileName(resultItem) + ": " + i18n(Output.class, "MSG_Position_Unavailable"), listener, false); // NOI18N
        }
        else {
            errorWriter.println(getFileName(resultItem) + ":" + lineNumber + "," + columnNumber, listener, false);
        }
        errorWriter.println(errorTypeStr + " " + resultItem.getDescription());
    }

    private String getFileName(ResultItem resultItem) {
        return ((FileObject) resultItem.getModel().getModelSource().getLookup().lookup(FileObject.class)).getPath();
    }

    private StyledDocument getStyledDocument(Component component) {
        int position = 0;
        DataObject data = null;
        
        try {
            Model model = component.getModel();
            
            if (model == null) {
                return null;
            }
            FileObject file = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);

            if (file == null) {
                return null;
            }
            data = DataObject.find(file);
        }
        catch (DataObjectNotFoundException e) {
            e.printStackTrace();
        }
        return ((CloneableEditorSupport) data.getCookie(EditorCookie.class)).getDocument();
    }

    private int getLineNumber(ResultItem resultItem) {
        if (resultItem.getComponents() == null) {
            return resultItem.getLineNumber();
        }
        StyledDocument doc = getStyledDocument(resultItem.getComponents());

        if (doc == null) {
            return -1;
        }
        int position = getPosition(resultItem.getComponents());
        return NbDocument.findLineNumber(doc, position) + 1;
    }

    private int getColumnNumber(ResultItem resultItem) {
        if (resultItem.getComponents() == null) {
            return resultItem.getColumnNumber();
        }
        StyledDocument doc = getStyledDocument(resultItem.getComponents());

        if (doc == null) {
            return -1;
        }
        int position = getPosition(resultItem.getComponents());
        return NbDocument.findLineColumn(doc, position);
    }

    private int getPosition(Component component) {
        if (component instanceof DocumentComponent) {
            return ((DocumentComponent)component).findPosition();
        }
        return 0;
    }

    private void printCountSummary(List<ResultItem> validationInformation) {
        int warnings = 0;
        int errors = 0;
        
        for (ResultItem resultItem: validationInformation) {
            if (resultItem.getType().equals(ResultType.ERROR)) {
                errors ++;
            }
            else if (resultItem.getType().equals(ResultType.WARNING)) {
                warnings++;
            }
        }
        normalWriter.println(errors + " Error(s),  " + warnings +" Warning(s)."); // NOI18N
    }

    private final class ValidationOutputListener implements OutputListener {
        private FileObject fileObject;
        private ResultItem myResultItem;
        
        public ValidationOutputListener(ResultItem resultItem) {
            fileObject = (FileObject) resultItem.getModel().getModelSource().getLookup().lookup(FileObject.class);
            myResultItem = resultItem;
        }
        
        public void outputLineSelected(OutputEvent event) {}
        public void outputLineCleared(OutputEvent event) {}
        
        public void outputLineAction(OutputEvent event) {
            try {
                DataObject data = DataObject.find(fileObject);
                
                if (data == null) {
                    return;
                }
                ShowCookie editorCookie = (ShowCookie) data.getCookie(ShowCookie.class);
                
                if (editorCookie != null) {
                    editorCookie.show(myResultItem);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private OutputWriter errorWriter;
    private OutputWriter normalWriter;
}
