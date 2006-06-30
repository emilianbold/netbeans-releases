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

package org.netbeans.jellytools.properties.editors;

/*
 * FileCustomEditorOperator.java
 *
 * Created on June 13, 2002, 4:01 PM
 */

import java.io.File;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling File Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class FileCustomEditorOperator extends NbDialogOperator {

    private JFileChooserOperator _fileChooser=null;
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param title String title of custom editor */
    public FileCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public FileCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    /** getter for JFileChooserOperator
     * @return JFileChooserOperator */    
    public JFileChooserOperator fileChooser() {
        if (_fileChooser==null) {
            _fileChooser=new JFileChooserOperator(this);
        }
        return _fileChooser;
    }
    
    /** returns edited file
     * @return File */    
    public File getFileValue() {
        return fileChooser().getSelectedFile();
    }
    
    /** sets edited file
     * @param file File */    
    public void setFileValue(File file) {
        // Need to go from parent to file because events are not fired when
        // only setSelectedFile(file) is used.
        // select parent directory
        fileChooser().setSelectedFile(file.getParentFile());
        // go into dir
        fileChooser().enterSubDir(file.getParentFile().getName());
        // wait file is displayed
        fileChooser().waitFileDisplayed(file.getName());
        // select file
        fileChooser().selectFile(file.getName());
    }
    
    /** sets edited file
     * @param fileName String file name */    
    public void setFileValue(String fileName) {
        setFileValue(new File(fileName));
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        fileChooser();
    }
}
