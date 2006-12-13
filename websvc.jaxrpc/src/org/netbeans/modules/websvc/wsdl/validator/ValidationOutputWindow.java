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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.wsdl.validator;

import java.io.IOException;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Class to manage displaying validation information in the Output window.
 * @author Praveen Savur
 * @author Rico Cruz
 */
public class ValidationOutputWindow {
    
    OutputWriter normalWriter;
    OutputWriter errorWriter;
    
    /**
     * Creates a new instance of ValidationOutputWindow
     */
    public ValidationOutputWindow() {
        initialize();
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
    private void initialize() {
        InputOutput io = IOProvider.getDefault().getIO(NbBundle.
                getMessage(ValidationOutputWindow.class, "TITLE_XML_check_window"), false);
        
        normalWriter = io.getOut();
        errorWriter = io.getErr();
    }
    
    
    /**
     * Display Error information.
     */
    private void showError(ResultItem resultItem)
    throws IOException {
        
        OutputListener listener = new ValidationOutputListener(resultItem);
        errorWriter.println(getFileName(resultItem) + ":" +
                getLineNumber(resultItem) + "," + getColumnNumber(resultItem),
                listener, true);
        errorWriter.println(resultItem.getDescription());
    }
    
    
    /**
     *  Display warning information.
     */
    private void showWarning(ResultItem resultItem)
    throws IOException {
        OutputListener listener = new ValidationOutputListener(resultItem);
        errorWriter.println(getFileName(resultItem) + ":" +
                getLineNumber(resultItem) + "," + getColumnNumber(resultItem),
                listener, false);
        errorWriter.println(resultItem.getDescription());
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
            dobj = DataObject.find((FileObject) component.getModel().
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
                
                CloneableEditorSupport editor = (CloneableEditorSupport)dataObject.
                        getCookie(org.openide.cookies.EditorCookie.class);
                JEditorPane[] panes = editor.getOpenedPanes();
                boolean focusFound = false;
                if(panes.length > 0){
                    for(int i = 0; i < panes.length; i++){
                        JEditorPane pane = panes[i];
                        if(pane.hasFocus()){
                            setCaretPosition(pane);
                            focusFound = true;
                            break;
                        }
                    }
                    if(!focusFound){
                        JEditorPane pane = panes[0];
                        pane.requestFocusInWindow();
                        setCaretPosition(pane);
                    }
                }
            } catch (IOException ex){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
        }
        
        private void setCaretPosition(JEditorPane pane){
            Component component = resultItem.getComponents();
            if(component instanceof WSDLComponent){
                int position = ((WSDLComponent)component).findPosition();
                pane.setCaretPosition(position);
            }else {
                int line = resultItem.getLineNumber();
                try {
                    int position = NbDocument.findLineOffset(
                            (StyledDocument)pane.getDocument(),line);
                    pane.setCaretPosition(position);
                } catch (IndexOutOfBoundsException iob) {
                    // ignore
                }
            }
        }
        
        public void outputLineCleared(org.openide.windows.OutputEvent ev) {
        }
    }
}